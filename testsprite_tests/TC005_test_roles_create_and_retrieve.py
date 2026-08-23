import requests

BASE_URL = "http://localhost:7272"
AUTH_USERNAME = "admin"
AUTH_PASSWORD = "admin"
TIMEOUT = 30

def get_access_token(username, password):
    login_payload = {"username": username, "password": password}
    response = requests.post(
        f"{BASE_URL}/api/auth/login",
        json=login_payload,
        headers={"Content-Type": "application/json", "Accept": "application/json"},
        timeout=TIMEOUT
    )
    assert response.status_code == 200, f"Login failed with status {response.status_code}"
    resp_json = response.json()
    assert resp_json.get("success") is True, "Login response success flag is not True"
    data = resp_json.get("data")
    assert data is not None, "Login response missing data"
    token = data.get("accessToken")
    assert token is not None, "accessToken missing in login response"
    return token

def test_roles_create_and_retrieve():
    token = get_access_token(AUTH_USERNAME, AUTH_PASSWORD)
    headers = {
        "Content-Type": "application/json",
        "Accept": "application/json",
        "Authorization": f"Bearer {token}"
    }

    role_payload = {
        "roleCode": "testrole123",
        "roleName": "Test Role 123",
        "description": "Role created for test_roles_create_and_retrieve",
        "active": True
    }
    role_id = None

    try:
        # Create Role - POST /api/roles
        create_response = requests.post(
            f"{BASE_URL}/api/roles",
            json=role_payload,
            headers=headers,
            timeout=TIMEOUT
        )
        assert create_response.status_code == 201, f"Expected 201, got {create_response.status_code}"
        create_response_json = create_response.json()
        assert create_response_json.get("success") is True, "Create role response success flag is not True"
        role_data = create_response_json.get("data")
        assert role_data is not None, "Create role response missing data"
        # The PRD shows roleCode is string, and GET uses roleId - typically an id field is included.
        # Attempt to get an 'id' or 'roleId' field first; fallback to roleCode if that's the identifier
        if "id" in role_data:
            role_id = role_data["id"]
        elif "roleId" in role_data:
            role_id = role_data["roleId"]
        else:
            role_id = role_data.get("roleCode")
        assert role_id is not None, "Role ID not found in creation response"

        # Retrieve Role - GET /api/roles/{roleId}
        retrieve_response = requests.get(
            f"{BASE_URL}/api/roles/{role_id}",
            headers=headers,
            timeout=TIMEOUT
        )
        assert retrieve_response.status_code == 200, f"Expected 200, got {retrieve_response.status_code}"
        retrieve_response_json = retrieve_response.json()
        assert retrieve_response_json.get("success") is True, "Retrieve role response success flag is not True"
        retrieved_role_data = retrieve_response_json.get("data")
        assert retrieved_role_data is not None, "Retrieve role response missing data"
        # Validate retrieved role fields match created role fields
        assert retrieved_role_data.get("roleCode") == role_payload["roleCode"], "roleCode mismatch"
        assert retrieved_role_data.get("roleName") == role_payload["roleName"], "roleName mismatch"
        assert retrieved_role_data.get("description") == role_payload["description"], "description mismatch"
        assert retrieved_role_data.get("active") == role_payload["active"], "active flag mismatch"

    finally:
        # Clean up: Delete the created role if role_id is set
        if role_id is not None:
            delete_response = requests.delete(
                f"{BASE_URL}/api/roles/{role_id}",
                headers=headers,
                timeout=TIMEOUT
            )
            # Accept 204 No Content on successful deletion or 409 if role in use (ignore on cleanup)
            if delete_response.status_code not in (204, 404, 409):
                raise AssertionError(f"Failed to delete role during cleanup, status code: {delete_response.status_code}")


test_roles_create_and_retrieve()
