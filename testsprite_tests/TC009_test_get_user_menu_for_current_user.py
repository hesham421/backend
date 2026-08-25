import requests

BASE_URL = "http://localhost:7272"
LOGIN_PATH = "/api/auth/login"
USER_MENU_PATH = "/api/menu/user-menu"

USERNAME = "admin"
PASSWORD = "admin"
TIMEOUT = 30


def test_get_user_menu_for_current_user():
    session = requests.Session()
    try:
        # Step 1: Login to get JWT access token
        login_url = f"{BASE_URL}{LOGIN_PATH}"
        login_payload = {
            "username": USERNAME,
            "password": PASSWORD
        }
        login_response = session.post(login_url, json=login_payload, timeout=TIMEOUT)
        assert login_response.status_code == 200, f"Login failed with status {login_response.status_code}"
        login_json = login_response.json()
        assert login_json.get("success") is True, f"Login API success flag is not True: {login_json}"
        access_token = login_json.get("data", {}).get("accessToken")
        assert access_token and isinstance(access_token, str), "No accessToken in login response"

        # Step 2: Call the user-menu endpoint with Bearer token
        menu_url = f"{BASE_URL}/api/menu/user-menu"
        headers = {
            "Authorization": f"Bearer {access_token}"
        }
        menu_response = session.get(menu_url, headers=headers, timeout=TIMEOUT)
        assert menu_response.status_code == 200, f"User menu request failed with status {menu_response.status_code}"
        menu_json = menu_response.json()
        assert menu_json.get("success") is True, f"User menu API success flag is not True: {menu_json}"
        menu_data = menu_json.get("data")
        assert menu_data is not None, "User menu data is None"
        assert isinstance(menu_data, list), "User menu data is not a list"

        # Additional assertions can be made about hierarchical menu structure.
        # For example, check that each item has expected keys.
        # Required keys for MenuItemDto are assumed from typical menu structures.
        def check_menu_item(item):
            assert isinstance(item, dict), "Menu item is not a dict"
            assert "id" in item or "pageCode" in item, "Menu item missing 'id' or 'pageCode'"
            assert "children" in item or "children" not in item or isinstance(item.get("children", []), list), \
                "'children' is not a list if present"
            # Recursive check for children
            for child in item.get("children", []):
                check_menu_item(child)

        for menu_item in menu_data:
            check_menu_item(menu_item)

    finally:
        session.close()


test_get_user_menu_for_current_user()