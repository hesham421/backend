import requests
from requests.auth import HTTPBasicAuth
import uuid
import time

BASE_URL = "http://localhost:7272"
AUTH_USERNAME = "admin"
AUTH_PASSWORD = "admin"
TIMEOUT = 30

def test_users_create_and_role_assignment():
    # Authenticate with basic auth to get JWT token from /api/auth/login-token
    login_url = f"{BASE_URL}/api/auth/login-token"
    auth_payload = {
        "username": AUTH_USERNAME,
        "password": AUTH_PASSWORD
    }
    auth_response = requests.post(login_url, json=auth_payload, timeout=TIMEOUT)
    assert auth_response.status_code == 200, f"Login failed: {auth_response.text}"
    auth_json = auth_response.json()
    assert auth_json.get("success") is True, f"Login unsuccessful: {auth_json}"
    access_token = auth_json.get("data", {}).get("accessToken")
    assert access_token, "Access token missing in login response"

    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json"
    }

    # Create a new user with roles
    unique_username = f"testuser_{uuid.uuid4().hex[:8]}"
    initial_roles = ["USER", "TEST_ROLE"]
    create_user_payload = {
        "username": unique_username,
        "password": "TestPass123!",
        "roleNames": initial_roles
    }

    created_user_id = None
    try:
        create_user_url = f"{BASE_URL}/api/users"
        create_response = requests.post(create_user_url, json=create_user_payload, headers=headers, timeout=TIMEOUT)
        assert create_response.status_code == 201, f"User creation failed: {create_response.text}"
        create_json = create_response.json()
        assert create_json.get("success") is True, f"User creation unsuccessful: {create_json}"
        user_data = create_json.get("data")
        created_user_id = user_data.get("id")
        assert created_user_id is not None, "Created user ID missing"
        # Verify assigned roles via GET /api/users/{userId}/roles
        get_roles_url = f"{BASE_URL}/api/users/{created_user_id}/roles"
        roles_response = requests.get(get_roles_url, headers=headers, timeout=TIMEOUT)
        assert roles_response.status_code == 200, f"Get user roles failed: {roles_response.text}"
        roles_json = roles_response.json()
        assert roles_json.get("success") is True, f"Get user roles unsuccessful: {roles_json}"
        assigned_roles = roles_json.get("data", [])
        # Roles returned should be at least those assigned initially (order and extras should be acceptable)
        for role in initial_roles:
            assert role in assigned_roles, f"Initial role '{role}' missing from assigned roles {assigned_roles}"

        # Now replace user's roles fully with a new set via PUT /api/users/{userId}/roles
        new_roles = ["ADMIN", "USER"]
        update_roles_payload = {
            "roleNames": new_roles
        }
        update_roles_url = f"{BASE_URL}/api/users/{created_user_id}/roles"
        update_response = requests.put(update_roles_url, json=update_roles_payload, headers=headers, timeout=TIMEOUT)
        assert update_response.status_code == 200, f"Update user roles failed: {update_response.text}"
        update_json = update_response.json()
        assert update_json.get("success") is True, f"Update user roles unsuccessful: {update_json}"
        updated_data = update_json.get("data")
        assert updated_data is not None, "Updated user data missing"

        # Verify roles replaced correctly by GET /api/users/{userId}/roles
        roles_response_after_update = requests.get(get_roles_url, headers=headers, timeout=TIMEOUT)
        assert roles_response_after_update.status_code == 200, f"Get user roles after update failed: {roles_response_after_update.text}"
        roles_json_after_update = roles_response_after_update.json()
        assert roles_json_after_update.get("success") is True, f"Get user roles after update unsuccessful: {roles_json_after_update}"
        assigned_roles_after_update = roles_json_after_update.get("data", [])
        # The roles assigned must match exactly the new set (order may vary)
        assert set(assigned_roles_after_update) == set(new_roles), f"User roles after update do not match. Expected: {new_roles}, Got: {assigned_roles_after_update}"

    finally:
        # Cleanup: delete created user if exists
        if created_user_id is not None:
            delete_url = f"{BASE_URL}/api/users/{created_user_id}"
            del_response = requests.delete(delete_url, headers=headers, timeout=TIMEOUT)
            # Deletion might fail if user has child relations, but we ignore here as this is cleanup
            if del_response.status_code not in (204, 404):
                raise AssertionError(f"User deletion failed with status {del_response.status_code}: {del_response.text}")

test_users_create_and_role_assignment()