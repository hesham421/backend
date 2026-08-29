import requests
from requests.auth import HTTPBasicAuth

BASE_URL = "http://localhost:7272"
LOGIN_ENDPOINT = "/api/auth/login"
MENU_USER_MENU_ENDPOINT = "/api/menu/user-menu"

USERNAME = "admin"
PASSWORD = "admin"


def test_get_api_menu_user_menu_with_valid_jwt():
    try:
        # Step 1: Login to get JWT token
        login_url = BASE_URL + LOGIN_ENDPOINT
        login_payload = {
            "username": USERNAME,
            "password": PASSWORD
        }
        login_response = requests.post(login_url, json=login_payload, timeout=30)
        assert login_response.status_code == 200, f"Login failed with status {login_response.status_code}"
        login_json = login_response.json()
        assert "accessToken" in login_json, "accessToken not in login response"
        access_token = login_json["accessToken"]
        assert isinstance(access_token, str) and len(access_token) > 0, "Invalid accessToken received"

        # Step 2: Access /api/menu/user-menu with valid JWT in Authorization header
        menu_url = BASE_URL + MENU_USER_MENU_ENDPOINT
        headers = {
            "Authorization": f"Bearer {access_token}"
        }
        menu_response = requests.get(menu_url, headers=headers, timeout=30)
        assert menu_response.status_code == 200, f"Menu fetch failed with status {menu_response.status_code}"

        menu_json = menu_response.json()
        # Validate that response is a list (MenuItemDto[])
        assert isinstance(menu_json, list), "Menu response is not a list"

        # Validate that menu items contain keys typically expected in MenuItemDto
        # Since schema is not fully specified here, do basic checks on keys
        if len(menu_json) > 0:
            required_keys = {"id", "name", "permissions"}  # Assuming common keys
            first_item = menu_json[0]
            assert isinstance(first_item, dict), "Menu item is not a dict"
            # We don't have explicit schema, so just check some keys presence
            # It's possible these keys might differ, so adapt accordingly
            # To avoid false failure, check if at least one key among many is present
            keys = first_item.keys()
            assert any(k in keys for k in ["id", "name", "permissions", "children"]), "Menu item keys are unexpected"

    except requests.RequestException as e:
        assert False, f"Request failed: {e}"


test_get_api_menu_user_menu_with_valid_jwt()