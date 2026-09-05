import requests
from requests.auth import HTTPBasicAuth

BASE_URL = "http://localhost:7272"
LOGIN_ENDPOINT = "/api/v1/security/auth/login"
TIMEOUT = 30

def test_post_api_v1_security_auth_login_with_valid_and_invalid_credentials():
    url = BASE_URL + LOGIN_ENDPOINT
    headers = {
        "Content-Type": "application/json"
    }

    # Valid credentials
    valid_payload = {
        "username": "admin",
        "password": "admin"
    }
    response = requests.post(url, json=valid_payload, headers=headers, timeout=TIMEOUT)
    assert response.status_code == 200, f"Expected 200 OK with valid credentials, got {response.status_code}"
    json_data = response.json()["data"]
    assert "accessToken" in json_data and isinstance(json_data["accessToken"], str) and json_data["accessToken"]
    assert "refreshToken" in json_data and isinstance(json_data["refreshToken"], str) and json_data["refreshToken"]
    assert "expiresIn" in json_data and isinstance(json_data["expiresIn"], int)

    # Missing username (only password)
    missing_username_payload = {
        "password": "admin"
    }
    response = requests.post(url, json=missing_username_payload, headers=headers, timeout=TIMEOUT)
    assert response.status_code == 400, f"Expected 400 Bad Request for missing username, got {response.status_code}"

    # Missing password (only username)
    missing_password_payload = {
        "username": "admin"
    }
    response = requests.post(url, json=missing_password_payload, headers=headers, timeout=TIMEOUT)
    assert response.status_code == 400, f"Expected 400 Bad Request for missing password, got {response.status_code}"

    # Missing both username and password (empty body)
    empty_payload = {}
    response = requests.post(url, json=empty_payload, headers=headers, timeout=TIMEOUT)
    assert response.status_code == 400, f"Expected 400 Bad Request for missing username and password, got {response.status_code}"

    # Invalid credentials
    invalid_payload = {
        "username": "invalidUser",
        "password": "invalidPass"
    }
    response = requests.post(url, json=invalid_payload, headers=headers, timeout=TIMEOUT)
    # Invalid credentials is a business-rule violation (USER_ACCOUNT_INVALID_CREDENTIALS), which this
    # API maps to 422, not 401 (401 is reserved for missing/malformed bearer tokens on protected routes).
    assert response.status_code == 422, f"Expected 422 Unprocessable Entity for invalid credentials, got {response.status_code}"

    # Inactive account simulation - as we don't have a real inactive user,
    # we'll try a username that likely is inactive or simulate by calling with known bad username.
    # If admin is active, testing inactive account isn't possible without test setup.
    # We try with username "inactiveUser" to expect 401 unauthorized.
    inactive_user_payload = {
        "username": "inactiveUser",
        "password": "somepassword"
    }
    response = requests.post(url, json=inactive_user_payload, headers=headers, timeout=TIMEOUT)
    assert response.status_code == 422, f"Expected 422 Unprocessable Entity for inactive/unknown account, got {response.status_code}"

test_post_api_v1_security_auth_login_with_valid_and_invalid_credentials()