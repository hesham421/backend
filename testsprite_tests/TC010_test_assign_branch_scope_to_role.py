import requests

BASE_URL = "http://localhost:7272"
LOGIN_ENDPOINT = "/api/auth/login"
ROLE_CREATE_ENDPOINT = "/api/roles"
BRANCH_CREATE_ENDPOINT = "/api/v1/org/branches"
ROLE_BRANCH_ASSIGN_ENDPOINT = "/api/v1/security/role-branches"
TIMEOUT = 30

def test_assign_branch_scope_to_role():
    try:
        # Step 1: Login to obtain JWT bearer token
        login_payload = {
            "username": "admin",
            "password": "admin"
        }
        login_resp = requests.post(
            f"{BASE_URL}{LOGIN_ENDPOINT}",
            json=login_payload,
            timeout=TIMEOUT
        )
        assert login_resp.status_code == 200, f"Login failed with status {login_resp.status_code}"
        login_json = login_resp.json()
        assert login_json.get("success") is True, "Login response success flag false"
        access_token = login_json.get("data", {}).get("accessToken")
        assert access_token, "No accessToken in login response"

        headers = {
            "Authorization": f"Bearer {access_token}",
            "Content-Type": "application/json"
        }

        # Step 2: Create a new role for testing
        role_payload = {
            "roleCode": "TEST_ROLE_CODE_TC010",
            "roleName": "Test Role TC010",
            "description": "Role created for test_assign_branch_scope_to_role"
        }
        role_resp = requests.post(
            f"{BASE_URL}{ROLE_CREATE_ENDPOINT}",
            headers=headers,
            json=role_payload,
            timeout=TIMEOUT
        )
        assert role_resp.status_code in (200, 201), f"Create role failed with status {role_resp.status_code}"
        role_json = role_resp.json()
        assert role_json.get("success") is True, "Create role response success flag false"
        role_data = role_json.get("data")
        assert role_data, "No data in create role response"
        role_id = role_data.get("id")
        assert role_id is not None, "Role ID not found in create role response"

        # Step 3: Create a branch for testing
        branch_payload = {
            "legalEntityFk": 1,
            "nameAr": "فرع اختبار TC010",
            "nameEn": "Test Branch TC010",
            "branchTypeId": 1,
            "notes": "Branch created for test_assign_branch_scope_to_role"
        }
        branch_resp = requests.post(
            f"{BASE_URL}{BRANCH_CREATE_ENDPOINT}",
            headers=headers,
            json=branch_payload,
            timeout=TIMEOUT
        )
        assert branch_resp.status_code == 200, f"Create branch failed with status {branch_resp.status_code}"
        branch_json = branch_resp.json()
        assert branch_json.get("success") is True, "Create branch response success flag false"
        branch_data = branch_json.get("data")
        assert branch_data, "No data in create branch response"
        branch_id = branch_data.get("id")
        assert branch_id is not None, "Branch ID not found in create branch response"

        # Step 4: Assign branch scope to role with valid dataAccessLevel
        assign_payload = {
            "roleIdFk": role_id,
            "branchIdFk": branch_id,
            "dataAccessLevel": "BRANCH"
        }
        assign_resp = requests.post(
            f"{BASE_URL}{ROLE_BRANCH_ASSIGN_ENDPOINT}",
            headers=headers,
            json=assign_payload,
            timeout=TIMEOUT
        )
        assert assign_resp.status_code == 200, f"Assign role-branch failed with status {assign_resp.status_code}"
        assign_json = assign_resp.json()
        assert assign_json.get("success") is True, "Assign role-branch response success flag false"
        data = assign_json.get("data")
        assert data is not None, "No data in assign role-branch response"
        assert data.get("roleIdFk") == role_id, "Mismatch in roleIdFk in response"
        assert data.get("branchIdFk") == branch_id, "Mismatch in branchIdFk in response"
        assert data.get("dataAccessLevel") == "BRANCH", "Mismatch in dataAccessLevel in response"

    finally:
        # Cleanup
        try:
            if 'role_id' in locals() and 'branch_id' in locals():
                del_resp = requests.delete(
                    f"{BASE_URL}{ROLE_BRANCH_ASSIGN_ENDPOINT}/{role_id}/{branch_id}",
                    headers=headers,
                    timeout=TIMEOUT
                )
                assert del_resp.status_code in (204, 200), f"Failed to delete role-branch assignment, status: {del_resp.status_code}"
        except Exception:
            pass

        # No DELETE endpoint for branch per PRD, so skip branch delete cleanup

        try:
            if 'role_id' in locals():
                requests.delete(
                    f"{BASE_URL}/api/roles/{role_id}",
                    headers=headers,
                    timeout=TIMEOUT
                )
        except Exception:
            pass

test_assign_branch_scope_to_role()
