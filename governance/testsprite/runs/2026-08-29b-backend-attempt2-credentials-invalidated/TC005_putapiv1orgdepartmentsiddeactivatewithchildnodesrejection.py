import requests

BASE_URL = "http://localhost:7272"
AUTH_USERNAME = "admin"
AUTH_PASSWORD = "admin"
TIMEOUT = 30


def test_put_api_v1_org_departments_id_deactivate_with_child_nodes_rejection():
    headers = {"Content-Type": "application/json"}

    # 0. Login to get bearer token
    login_payload = {"username": AUTH_USERNAME, "password": AUTH_PASSWORD}
    resp = requests.post(f"{BASE_URL}/api/auth/login", json=login_payload, headers={"Content-Type": "application/json"}, timeout=TIMEOUT)
    assert resp.status_code == 200
    data = resp.json()["data"]
    access_token = data["accessToken"]
    auth_headers = {"Content-Type": "application/json", "Authorization": f"Bearer {access_token}"}

    created_branch = None
    created_parent_department = None
    created_child_department = None

    try:
        # 1. Create a Legal Entity (required for branch)
        legal_entity_payload = {
            "nameAr": "Test Entity AR",
            "nameEn": "Test Entity EN",
            "entityTypeId": 1,
            "notes": "Test legal entity for dept deactivate test"
        }
        resp = requests.post(
            f"{BASE_URL}/api/v1/org/legal-entities",
            json=legal_entity_payload,
            headers=auth_headers,
            timeout=TIMEOUT,
        )
        assert resp.status_code == 200
        legal_entity = resp.json()["data"]
        legal_entity_id = legal_entity["id"]

        # 2. Create a Branch under the legal entity (for department branchFk)
        branch_payload = {
            "legalEntityFk": legal_entity_id,
            "nameAr": "Test Branch AR",
            "nameEn": "Test Branch EN",
            "branchTypeId": 1,
            "notes": "Branch for deactivation test"
        }
        resp = requests.post(
            f"{BASE_URL}/api/v1/org/branches",
            json=branch_payload,
            headers=auth_headers,
            timeout=TIMEOUT,
        )
        assert resp.status_code == 200
        created_branch = resp.json()["data"]
        branch_id = created_branch["id"]

        # 3. Create a parent department in the branch
        parent_dept_payload = {
            "branchFk": branch_id,
            "nameAr": "Parent Dept AR",
            "nameEn": "Parent Dept EN",
            "parentDepartmentFk": None,
            "nodeTypeId": 1,
            "notes": "Parent department for deactivate test with child"
        }
        resp = requests.post(
            f"{BASE_URL}/api/v1/org/departments",
            json=parent_dept_payload,
            headers=auth_headers,
            timeout=TIMEOUT,
        )
        assert resp.status_code == 200
        created_parent_department = resp.json()["data"]
        parent_dept_id = created_parent_department["id"]

        # 4. Create a child department setting parentDepartmentFk to the parent department
        child_dept_payload = {
            "branchFk": branch_id,
            "nameAr": "Child Dept AR",
            "nameEn": "Child Dept EN",
            "parentDepartmentFk": parent_dept_id,
            "nodeTypeId": 1,
            "notes": "Child department to block parent deactivation"
        }
        resp = requests.post(
            f"{BASE_URL}/api/v1/org/departments",
            json=child_dept_payload,
            headers=auth_headers,
            timeout=TIMEOUT,
        )
        assert resp.status_code == 200
        created_child_department = resp.json()["data"]
        child_dept_id = created_child_department["id"]

        # 5. Attempt to deactivate the parent department (should fail with business-rule rejection)
        resp = requests.put(
            f"{BASE_URL}/api/v1/org/departments/{parent_dept_id}/deactivate",
            headers=auth_headers,
            timeout=TIMEOUT,
        )
        # The business-rule rejection should return an error response (could be 409 or another 4xx)
        assert 400 <= resp.status_code < 500

        resp_json = resp.json()
        assert resp_json.get("success") is False
        data = resp_json.get("data")
        assert data is not None

        # Verify error/rejection details presence in data
        # The data object should contain business-rule rejection details; any of these fields should be present
        rejection_fields = ["errorCode", "message", "rejectionReason", "details"]
        assert any(field in data for field in rejection_fields)

        # Optional: Confirm isActive flag is still True in GET after failed deactivation
        get_resp = requests.get(
            f"{BASE_URL}/api/v1/org/departments/{parent_dept_id}",
            headers=auth_headers,
            timeout=TIMEOUT,
        )
        assert get_resp.status_code == 200
        get_data = get_resp.json()["data"]
        assert get_data["isActive"] is True

    finally:
        # Cleanup: Delete child department
        if created_child_department:
            try:
                requests.delete(
                    f"{BASE_URL}/api/v1/org/departments/{created_child_department['id']}",
                    headers=auth_headers,
                    timeout=TIMEOUT,
                )
            except Exception:
                pass

        # Cleanup: Delete parent department
        if created_parent_department:
            try:
                requests.delete(
                    f"{BASE_URL}/api/v1/org/departments/{created_parent_department['id']}",
                    headers=auth_headers,
                    timeout=TIMEOUT,
                )
            except Exception:
                pass

        # Cleanup: Delete branch
        if created_branch:
            try:
                requests.delete(
                    f"{BASE_URL}/api/v1/org/branches/{created_branch['id']}",
                    headers=auth_headers,
                    timeout=TIMEOUT,
                )
            except Exception:
                pass

        # Cleanup: Delete legal entity
        if 'legal_entity_id' in locals():
            try:
                requests.delete(
                    f"{BASE_URL}/api/v1/org/legal-entities/{legal_entity_id}",
                    headers=auth_headers,
                    timeout=TIMEOUT,
                )
            except Exception:
                pass


test_put_api_v1_org_departments_id_deactivate_with_child_nodes_rejection()
