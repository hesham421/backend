import requests

BASE_URL = "http://localhost:7272"
AUTH_USER = "admin"
AUTH_PASS = "admin"
TIMEOUT = 30


def get_access_token(username, password):
    login_payload = {"username": username, "password": password}
    resp = requests.post(f"{BASE_URL}/api/auth/login", json=login_payload, timeout=TIMEOUT)
    assert resp.status_code == 200, f"Login failed: {resp.text}"
    data = resp.json()
    access_token = data.get("accessToken")
    assert access_token is not None, "accessToken not returned from login"
    return access_token


def test_put_api_v1_org_legal_entities_id_deactivate_rejects_with_dependent_branches():
    access_token = get_access_token(AUTH_USER, AUTH_PASS)
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {access_token}"
    }

    # Step 1: Create a legal entity
    legal_entity_payload = {
        "nameAr": "Test LegalEntity AR",
        "nameEn": "Test LegalEntity EN",
        "entityTypeId": 1,
        "notes": "Test notes for legal entity"
    }

    legal_entity_id = None
    branch_id = None
    try:
        resp_create_legal_entity = requests.post(
            f"{BASE_URL}/api/v1/org/legal-entities",
            json=legal_entity_payload,
            headers=headers,
            timeout=TIMEOUT,
        )
        assert resp_create_legal_entity.status_code == 200, f"Failed to create legal entity: {resp_create_legal_entity.text}"
        legal_entity_data = resp_create_legal_entity.json()
        legal_entity_id = legal_entity_data.get("id")
        assert legal_entity_id is not None, "Legal entity ID not returned"

        # Step 2: Create a branch under the legal entity (this creates a dependent branch)
        branch_payload = {
            "legalEntityFk": legal_entity_id,
            "nameAr": "Test Branch AR",
            "nameEn": "Test Branch EN",
            "branchTypeId": 1,
            "notes": "Test notes for branch"
        }

        resp_create_branch = requests.post(
            f"{BASE_URL}/api/v1/org/branches",
            json=branch_payload,
            headers=headers,
            timeout=TIMEOUT,
        )
        assert resp_create_branch.status_code == 200, f"Failed to create branch: {resp_create_branch.text}"
        branch_data = resp_create_branch.json()
        branch_id = branch_data.get("id")
        assert branch_id is not None, "Branch ID not returned"

        # Step 3: Attempt to deactivate the legal entity which has dependent branches
        resp_deactivate = requests.put(
            f"{BASE_URL}/api/v1/org/legal-entities/{legal_entity_id}/deactivate",
            headers=headers,
            timeout=TIMEOUT,
        )
        # Expecting error response (likely 409 Conflict or 400 Bad Request) due to business rule enforcement
        assert resp_deactivate.status_code >= 400, "Deactivate succeeded despite dependent branches"
        error_response = resp_deactivate.json()
        assert "error" in error_response or "message" in error_response or "success" in error_response, "Error response details missing"

    finally:
        # Cleanup: Delete branch and legal entity if created
        if branch_id is not None:
            try:
                del_branch_resp = requests.delete(
                    f"{BASE_URL}/api/v1/org/branches/{branch_id}",
                    headers=headers,
                    timeout=TIMEOUT,
                )
                # Deletion may fail if constraints exist; ignore for cleanup
            except Exception:
                pass
        if legal_entity_id is not None:
            try:
                del_legal_entity_resp = requests.delete(
                    f"{BASE_URL}/api/v1/org/legal-entities/{legal_entity_id}",
                    headers=headers,
                    timeout=TIMEOUT,
                )
                # Deletion may fail if related data exists; ignore for cleanup
            except Exception:
                pass


test_put_api_v1_org_legal_entities_id_deactivate_rejects_with_dependent_branches()
