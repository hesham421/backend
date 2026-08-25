import requests

BASE_URL = "http://localhost:7272"
LOGIN_PATH = "/api/auth/login"
PROTECTED_USERS_PATH = "/api/users"
TIMEOUT = 30

def test_user_login_with_valid_credentials():
    login_url = BASE_URL + LOGIN_PATH
    protected_url = BASE_URL + PROTECTED_USERS_PATH

    login_payload = {
        "username": "admin",
        "password": "admin"
    }
    headers = {
        "Content-Type": "application/json"
    }

    # Step 1: POST /api/auth/login with valid credentials
    try:
        login_response = requests.post(login_url, json=login_payload, headers=headers, timeout=TIMEOUT)
    except requests.RequestException as e:
        assert False, f"Login request failed: {e}"

    assert login_response.status_code == 200, f"Expected 200 OK from login but got {login_response.status_code}"

    login_json = login_response.json()
    assert "success" in login_json and login_json["success"] is True, "Login response success flag is not True"
    assert "data" in login_json and isinstance(login_json["data"], dict), "Login response missing data object"
    data = login_json["data"]
    assert "accessToken" in data and isinstance(data["accessToken"], str) and data["accessToken"], "accessToken missing or empty"
    assert "expiresIn" in data and isinstance(data["expiresIn"], (int, float)), "expiresIn missing or not a number"

    access_token = data["accessToken"]

    # Step 2: Use accessToken to access a protected endpoint GET /api/users
    auth_headers = {
        "Authorization": f"Bearer {access_token}"
    }
    try:
        protected_response = requests.get(protected_url, headers=auth_headers, timeout=TIMEOUT)
    except requests.RequestException as e:
        assert False, f"Protected endpoint request failed: {e}"

    assert protected_response.status_code == 200, f"Expected 200 OK from protected endpoint but got {protected_response.status_code}"

    protected_json = protected_response.json()
    assert "success" in protected_json and protected_json["success"] is True, "Protected endpoint response success flag is not True"
    assert "data" in protected_json, "Protected endpoint response missing data"

test_user_login_with_valid_credentials()