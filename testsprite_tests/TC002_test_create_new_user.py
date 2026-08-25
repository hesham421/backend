import requests
from requests.auth import HTTPBasicAuth
import uuid

BASE_URL = "http://localhost:7272"
AUTH_LOGIN_ENDPOINT = "/api/auth/login"
USER_CREATE_ENDPOINT = "/api/users"
USER_ROLES_ASSIGN_ENDPOINT = "/api/users/{userId}/roles"
USER_ROLES_GET_ENDPOINT = "/api/users/{userId}/roles"
USER_DELETE_ENDPOINT = "/api/users/{userId}"

ADMIN_CREDENTIALS = {
    "username": "admin",
    "password": "admin"
}

TIMEOUT = 30


def test_create_new_user():
    # Step 1: Login as admin to obtain JWT access token
    login_url = BASE_URL + AUTH_LOGIN_ENDPOINT
    try:
        login_response = requests.post(
            login_url,
            json={"username": ADMIN_CREDENTIALS["username"], "password": ADMIN_CREDENTIALS["password"]},
            timeout=TIMEOUT
        )
        assert login_response.status_code == 200, f"Expected 200 OK on login, got {login_response.status_code}"
        login_resp_json = login_response.json()
        assert login_resp_json.get("success") is True, "Login response success flag is not True"
        access_token = login_resp_json["data"].get("accessToken")
        assert access_token, "Access token missing in login response"
    except Exception as e:
        assert False, f"Admin login failed: {e}"

    # Prepare auth headers for subsequent requests
    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json"
    }

    # Step 2: Create a new user with valid username and password
    # Generate unique username to avoid duplicates
    unique_username = f"testuser_{uuid.uuid4().hex[:8]}"
    user_password = "TestPassword123!"

    create_user_url = BASE_URL + USER_CREATE_ENDPOINT
    user_payload = {
        "username": unique_username,
        "password": user_password
    }

    user_id = None

    try:
        create_response = requests.post(
            create_user_url,
            json=user_payload,
            headers=headers,
            timeout=TIMEOUT
        )
        assert create_response.status_code == 200, f"Expected 200 OK on user creation, got {create_response.status_code}"
        create_json = create_response.json()
        assert create_json.get("success") is True, "User creation response success flag is not True"
        user_data = create_json.get("data")
        assert user_data is not None, "User data missing in creation response"

        user_id = user_data.get("userId")
        assert user_id is not None, "User ID missing in user creation response data"
        assert user_data.get("username") == unique_username, "Returned username does not match the created one"

        # Step 3: Retrieve the roles of the created user (should be empty or default)
        get_roles_url = BASE_URL + USER_ROLES_GET_ENDPOINT.format(userId=user_id)
        get_roles_response = requests.get(
            get_roles_url,
            headers=headers,
            timeout=TIMEOUT
        )
        assert get_roles_response.status_code == 200, f"Expected 200 OK on getting user roles, got {get_roles_response.status_code}"
        roles_json = get_roles_response.json()
        assert roles_json.get("success") is True, "Get user roles response success flag is not True"
        roles_data = roles_json.get("data")
        assert isinstance(roles_data, list), "User roles data is not a list"

        # Step 4: Assign roles to the user and validate the updated roles
        # Assign an example role: "USER" (assuming this role exists)
        assign_roles_url = BASE_URL + USER_ROLES_ASSIGN_ENDPOINT.format(userId=user_id)
        roles_to_assign = ["USER"]
        assign_roles_response = requests.put(
            assign_roles_url,
            headers=headers,
            json={"roleNames": roles_to_assign},
            timeout=TIMEOUT
        )
        assert assign_roles_response.status_code == 200, f"Expected 200 OK on assigning roles, got {assign_roles_response.status_code}"
        assign_roles_json = assign_roles_response.json()
        assert assign_roles_json.get("success") is True, "Assign roles response success flag is not True"
        updated_user_data = assign_roles_json.get("data")
        assert updated_user_data is not None, "Updated user data missing after role assignment"
        assigned_roles = updated_user_data.get("roles")
        assert assigned_roles is not None, "Assigned roles missing in response"
        for role in roles_to_assign:
            assert role in assigned_roles, f"Role '{role}' missing in assigned roles"

        # Verify roles again by fetching
        get_roles_after_assign_response = requests.get(
            get_roles_url,
            headers=headers,
            timeout=TIMEOUT
        )
        assert get_roles_after_assign_response.status_code == 200, f"Expected 200 OK on getting user roles after assign, got {get_roles_after_assign_response.status_code}"
        roles_after_assign_json = get_roles_after_assign_response.json()
        assert roles_after_assign_json.get("success") is True, "Get user roles after assign response success flag is not True"
        roles_after_assign_data = roles_after_assign_json.get("data")
        assert set(roles_to_assign).issubset(set(roles_after_assign_data)), "Assigned roles not found in user roles after assignment"

    finally:
        # Cleanup: Delete the created user to avoid test side-effects
        if user_id is not None:
            delete_user_url = BASE_URL + USER_DELETE_ENDPOINT.format(userId=user_id)
            try:
                delete_response = requests.delete(
                    delete_user_url,
                    headers=headers,
                    timeout=TIMEOUT
                )
                # Could be 204 No Content or error if cascading constraints; log but don't assert here
                if delete_response.status_code not in (204, 200):
                    print(f"Warning: Failed to delete test user {user_id}, status {delete_response.status_code}")
            except Exception as ex:
                print(f"Warning: Exception occurred trying to delete test user {user_id}: {ex}")

test_create_new_user()
