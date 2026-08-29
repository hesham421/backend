import requests
import uuid

BASE_URL = "http://localhost:7272"
AUTH_PATH = "/api/auth/login"
USERS_PATH = "/api/users"
USERS_SEARCH_PATH = "/api/users/search"
USER_ROLES_PATH_TEMPLATE = "/api/users/{userId}/roles"
TIMEOUT = 30

ADMIN_CREDENTIALS = {
    "username": "admin",
    "password": "admin"
}


def test_create_new_user():
    # Login as admin to obtain JWT access token
    login_resp = requests.post(
        BASE_URL + AUTH_PATH,
        json=ADMIN_CREDENTIALS,
        timeout=TIMEOUT
    )
    assert login_resp.status_code == 200, f"Login failed: {login_resp.text}"
    login_data = login_resp.json()
    assert login_data.get("success") is True
    access_token = login_data.get("data", {}).get("accessToken")
    assert isinstance(access_token, str) and len(access_token) > 0

    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json"
    }

    # Create unique username and password
    unique_suffix = uuid.uuid4().hex[:8]
    new_username = f"testuser_{unique_suffix}"
    new_password = f"Passw0rd!{unique_suffix}"

    create_payload = {
        "username": new_username,
        "password": new_password
    }

    user_id = None
    try:
        # POST /api/users to create new user - expect 201 Created
        create_resp = requests.post(
            BASE_URL + USERS_PATH,
            json=create_payload,
            headers=headers,
            timeout=TIMEOUT
        )
        assert create_resp.status_code == 201, f"User creation failed: {create_resp.text}"
        create_data = create_resp.json()
        assert create_data.get("success") is True
        user_data = create_data.get("data")
        assert user_data is not None
        user_id = user_data.get("id") or user_data.get("userId")
        assert user_id is not None, "Created user ID not found in response"
        assert user_data.get("username") == new_username

        # POST /api/users/search with proper filter (not flat JSON) to find the user
        search_payload = {
            "filters": [
                {
                    "field": "username",
                    "operator": "EQUALS",
                    "value": new_username
                }
            ],
            "page": 0,
            "size": 20
        }
        search_resp = requests.post(
            BASE_URL + USERS_SEARCH_PATH,
            json=search_payload,
            headers=headers,
            timeout=TIMEOUT
        )
        assert search_resp.status_code == 200, f"User search failed: {search_resp.text}"
        search_data = search_resp.json()
        assert search_data.get("success") is True
        page_data = search_data.get("data", {})
        content = page_data.get("content", [])
        # The created user should appear in the search results
        assert any(u.get("username") == new_username for u in content), "Created user not found in search results"

        # Assign roles to the user
        # For the test, assign an empty list of roles to verify the endpoint
        roles_payload = {
            "roleNames": []
        }
        put_roles_resp = requests.put(
            BASE_URL + USER_ROLES_PATH_TEMPLATE.format(userId=user_id),
            json=roles_payload,
            headers=headers,
            timeout=TIMEOUT
        )
        assert put_roles_resp.status_code == 200, f"Assigning roles failed: {put_roles_resp.text}"
        put_roles_data = put_roles_resp.json()
        assert put_roles_data.get("success") is True
        updated_user_data = put_roles_data.get("data", {})
        assert updated_user_data.get("id") == user_id
        # roles should be present (empty or list)
        assert "roles" in updated_user_data
    finally:
        # Cleanup: delete the created user if user_id is set
        if user_id:
            del_resp = requests.delete(
                BASE_URL + f"/api/users/{user_id}",
                headers=headers,
                timeout=TIMEOUT
            )
            # Deletion might fail if user has child relations, but we do not want to raise on cleanup failure.
            if del_resp.status_code not in (204, 200):
                print(f"Warning: Failed to delete user {user_id} during cleanup. Status code: {del_resp.status_code}, Response: {del_resp.text}")


test_create_new_user()