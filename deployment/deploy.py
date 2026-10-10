#!/usr/bin/env python3
"""
Deploy the erp stack (backend + frontend) to the shared production server.

    python3 backend/deployment/deploy.py deploy [--only backend|frontend] [--prune] [--no-rollback]
    python3 backend/deployment/deploy.py backup
    python3 backend/deployment/deploy.py rollback [--only backend|frontend]
    python3 backend/deployment/deploy.py status
    python3 backend/deployment/deploy.py logs backend|frontend [--lines N]
    python3 backend/deployment/deploy.py restart [backend|frontend]
    python3 backend/deployment/deploy.py stop

This is an IN-PLACE update of the live system: the containers keep their
production names (erp-backend, erp-frontend), host networking and ports
(7272, 80). The database container postgres-db is never recreated - this
script only runs pg_dump and SELECTs against it.

Images are built ON THE SERVER (x86_64). Flyway is OFF in production
(SPRING_FLYWAY_ENABLED=false in docker-compose.prod.yml).
If the new version is not healthy or a post-deploy check fails, deploy rolls
back to the :previous images automatically (unless --no-rollback).

SSH: one multiplexed connection per run (ControlMaster). If ERP_SSH_PASSWORD
is set, it is handed to ssh through an SSH_ASKPASS helper in a temp dir;
otherwise ssh uses your keys / prompts as usual. The password is never
printed or written into the repo.

Never runs `docker compose down -v`, `docker volume rm`, `docker system prune`,
or anything against containers/images/volumes outside this project.

Standard library only; needs the system `ssh` and `tar`.
"""
import argparse
import json
import os
import shlex
import shutil
import subprocess
import sys
import tempfile
import time
import urllib.error
import urllib.request
from pathlib import Path

# ---------------------------------------------------------------- settings
HOST = "89.117.37.75"
SSH_USER = "remote"
SSH_PORT = 22
PROJECT = "erp"
REMOTE_DIR = "erp-deploy"          # ~/erp-deploy on the server
BACKUP_DIR = "erp-backups"         # ~/erp-backups on the server
KEEP_BACKUPS = 10
PASSWORD_ENV = "ERP_SSH_PASSWORD"

HERE = Path(__file__).resolve().parent          # backend/deployment
WORKSPACE = HERE.parent.parent                   # workspace root: backend/, frontend/
ENV_FILE = HERE / ".env.prod"

SERVICES = {
    "backend": {
        "container": "erp-backend",
        "image": "erp-backend",
        "port": 7272,
        "src": WORKSPACE / "backend",
        "include": ["pom.xml", "src"],
        "dockerfile": HERE / "backend.Dockerfile",
        "extra": {},
    },
    "frontend": {
        "container": "erp-frontend",
        "image": "erp-frontend",
        "port": 80,
        "src": WORKSPACE / "frontend" / "mxdashboard",
        "include": ["package.json", "package-lock.json", "index.html", "public", "src",
                    "tsconfig.json", "tsconfig.app.json", "tsconfig.node.json", "vite.config.ts"],
        "dockerfile": HERE / "frontend.Dockerfile",
        "extra": {"nginx.conf": HERE / "nginx.conf"},
    },
}

REQUIRED_KEYS = ["DB_URL", "DB_USERNAME", "DB_PASSWORD", "JWT_SECRET", "FILE_ACCESS_TOKEN_SECRET",
                 "FRONTEND_URL", "CORS_ALLOWED_ORIGINS", "DB_CONTAINER", "DB_NAME", "PUBLIC_URL"]
EXCLUDE_NAMES = {"node_modules", "dist", "target", ".git", ".DS_Store", "coverage",
                 "test-results", "playwright-report", "__pycache__"}

HEALTH_TIMEOUT = 420   # seconds to wait for the backend to become healthy
COMPOSE = f"docker compose -p {PROJECT} --env-file .env -f docker-compose.yml"


def info(msg):
    print(f"==> {msg}", flush=True)


class Failed(Exception):
    """A post-switch check failed - deploy rolls back automatically."""


def die(msg):
    print(f"ERROR: {msg}", file=sys.stderr, flush=True)
    sys.exit(1)


# ---------------------------------------------------------------- ssh
class Remote:
    def __init__(self):
        self.tmp = tempfile.mkdtemp(prefix="erpd-", dir="/tmp")  # short path: unix socket limit
        self.sock = os.path.join(self.tmp, "cm")
        self.env = dict(os.environ)
        if os.environ.get(PASSWORD_ENV):
            askpass = os.path.join(self.tmp, "askpass.sh")
            with open(askpass, "w") as f:
                f.write(f'#!/bin/sh\nprintf \'%s\\n\' "${PASSWORD_ENV}"\n')
            os.chmod(askpass, 0o700)
            self.env.update(SSH_ASKPASS=askpass, SSH_ASKPASS_REQUIRE="force", DISPLAY=":0")
        self.base = ["ssh", "-p", str(SSH_PORT), "-o", f"ControlPath={self.sock}",
                     "-o", "StrictHostKeyChecking=accept-new", f"{SSH_USER}@{HOST}"]

    def open(self):
        cmd = self.base[:1] + ["-fN", "-o", "ControlMaster=yes", "-o", "ControlPersist=900"] + self.base[1:]
        if subprocess.run(cmd, env=self.env).returncode != 0:
            die(f"cannot open ssh to {SSH_USER}@{HOST}:{SSH_PORT}")

    def close(self):
        subprocess.run(self.base[:1] + ["-O", "exit"] + self.base[1:],
                       env=self.env, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        shutil.rmtree(self.tmp, ignore_errors=True)

    def sh(self, script, check=True, capture=False, stdin=None):
        """Run a bash script on the server (sent on stdin unless stdin is given)."""
        if stdin is None:
            cmd, inp = self.base + ["bash -s"], ("set -euo pipefail\n" + script).encode()
        else:
            cmd, inp = self.base + ["bash -c " + shlex.quote("set -euo pipefail\n" + script)], None
        p = subprocess.run(cmd, env=self.env, input=inp, stdin=stdin if inp is None else None,
                           stdout=subprocess.PIPE if capture else None)
        if check and p.returncode != 0:
            die(f"remote command failed (exit {p.returncode})")
        return p.stdout.decode() if capture else p.returncode

    def out(self, script):
        return self.sh(script, capture=True).strip()


# ---------------------------------------------------------------- helpers
def load_env():
    if not ENV_FILE.exists():
        die(f"{ENV_FILE} missing - copy .env.prod.example and fill it with the production values")
    env = {}
    for line in ENV_FILE.read_text().splitlines():
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            k, v = line.split("=", 1)
            env[k.strip()] = v.strip()
    missing = [k for k in REQUIRED_KEYS if not env.get(k)]
    if missing:
        die(f".env.prod: required keys empty or missing: {', '.join(missing)}")
    return env


def pick(only):
    return [only] if only else list(SERVICES)


def package(services):
    """Stage only what the Dockerfiles need, tar it with the system tar."""
    stage = Path(tempfile.mkdtemp(prefix="erp-pkg-"))

    def ignore(_dir, names):
        return {n for n in names if n in EXCLUDE_NAMES or n.startswith(".env") or n.startswith("._")}

    shutil.copy(HERE / "docker-compose.prod.yml", stage / "docker-compose.yml")
    shutil.copy(ENV_FILE, stage / ".env")
    for name in services:
        svc = SERVICES[name]
        dst = stage / name
        dst.mkdir()
        for item in svc["include"]:
            src = svc["src"] / item
            if not src.exists():
                die(f"{src} not found")
            if src.is_dir():
                shutil.copytree(src, dst / item, ignore=ignore)
            else:
                shutil.copy(src, dst / item)
        shutil.copy(svc["dockerfile"], dst / "Dockerfile")
        for target, src in svc["extra"].items():
            shutil.copy(src, dst / target)
    tarball = stage.parent / (stage.name + ".tgz")
    env = dict(os.environ, COPYFILE_DISABLE="1")  # no macOS ._ files
    subprocess.run(["tar", "-czf", str(tarball), "-C", str(stage), "."], check=True, env=env)
    shutil.rmtree(stage)
    info(f"package: {tarball.stat().st_size / 1e6:.1f} MB ({', '.join(services)})")
    return tarball


def upload(r, tarball, services):
    dirs = " ".join(services)
    with open(tarball, "rb") as f:
        r.sh(f"""
            mkdir -p ~/{REMOTE_DIR}/.incoming && cd ~/{REMOTE_DIR}
            rm -rf .incoming && mkdir .incoming
            tar -xzf - -C .incoming
            for d in {dirs}; do rm -rf "$d"; mv ".incoming/$d" "$d"; done
            mv .incoming/docker-compose.yml docker-compose.yml
            mv .incoming/.env .env && chmod 600 .env
            rm -rf .incoming
        """, stdin=f)
    os.remove(tarball)
    info(f"uploaded to ~/{REMOTE_DIR}")


def backup(r, env):
    info("backup: pg_dump of the production database")
    stamp = time.strftime("%Y%m%d-%H%M%S")
    c, db, u = shlex.quote(env["DB_CONTAINER"]), shlex.quote(env["DB_NAME"]), shlex.quote(env["DB_USERNAME"])
    path = r.out(f"""
        umask 077; mkdir -p ~/{BACKUP_DIR}
        f=~/{BACKUP_DIR}/{env['DB_NAME']}_{stamp}.dump
        docker exec {c} pg_dump -U {u} -d {db} -Fc > "$f.part"
        docker exec -i {c} pg_restore -l < "$f.part" | grep -q 'TABLE DATA' || {{ echo "dump not restorable" >&2; exit 1; }}
        mv "$f.part" "$f"
        ls -1t ~/{BACKUP_DIR}/{env['DB_NAME']}_*.dump | tail -n +{KEEP_BACKUPS + 1} | xargs -r rm --
        echo "$f $(du -h "$f" | cut -f1)"
    """)
    info(f"backup: {path}")


def flyway_rows(r, env):
    c, db, u = shlex.quote(env["DB_CONTAINER"]), shlex.quote(env["DB_NAME"]), shlex.quote(env["DB_USERNAME"])
    return int(r.out(f"docker exec {c} psql -U {u} -d {db} -Atc 'select count(*) from flyway_schema_history'"))


def containers(r):
    """name -> {project, image, started, running, health} for every container on the host."""
    raw = r.out("docker ps -aq | xargs -r docker inspect")
    res = {}
    for c in json.loads(raw or "[]"):
        st = c["State"]
        res[c["Name"].lstrip("/")] = {
            "project": (c["Config"].get("Labels") or {}).get("com.docker.compose.project", ""),
            "image": c["Image"], "started": st.get("StartedAt"), "running": st.get("Running"),
            "health": (st.get("Health") or {}).get("Status", "-"),
        }
    return res


def ours(name):
    return name in {s["container"] for s in SERVICES.values()}


def port_check(r, services, ctrs):
    for name in services:
        svc = SERVICES[name]
        listening = r.out(f"ss -ltnH 'sport = :{svc['port']}' | head -1")
        own = ctrs.get(svc["container"], {}).get("running")
        if listening and not own:
            die(f"port {svc['port']} is held by something other than {svc['container']} - refusing to deploy")
    if "frontend" in services and r.sh("systemctl is-active --quiet nginx", check=False) == 0:
        die("host nginx is active and would hold port 80 - refusing to deploy")
    info("ports: " + ", ".join(f"{SERVICES[s]['port']} ok" for s in services))


def tag_previous(r, services, ctrs):
    for name in services:
        svc = SERVICES[name]
        c = ctrs.get(svc["container"])
        if c:
            r.sh(f"docker tag {c['image']} {svc['image']}:previous")
            info(f"tagged running {svc['container']} image as {svc['image']}:previous")


def adopt_legacy(r, services, ctrs):
    """First deploy only: our containers were created by the old per-folder compose projects.
    Remove exactly those containers (never volumes, never images) so project 'erp' can recreate them."""
    for name in services:
        cname = SERVICES[name]["container"]
        c = ctrs.get(cname)
        if c and c["project"] != PROJECT:
            info(f"replacing {cname} (was compose project '{c['project'] or 'none'}')")
            r.sh(f"docker stop {cname} >/dev/null && docker rm {cname} >/dev/null")


def wait_healthy(r, env, services):
    deadline = time.time() + HEALTH_TIMEOUT
    if "backend" in services:
        info("waiting for erp-backend to be healthy ...")
        while True:
            h = r.out("docker inspect -f '{{.State.Health.Status}}' erp-backend 2>/dev/null || echo missing")
            if h == "healthy":
                break
            if h in ("unhealthy", "missing") or time.time() > deadline:
                r.sh("docker logs --tail 60 erp-backend 2>&1 | grep -E 'ERROR|Caused by' | tail -5", check=False)
                raise Failed(f"erp-backend is {h}")
            time.sleep(5)
        info("erp-backend healthy")
    base = env["PUBLIC_URL"].rstrip("/")
    checks = [(base + "/", (200,))]
    if "backend" in services:
        checks.append((base + ":7272/actuator/health/readiness", (200,)))
    for url, ok in checks:
        while True:
            code = http_code(url)
            if code in ok:
                info(f"outside: {url} -> {code}")
                break
            if time.time() > deadline:
                raise Failed(f"outside: {url} -> {code}")
            time.sleep(5)


def http_code(url, data=None):
    req = urllib.request.Request(url, data=data, headers={"Content-Type": "application/json"} if data else {})
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            return resp.status
    except urllib.error.HTTPError as e:
        return e.code
    except Exception:
        return None


# ---------------------------------------------------------------- commands
def cmd_deploy(r, args):
    env = load_env()
    services = pick(args.only)
    r.open()
    before = containers(r)
    port_check(r, services, before)
    tarball = package(services)
    upload(r, tarball, services)
    backup(r, env)
    rows_before = flyway_rows(r, env)
    info(f"flyway_schema_history rows before: {rows_before}")
    tag_previous(r, services, before)
    info("building on the server ...")
    r.sh(f"cd ~/{REMOTE_DIR} && {COMPOSE} build {' '.join(services)}")
    adopt_legacy(r, services, before)
    try:
        r.sh(f"cd ~/{REMOTE_DIR} && {COMPOSE} up -d --no-build --no-deps {' '.join(services)}")
        wait_healthy(r, env, services)
        verify(r, env, services, before, rows_before)
    except (Failed, SystemExit) as e:
        print(f"ERROR: {e}", file=sys.stderr, flush=True)
        if args.no_rollback:
            die("deploy failed (automatic rollback disabled) - run `deploy.py rollback`")
        prev = [s for s in services if SERVICES[s]["container"] in before]
        info(f"deploy failed - rolling back {', '.join(prev)} automatically")
        rollback(r, env, prev)
        die("deploy failed; previous version restored")
    if args.prune:
        r.sh(f"docker image prune -f --filter label=com.docker.compose.project={PROJECT}")
    info("deploy OK")


def verify(r, env, services, before, rows_before):
    failed = False
    rows_after = flyway_rows(r, env)
    if rows_after != rows_before:
        failed = True
        print(f"FAIL flyway_schema_history rows {rows_before} -> {rows_after}")
    else:
        info(f"flyway_schema_history unchanged ({rows_after} rows)")
    if "backend" in services:
        hits = r.out("docker logs erp-backend 2>&1 | grep -iE 'flyway|migrating schema|successfully applied' || true")
        if hits:
            failed = True
            print("FAIL Flyway lines in backend log:\n" + hits)
        else:
            info("backend log: no Flyway lines")
    after = containers(r)
    for name, c in before.items():
        if ours(name):
            continue
        a = after.get(name)
        if not a or a["started"] != c["started"] or a["running"] != c["running"]:
            failed = True
            print(f"FAIL other container changed: {name}")
    info(f"other containers unchanged: {sum(1 for n in before if not ours(n))}")
    if failed:
        raise Failed("post-deploy verification failed")


def cmd_backup(r, args):
    env = load_env()
    r.open()
    backup(r, env)


def cmd_rollback(r, args):
    env = load_env()
    r.open()
    rollback(r, env, pick(args.only))


def rollback(r, env, services):
    for name in services:
        img = SERVICES[name]["image"]
        if r.sh(f"docker image inspect {img}:previous >/dev/null 2>&1", check=False) != 0:
            die(f"no {img}:previous image to roll back to")
    ctrs = containers(r)
    for name in services:
        img = SERVICES[name]["image"]
        r.sh(f"docker tag {img}:previous {img}:latest")
        info(f"{img}:previous -> {img}:latest")
    adopt_legacy(r, services, ctrs)
    r.sh(f"cd ~/{REMOTE_DIR} && {COMPOSE} up -d --no-build --no-deps --force-recreate {' '.join(services)}")
    try:
        wait_healthy(r, env, services)
    except Failed as e:
        die(f"rollback did not come up healthy: {e}")
    info("rollback OK")


def cmd_status(r, args):
    r.open()
    r.sh(f"""
        cd ~/{REMOTE_DIR} 2>/dev/null && {COMPOSE} ps || true
        echo; docker ps -a --filter name='^erp-(backend|frontend)$' --format 'table {{{{.Names}}}}\\t{{{{.Image}}}}\\t{{{{.Status}}}}'
        echo; docker images --format '{{{{.Repository}}}}:{{{{.Tag}}}}\\t{{{{.ID}}}}\\t{{{{.CreatedSince}}}}' | grep -E '^erp-(backend|frontend):' || true
        echo; ls -1t ~/{BACKUP_DIR} 2>/dev/null | head -3 || true
    """)


def cmd_logs(r, args):
    r.open()
    r.sh(f"docker logs --tail {int(args.lines)} {SERVICES[args.service]['container']} 2>&1")


def cmd_restart(r, args):
    r.open()
    r.sh(f"cd ~/{REMOTE_DIR} && {COMPOSE} restart {args.service or ''}")


def cmd_stop(r, args):
    r.open()
    r.sh(f"cd ~/{REMOTE_DIR} && {COMPOSE} stop")


def main():
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = p.add_subparsers(dest="cmd", required=True)
    d = sub.add_parser("deploy")
    d.add_argument("--only", choices=list(SERVICES))
    d.add_argument("--prune", action="store_true", help="remove dangling images of this compose project")
    d.add_argument("--no-rollback", action="store_true", help="do not roll back automatically on failure")
    sub.add_parser("backup")
    rb = sub.add_parser("rollback")
    rb.add_argument("--only", choices=list(SERVICES))
    sub.add_parser("status")
    lg = sub.add_parser("logs")
    lg.add_argument("service", choices=list(SERVICES))
    lg.add_argument("--lines", type=int, default=200)
    rs = sub.add_parser("restart")
    rs.add_argument("service", nargs="?", choices=list(SERVICES))
    sub.add_parser("stop")
    args = p.parse_args()

    r = Remote()
    try:
        {"deploy": cmd_deploy, "backup": cmd_backup, "rollback": cmd_rollback, "status": cmd_status,
         "logs": cmd_logs, "restart": cmd_restart, "stop": cmd_stop}[args.cmd](r, args)
    finally:
        r.close()


if __name__ == "__main__":
    main()
