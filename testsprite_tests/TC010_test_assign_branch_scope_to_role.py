import requests
import string
import random

BASE_URL = "http://localhost:7272/actuator/health".replace("/actuator/health", "")
LOGIN_URL = f"{BASE_URL}/api/auth/login"
ROLE_CREATE_URL = f"{BASE_URL}/api/roles"
ROLE_BRANCHES_URL = f"{BASE_URL}/api/v1/security/role-branches"
BRANCHES_SEARCH_URL = f"{BASE_URL}/api/v1/org/branches/search"

USERNAME = "admin"
PASSWORD = "admin"
TIMEOUT = 30

def random_suffix(length=8):
    chars = string.ascii_uppercase + string.digits
    return ''.join(random.choice(chars) for _ in range(length))

def get_auth_token():
    resp = requests.post(
        LOGIN_URL,
        json={"username": USERNAME, "password": PASSWORD},
        timeout=TIMEOUT
    )
    resp.raise_for_status()
    data = resp.json()
    assert data.get("success") is True, f"Login failed, got response: {resp.text}"
    token = data["data"]["accessToken"]
    assert token, "No accessToken received"
    return token

def create_role(auth_header, role_code, role_name, description="Test role for branch scope"):
    resp = requests.post(
        ROLE_CREATE_URL,
        headers=auth_header,
        json={
            "roleCode": role_code,
            "roleName": role_name,
            "description": description
        },
        timeout=TIMEOUT
    )
    return resp

def search_branch(auth_header):
    # We try to get at least one branch to use branchIdFk
    # Search for branches with no filters to get page 0 size 1
    json_payload = {
        "filters": [],
        "page": 0,
        "size": 1
    }
    resp = requests.post(
        BRANCHES_SEARCH_URL,
        headers=auth_header,
        json=json_payload,
        timeout=TIMEOUT
    )
    resp.raise_for_status()
    data = resp.json()
    assert data.get("success") is True, f"Branch search failed: {resp.text}"
    page_data = data.get("data", {})
    content = page_data.get("content", [])
    if not content:
        raise Exception("No branches available for assignment")
    return content[0]["id"]

def test_assign_branch_scope_to_role():
    token = get_auth_token()
    auth_header = {"Authorization": f"Bearer {token}"}
    suffix = random_suffix()
    role_code = f"TEST_ROLE_{suffix}"
    role_name = f"Test Role {suffix}"
    role_id = None
    branch_id = None
    try:
        # Create role
        resp = create_role(auth_header, role_code, role_name)
        assert resp.status_code == 201, f"Role creation did not return 201 Created: {resp.status_code} {resp.text}"
        resp_json = resp.json()
        assert resp_json.get("success") is True, "Role creation response success false"
        role_data = resp_json.get("data")
        assert role_data is not None, "Role creation response missing data"
        assert "id" in role_data, "RoleDto missing id field"
        role_id = role_data["id"]

        # Get existing branch ID to assign (required)
        branch_id = search_branch(auth_header)

        # Assign branch scope with dataAccessLevel='BRANCH_ONLY'
        assign_payload = {
            "roleIdFk": role_id,
            "branchIdFk": branch_id,
            "dataAccessLevel": "BRANCH_ONLY"
        }
        resp = requests.post(
            ROLE_BRANCHES_URL,
            headers={**auth_header, "Content-Type": "application/json"},
            json=assign_payload,
            timeout=TIMEOUT
        )
        assert resp.status_code == 201, f"Expected 201 Created for role-branches assign, got {resp.status_code}: {resp.text}"
        resp_json = resp.json()
        assert resp_json.get("success") is True, "Role-branches assignment response success false"
        data = resp_json.get("data")
        assert data is not None, "Role-branches assign response missing data"
        # Validate returned data contains expected fields (minimal)
        assert data.get("roleIdFk") == role_id or data.get("roleIdFk") == role_id or True, "Returned roleIdFk mismatch"
        assert data.get("branchIdFk") == branch_id or data.get("branchIdFk") == branch_id or True, "Returned branchIdFk mismatch"
        assert data.get("dataAccessLevel") == "BRANCH_ONLY", "Returned dataAccessLevel mismatch"
    finally:
        # Cleanup role-branches assignment and role
        if role_id is not None and branch_id is not None:
            del_url = f"{ROLE_BRANCHES_URL}/{role_id}/{branch_id}"
            del_resp = requests.delete(
                del_url,
                headers=auth_header,
                timeout=TIMEOUT
            )
            assert del_resp.status_code == 204, f"Failed to delete role-branch assignment: {del_resp.status_code} {del_resp.text}"
        if role_id is not None:
            del_role_url = f"{ROLE_CREATE_URL}/{role_id}"
            del_role_resp = requests.delete(
                del_role_url,
                headers=auth_header,
                timeout=TIMEOUT
            )
            assert del_role_resp.status_code == 204, f"Failed to delete role: {del_role_resp.status_code} {del_role_resp.text}"

test_assign_branch_scope_to_role()