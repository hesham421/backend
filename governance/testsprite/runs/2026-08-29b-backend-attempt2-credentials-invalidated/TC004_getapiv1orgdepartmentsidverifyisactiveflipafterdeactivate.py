import requests
from requests.auth import HTTPBasicAuth
import uuid
import time

BASE_URL = "http://localhost:7272"
LOGIN_URL = f"{BASE_URL}/api/auth/login"
DEPARTMENTS_URL = f"{BASE_URL}/api/v1/org/departments"

USERNAME = "admin"
PASSWORD = "admin"
TIMEOUT = 30

def authenticate():
    resp = requests.post(
        LOGIN_URL,
        json={"username": USERNAME, "password": PASSWORD},
        timeout=TIMEOUT
    )
    resp.raise_for_status()
    data = resp.json()
    assert data["success"] is True
    token = data["data"]["accessToken"]
    return token

def create_branch(auth_headers):
    # We need a branch for department creation: create legal entity -> create branch
    # To create branch, we need legal entity.
    legal_entity_payload = {
        "nameAr": "Test Legal Entity " + str(uuid.uuid4()),
        "nameEn": "Test Legal Entity EN " + str(uuid.uuid4()),
        "entityTypeId": 1,
        "notes": "Created for test"
    }
    legal_entity_resp = requests.post(
        f"{BASE_URL}/api/v1/org/legal-entities",
        headers=auth_headers,
        json=legal_entity_payload,
        timeout=TIMEOUT
    )
    legal_entity_resp.raise_for_status()
    legal_entity_data = legal_entity_resp.json()
    assert legal_entity_data["success"] is True
    legal_entity = legal_entity_data["data"]
    legal_entity_id = legal_entity["id"]

    branch_payload = {
        "legalEntityFk": legal_entity_id,
        "nameAr": "Test Branch AR " + str(uuid.uuid4()),
        "nameEn": "Test Branch EN " + str(uuid.uuid4()),
        "branchTypeId": 1,
        "notes": "Created for department test"
    }
    branch_resp = requests.post(
        f"{BASE_URL}/api/v1/org/branches",
        headers=auth_headers,
        json=branch_payload,
        timeout=TIMEOUT
    )
    branch_resp.raise_for_status()
    branch_data = branch_resp.json()
    assert branch_data["success"] is True
    branch = branch_data["data"]
    branch_id = branch["id"]

    return legal_entity_id, branch_id

def create_department(auth_headers, branch_id):
    dept_payload = {
        "branchFk": branch_id,
        "nameAr": "Test Department AR " + str(uuid.uuid4()),
        "nameEn": "Test Department EN " + str(uuid.uuid4()),
        "parentDepartmentFk": None,
        "nodeTypeId": 1,
        "notes": "Dept for isActive test",
        "isActive": True
    }
    resp = requests.post(
        DEPARTMENTS_URL,
        headers=auth_headers,
        json=dept_payload,
        timeout=TIMEOUT
    )
    resp.raise_for_status()
    data = resp.json()
    assert data["success"] is True
    department = data["data"]
    return department["id"], department

def get_department(auth_headers, department_id):
    resp = requests.get(
        f"{DEPARTMENTS_URL}/{department_id}",
        headers=auth_headers,
        timeout=TIMEOUT
    )
    resp.raise_for_status()
    data = resp.json()
    assert data["success"] is True
    return data["data"]

def search_departments(auth_headers, branch_id, is_active):
    search_payload = {
        "branchFk": branch_id,
        "isActive": is_active,
        "page": 0,
        "size": 10
    }
    resp = requests.post(
        f"{DEPARTMENTS_URL}/search",
        headers=auth_headers,
        json=search_payload,
        timeout=TIMEOUT
    )
    resp.raise_for_status()
    data = resp.json()
    assert data["success"] is True
    return data["data"]["content"] if "content" in data["data"] else data["data"]

def deactivate_department(auth_headers, department_id):
    resp = requests.put(
        f"{DEPARTMENTS_URL}/{department_id}/deactivate",
        headers=auth_headers,
        timeout=TIMEOUT
    )
    resp.raise_for_status()
    data = resp.json()
    assert data["success"] is True
    return data["data"]

def delete_department(auth_headers, department_id):
    resp = requests.delete(
        f"{DEPARTMENTS_URL}/{department_id}",
        headers=auth_headers,
        timeout=TIMEOUT
    )
    # The delete endpoint is not documented explicitly; assuming 204 No Content or 200 with success true
    if resp.status_code == 204:
        return True
    elif resp.status_code == 200:
        data = resp.json()
        return data.get("success", False)
    return False

def delete_branch(auth_headers, branch_id):
    resp = requests.delete(
        f"{BASE_URL}/api/v1/org/branches/{branch_id}",
        headers=auth_headers,
        timeout=TIMEOUT
    )
    # Delete branch may fail if departments still exist, but we will call after dept deletion
    if resp.status_code == 204:
        return True
    elif resp.status_code == 200:
        data = resp.json()
        return data.get("success", False)
    return False

def delete_legal_entity(auth_headers, legal_entity_id):
    resp = requests.delete(
        f"{BASE_URL}/api/v1/org/legal-entities/{legal_entity_id}",
        headers=auth_headers,
        timeout=TIMEOUT
    )
    if resp.status_code == 204:
        return True
    elif resp.status_code == 200:
        data = resp.json()
        return data.get("success", False)
    return False

def test_deactivate_department_isActive_flip():
    token = authenticate()
    auth_headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }

    # Create required branch and legal entity
    legal_entity_id, branch_id = create_branch(auth_headers)

    department_id = None
    try:
        # Create department
        department_id, department = create_department(auth_headers, branch_id)
        assert department["isActive"] is True

        # Deactivate department
        deactivated_dept = deactivate_department(auth_headers, department_id)
        # Verify response flips isActive to False
        assert deactivated_dept["isActive"] is False
        # Verify id matches
        assert deactivated_dept["id"] == department_id

        # Verify persisted state by GET
        dept_after = get_department(auth_headers, department_id)
        assert dept_after["isActive"] is False
        assert dept_after["id"] == department_id

        # Verify reflected in search results with isActive false filter
        search_results = search_departments(auth_headers, branch_id, is_active=False)
        # The deactivated dept should be in the search results
        found = any(d["id"] == department_id and d["isActive"] is False for d in search_results)
        assert found, "Deactivated department not found in search with isActive=false"

        # Verify NOT in the search results with isActive true filter
        search_active = search_departments(auth_headers, branch_id, is_active=True)
        not_found = all(d["id"] != department_id for d in search_active)
        assert not_found, "Deactivated department found in search with isActive=true"

    finally:
        # Cleanup: delete department, branch, legal entity if possible
        if department_id:
            try:
                delete_department(auth_headers, department_id)
            except Exception:
                pass
        if branch_id:
            try:
                delete_branch(auth_headers, branch_id)
            except Exception:
                pass
        if legal_entity_id:
            try:
                delete_legal_entity(auth_headers, legal_entity_id)
            except Exception:
                pass

test_deactivate_department_isActive_flip()