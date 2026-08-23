import requests

BASE_URL = "http://localhost:7272"
LOGIN_PATH = "/api/auth/login"
USER_MENU_PATH = "/api/menu/user-menu"
TIMEOUT = 30

def test_post_api_auth_login_with_valid_credentials():
    login_url = BASE_URL + LOGIN_PATH
    user_menu_url = BASE_URL + USER_MENU_PATH
    login_payload = {
        "username": "admin",
        "password": "admin"
    }
    headers = {
        "Content-Type": "application/json"
    }

    # Step 1: POST /api/auth/login with valid credentials
    try:
        response = requests.post(login_url, json=login_payload, headers=headers, timeout=TIMEOUT)
    except requests.RequestException as e:
        assert False, f"Login request failed: {e}"
    assert response.status_code == 200, f"Expected status 200 but got {response.status_code}"
    response_json = response.json()
    assert response_json.get("success") is True, "Login response success flag is not True"
    data = response_json.get("data")
    assert data is not None, "Login response missing 'data'"
    access_token = data.get("accessToken")
    expires_in = data.get("expiresIn")
    assert isinstance(access_token, str) and access_token, "accessToken missing or invalid"
    assert isinstance(expires_in, (int, float)), "expiresIn missing or invalid"

    # Step 2: Use token for authenticated API call GET /api/menu/user-menu
    auth_headers = {
        "Authorization": f"Bearer {access_token}"
    }
    try:
        menu_response = requests.get(user_menu_url, headers=auth_headers, timeout=TIMEOUT)
    except requests.RequestException as e:
        assert False, f"Authenticated API call failed: {e}"
    assert menu_response.status_code == 200, f"Expected status 200 on authenticated call but got {menu_response.status_code}"
    menu_response_json = menu_response.json()
    assert menu_response_json.get("success") is True, "Authenticated call response success flag is not True"
    assert isinstance(menu_response_json.get("data"), list), "Menu data is not a list"

test_post_api_auth_login_with_valid_credentials()