import requests
from requests.auth import HTTPBasicAuth

BASE_URL = "http://localhost:7272"
LOGIN_PATH = "/api/auth/login"
MENU_USER_MENU_PATH = "/api/menu/user-menu"

ADMIN_USERNAME = "admin"
ADMIN_PASSWORD = "admin"

def test_get_api_menu_user_menu_for_another_user_as_admin():
    timeout_seconds = 30

    # Step 1: Authenticate as admin to get JWT access token
    login_url = f"{BASE_URL}{LOGIN_PATH}"
    login_payload = {
        "username": ADMIN_USERNAME,
        "password": ADMIN_PASSWORD
    }
    try:
        login_response = requests.post(login_url, json=login_payload, timeout=timeout_seconds)
        assert login_response.status_code == 200, f"Login failed: {login_response.text}"
        login_data = login_response.json()
        access_token = login_data.get("accessToken")
        assert access_token, "No accessToken in login response"

        # Step 2: Get list of users to pick a different userId for testing
        # Since userId is required and not provided, attempt to list users to get an ID different from admin
        users_url = f"{BASE_URL}/api/users"
        headers = {"Authorization": f"Bearer {access_token}"}
        users_response = requests.get(users_url, headers=headers, timeout=timeout_seconds)
        assert users_response.status_code == 200, f"Failed to get users list: {users_response.text}"
        users_data = users_response.json()
        # Assume users_data structure has 'content' list or similar; fallback to empty if not found 
        users_list = users_data.get('content') or users_data.get('items') or []
        if not users_list:
            # If no users list structured, try to parse as list directly
            if isinstance(users_data, list):
                users_list = users_data

        # Find a userId different than admin (assume admin userId not known, exclude self by username)
        user_id_to_test = None
        for user in users_list:
            if isinstance(user, dict):
                if user.get("username") != ADMIN_USERNAME and user.get("id") is not None:
                    user_id_to_test = user.get("id")
                    break
                elif user.get("userId") and user.get("username") != ADMIN_USERNAME:
                    user_id_to_test = user.get("userId")
                    break
                elif user.get("id") and user.get("username", "") != ADMIN_USERNAME:
                    user_id_to_test = user.get("id")
                    break
        if user_id_to_test is None:
            # If no other user found, fallback: try to create a test user and delete after test
            created_user = None
            try:
                create_user_url = f"{BASE_URL}/api/users"
                user_create_payload = {
                    "username": "testuser_for_menu_tc008",
                    "password": "TestPassword1!",
                    "roleNames": ["User"]
                }
                create_response = requests.post(create_user_url, headers=headers, json=user_create_payload, timeout=timeout_seconds)
                assert create_response.status_code == 200, f"User creation failed: {create_response.text}"
                created_user = create_response.json()
                user_id_to_test = created_user.get("id") or created_user.get("userId")
                assert user_id_to_test is not None, "Created user has no ID"
                
                # Proceed with the user_id_to_test set
            finally:
                # Clean up the created user if it was created and test finishes
                def delete_test_user(user_id):
                    delete_url = f"{BASE_URL}/api/users/{user_id}"
                    del_resp = requests.delete(delete_url, headers=headers, timeout=timeout_seconds)
                    # Ignore errors if deletion fails
                # Deletion will be handled after the test request below

        # Step 3: Request the menu tree for the specified userId with admin JWT
        menu_url = f"{BASE_URL}{MENU_USER_MENU_PATH}/{user_id_to_test}"
        menu_response = requests.get(menu_url, headers={"Authorization": f"Bearer {access_token}"}, timeout=timeout_seconds)
        assert menu_response.status_code == 200, f"Failed to get menu for user {user_id_to_test}: {menu_response.text}"
        menu_data = menu_response.json()
        assert isinstance(menu_data, list), "Menu response is not a list"
        # Further checks could include structure validation, but not specified

    finally:
        # Clean up created user if any
        if 'created_user' in locals() and created_user:
            try:
                delete_url = f"{BASE_URL}/api/users/{user_id_to_test}"
                requests.delete(delete_url, headers={"Authorization": f"Bearer {access_token}"}, timeout=timeout_seconds)
            except Exception:
                pass

test_get_api_menu_user_menu_for_another_user_as_admin()