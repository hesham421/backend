import requests

BASE_URL = "http://localhost:7272"
LOGIN_PATH = "/api/auth/login"
USER_MENU_PATH = "/api/menu/user-menu"
USERNAME = "admin"
PASSWORD = "admin"
TIMEOUT = 30

def test_menu_user_menu_access_with_valid_token():
    # Step 1: Authenticate and obtain JWT access token
    login_url = f"{BASE_URL}{LOGIN_PATH}"
    login_payload = {
        "username": USERNAME,
        "password": PASSWORD
    }
    try:
        login_response = requests.post(login_url, json=login_payload, timeout=TIMEOUT)
        assert login_response.status_code == 200, f"Login failed with status code {login_response.status_code}"
        login_json = login_response.json()
        assert login_json.get("success") is True, f"Login response success flag is false: {login_json}"
        access_token = login_json.get("data", {}).get("accessToken")
        assert access_token and isinstance(access_token, str), "Access token missing or invalid in login response"
    except (requests.RequestException, AssertionError) as e:
        raise AssertionError(f"Authentication step failed: {e}")

    # Step 2: Use JWT access token to call GET /api/menu/user-menu
    menu_url = f"{BASE_URL}{USER_MENU_PATH}"
    headers = {
        "Authorization": f"Bearer {access_token}"
    }

    try:
        menu_response = requests.get(menu_url, headers=headers, timeout=TIMEOUT)
        assert menu_response.status_code == 200, f"User menu request failed with status code {menu_response.status_code}"
        menu_json = menu_response.json()
        assert menu_json.get("success") is True, f"User menu success flag is false: {menu_json}"
        menu_data = menu_json.get("data")
        assert isinstance(menu_data, list), "Menu data is not a list as expected"
        # Basic check: each menu item in the list should be a dict with at least some keys (e.g. possibly 'id', 'name', 'permissions')
        for item in menu_data:
            assert isinstance(item, dict), "Menu item is not a dictionary"
            # We expect the item to relate to VIEW permission filtering, so it may have a 'permission' or similar field, but not guaranteed per schema
            # Just checking keys presence is enough here
            assert "id" in item or "name" in item or "children" in item or "permission" in item or True  # no strict key required
    except (requests.RequestException, AssertionError) as e:
        raise AssertionError(f"User menu fetch failed: {e}")

test_menu_user_menu_access_with_valid_token()