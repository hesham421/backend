import requests
from requests.auth import HTTPBasicAuth

BASE_URL = "http://localhost:7272"
TIMEOUT = 30

def authenticate():
    url = f"{BASE_URL}/api/auth/login"
    payload = {"username": "admin", "password": "admin"}
    resp = requests.post(url, json=payload, timeout=TIMEOUT)
    resp.raise_for_status()
    body = resp.json()
    assert body.get("success") is True
    token = body.get("data", {}).get("accessToken")
    assert token, "Access token not found in login response"
    return token

def create_role(token):
    url = f"{BASE_URL}/api/roles"
    payload = {
        "roleCode": "testRoleCode123",
        "roleName": "Test Role Name",
        "description": "Test role for role-branch assignment",
        "active": True
    }
    headers = {"Authorization": f"Bearer {token}"}
    resp = requests.post(url, json=payload, headers=headers, timeout=TIMEOUT)
    resp.raise_for_status()
    body = resp.json()
    assert body.get("success") is True
    role = body.get("data")
    assert role and "id" in role
    return role["id"]

def delete_role(token, role_id):
    url = f"{BASE_URL}/api/roles/{role_id}"
    headers = {"Authorization": f"Bearer {token}"}
    resp = requests.delete(url, headers=headers, timeout=TIMEOUT)
    # Role might be in use and refuse delete; ignore delete errors silently
    if resp.status_code not in (204, 404):
        resp.raise_for_status()

def create_branch(token, legal_entity_id):
    url = f"{BASE_URL}/api/v1/org/branches"
    # Using fixed branchTypeId as 1 for test; notes arbitrary
    payload = {
        "legalEntityFk": legal_entity_id,
        "nameAr": "Test Branch Ar",
        "nameEn": "Test Branch En",
        "branchTypeId": 1,
        "notes": "Branch created for role-branch assignment test"
    }
    headers = {"Authorization": f"Bearer {token}"}
    resp = requests.post(url, json=payload, headers=headers, timeout=TIMEOUT)
    resp.raise_for_status()
    body = resp.json()
    assert body.get("success") is True
    branch = body.get("data")
    assert branch and "id" in branch
    return branch["id"]

def delete_branch(token, branch_id):
    url = f"{BASE_URL}/api/v1/org/branches/{branch_id}/deactivate"
    headers = {"Authorization": f"Bearer {token}"}
    resp = requests.put(url, headers=headers, timeout=TIMEOUT)
    if resp.status_code != 200:
        resp.raise_for_status()
    # Then try delete if available
    url_delete = f"{BASE_URL}/api/v1/org/branches/{branch_id}"
    del_resp = requests.delete(url_delete, headers=headers, timeout=TIMEOUT)
    if del_resp.status_code not in (204, 404):
        del_resp.raise_for_status()

def create_legal_entity(token):
    url = f"{BASE_URL}/api/v1/org/legal-entities"
    payload = {
        "nameAr": "Test Legal Entity Ar",
        "nameEn": "Test Legal Entity En",
        "entityTypeId": 1,
        "notes": "Legal entity for role-branch assignment test"
    }
    headers = {"Authorization": f"Bearer {token}"}
    resp = requests.post(url, json=payload, headers=headers, timeout=TIMEOUT)
    resp.raise_for_status()
    body = resp.json()
    assert body.get("success") is True
    legal_entity = body.get("data")
    assert legal_entity and "id" in legal_entity
    return legal_entity["id"]

def delete_legal_entity(token, legal_entity_id):
    url = f"{BASE_URL}/api/v1/org/legal-entities/{legal_entity_id}/deactivate"
    headers = {"Authorization": f"Bearer {token}"}
    resp = requests.put(url, headers=headers, timeout=TIMEOUT)
    if resp.status_code != 200:
        resp.raise_for_status()
    url_delete = f"{BASE_URL}/api/v1/org/legal-entities/{legal_entity_id}"
    del_resp = requests.delete(url_delete, headers=headers, timeout=TIMEOUT)
    if del_resp.status_code not in (204, 404):
        del_resp.raise_for_status()

def test_role_branches_assign_and_update():
    token = authenticate()
    headers = {"Authorization": f"Bearer {token}"}
    legal_entity_id = None
    branch_id = None
    role_id = None
    try:
        # Setup legal entity for branch
        legal_entity_id = create_legal_entity(token)
        # Setup branch for role-branch
        branch_id = create_branch(token, legal_entity_id)
        # Setup role for assignment
        role_id = create_role(token)
        # Step 1: POST /api/v1/security/role-branches assign branch data-access scope to role
        url_assign = f"{BASE_URL}/api/v1/security/role-branches"
        assign_payload = {
            "roleIdFk": role_id,
            "branchIdFk": branch_id,
            "dataAccessLevel": "READ"  # Example value; could be READ/WRITE or similar
        }
        resp_assign = requests.post(url_assign, json=assign_payload, headers=headers, timeout=TIMEOUT)
        resp_assign.raise_for_status()
        body_assign = resp_assign.json()
        assert body_assign.get("success") is True
        assigned_data = body_assign.get("data")
        assert assigned_data is not None
        assert assigned_data.get("roleIdFk") == role_id
        assert assigned_data.get("branchIdFk") == branch_id
        assert assigned_data.get("dataAccessLevel") == "READ"
        # Step 2: PUT /api/v1/security/role-branches/{roleId}/{branchId} update the assignment
        url_update = f"{BASE_URL}/api/v1/security/role-branches/{role_id}/{branch_id}"
        update_payload = {
            "roleIdFk": role_id,
            "branchIdFk": branch_id,
            "dataAccessLevel": "WRITE"
        }
        resp_update = requests.put(url_update, json=update_payload, headers=headers, timeout=TIMEOUT)
        resp_update.raise_for_status()
        body_update = resp_update.json()
        assert body_update.get("success") is True
        updated_data = body_update.get("data")
        assert updated_data is not None
        assert updated_data.get("roleIdFk") == role_id
        assert updated_data.get("branchIdFk") == branch_id
        assert updated_data.get("dataAccessLevel") == "WRITE"
    finally:
        # Cleanup created resources
        if role_id:
            delete_role(token, role_id)
        if branch_id:
            try:
                delete_branch(token, branch_id)
            except Exception:
                pass
        if legal_entity_id:
            try:
                delete_legal_entity(token, legal_entity_id)
            except Exception:
                pass

test_role_branches_assign_and_update()