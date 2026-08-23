import requests

BASE_URL = "http://localhost:7272/actuator/health"
LOGIN_URL = "http://localhost:7272/api/auth/login"
MENU_URL = "http://localhost:7272/api/menu/user-menu"
TIMEOUT = 30

def test_post_api_auth_login_with_valid_credentials():
    # Step 1: POST /api/auth/login with valid username and password
    login_payload = {
        "username": "admin",
        "password": "admin"
    }
    try:
        login_response = requests.post(LOGIN_URL, json=login_payload, timeout=TIMEOUT)
    except requests.RequestException as e:
        assert False, f"Login request failed: {e}"
    
    assert login_response.status_code == 200, f"Expected status 200, got {login_response.status_code}"
    
    try:
        login_json = login_response.json()
    except ValueError:
        assert False, "Login response is not a valid JSON"
    
    assert "accessToken" in login_json, "Login response missing accessToken"
    assert "expiresIn" in login_json, "Login response missing expiresIn"
    assert isinstance(login_json["accessToken"], str) and login_json["accessToken"], "accessToken is empty or not a string"
    assert isinstance(login_json["expiresIn"], (int, float)), "expiresIn is not a number"
    
    access_token = login_json["accessToken"]
    
    # Step 2: Use accessToken to access a protected endpoint (GET /api/menu/user-menu)
    headers = {
        "Authorization": f"Bearer {access_token}"
    }
    try:
        menu_response = requests.get(MENU_URL, headers=headers, timeout=TIMEOUT)
    except requests.RequestException as e:
        assert False, f"Accessing protected endpoint failed: {e}"
    
    assert menu_response.status_code == 200, f"Expected status 200 for protected endpoint, got {menu_response.status_code}"
    # Optionally check if response JSON is list (MenuItemDto[])
    try:
        menu_json = menu_response.json()
    except ValueError:
        assert False, "Protected endpoint response is not valid JSON"
    assert isinstance(menu_json, list), "Protected endpoint response is not a list as expected for MenuItemDto[]"

test_post_api_auth_login_with_valid_credentials()