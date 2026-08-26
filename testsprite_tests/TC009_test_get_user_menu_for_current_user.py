import requests

BASE_URL = "http://localhost:7272"
LOGIN_PATH = "/api/auth/login"
USER_MENU_PATH = "/api/menu/user-menu"
TIMEOUT = 30

def test_get_user_menu_for_current_user():
    # Login with basic token credentials to get JWT access token
    login_url = BASE_URL + LOGIN_PATH
    login_payload = {
        "username": "admin",
        "password": "admin"
    }
    headers = {
        "Content-Type": "application/json"
    }
    try:
        login_response = requests.post(login_url, json=login_payload, headers=headers, timeout=TIMEOUT)
        assert login_response.status_code == 200, f"Login failed with status code {login_response.status_code}"
        login_json = login_response.json()
        assert login_json.get("success") is True, f"Login response success false or missing: {login_json}"
        access_token = login_json.get("data", {}).get("accessToken")
        assert access_token, "Access token missing in login response"

        # Use JWT bearer token to access user menu endpoint
        user_menu_url = BASE_URL + USER_MENU_PATH
        auth_headers = {
            "Authorization": f"Bearer {access_token}"
        }
        user_menu_response = requests.get(user_menu_url, headers=auth_headers, timeout=TIMEOUT)
        assert user_menu_response.status_code == 200, f"User menu request failed with status code {user_menu_response.status_code}"

        user_menu_json = user_menu_response.json()
        assert user_menu_json.get("success") is True, f"User menu success false or missing: {user_menu_json}"
        menu_data = user_menu_json.get("data")
        assert isinstance(menu_data, list), "Menu data is not a list"

        # Validate hierarchical menu structure filtered by VIEW permissions for current user
        # We check that each item has expected structure keys: id, name or similar, children list (optional)
        # Since menu schema is not explicitly detailed, we verify presence of list and nested dicts
        def validate_menu_hierarchy(items):
            for item in items:
                assert isinstance(item, dict), "Menu item is not a dict"
                # check common menu fields, assuming id or code and children or submenus possible
                assert "id" in item or "pageCode" in item or "name" in item or "label" in item, "Menu item missing identifying key"
                # children can be empty or missing for leaf nodes
                children = item.get("children") or item.get("subMenus") or item.get("items")
                if children is not None:
                    assert isinstance(children, list), "Menu children is not a list"
                    validate_menu_hierarchy(children)
        
        validate_menu_hierarchy(menu_data)
    except requests.RequestException as e:
        assert False, f"HTTP request failed: {e}"
    except ValueError as e:
        assert False, f"Failed to parse JSON response: {e}"

test_get_user_menu_for_current_user()