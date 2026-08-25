import requests
from requests.auth import HTTPBasicAuth

BASE_URL = "http://localhost:7272"
LOGIN_PATH = "/api/auth/login"
ROLE_CREATE_PATH = "/api/roles"
USERNAME = "admin"
PASSWORD = "admin"
TIMEOUT = 30


def test_create_role_with_valid_data():
    # Authenticate and get JWT access token
    auth_response = requests.post(
        f"{BASE_URL}{LOGIN_PATH}",
        json={"username": USERNAME, "password": PASSWORD},
        timeout=TIMEOUT,
    )
    assert auth_response.status_code == 200, f"Login failed: {auth_response.text}"
    auth_json = auth_response.json()
    assert auth_json.get("success") is True, f"Login success false: {auth_json}"
    access_token = auth_json.get("data", {}).get("accessToken")
    assert access_token, "No accessToken returned in login response"

    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json",
    }

    role_payload = {
        "roleCode": "TEST_ROLE_CODE_123",
        "roleName": "Test Role Name",
        "description": "Test role description"
    }

    role_id = None
    try:
        create_role_response = requests.post(
            f"{BASE_URL}{ROLE_CREATE_PATH}",
            json=role_payload,
            headers=headers,
            timeout=TIMEOUT,
        )
        assert create_role_response.status_code == 200, f"Role creation failed: {create_role_response.text}"
        create_role_json = create_role_response.json()
        assert create_role_json.get("success") is True, f"Role creation success false: {create_role_json}"
        data = create_role_json.get("data")
        assert data is not None, "No data in role creation response"
        assert data.get("roleCode") == role_payload["roleCode"], "roleCode mismatch"
        assert data.get("roleName") == role_payload["roleName"], "roleName mismatch"
        assert data.get("description") == role_payload["description"], "description mismatch"

        role_id = data.get("id") or data.get("roleId")  # Just in case different naming
        assert role_id is not None, "No role ID returned in create role response"

    finally:
        # Cleanup: delete the created role if role_id is set
        if role_id:
            # Deleting role endpoint: DELETE /api/roles/{roleId}
            # Note from PRD: DELETE returns 204 No Content on success
            delete_resp = requests.delete(
                f"{BASE_URL}{ROLE_CREATE_PATH}/{role_id}",
                headers=headers,
                timeout=TIMEOUT,
            )
            # We allow 204 or 409 (conflict if role assigned to users)
            if delete_resp.status_code == 204:
                pass  # Deleted successfully
            elif delete_resp.status_code == 409:
                # Role still assigned to users - cannot delete - acceptable cleanup failure
                pass
            else:
                # Unexpected failure on delete - raise error
                delete_resp.raise_for_status()


test_create_role_with_valid_data()