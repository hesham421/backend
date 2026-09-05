import requests

BASE_URL = "http://localhost:7272/v3/api-docs"
LOGIN_URL = BASE_URL.replace('/v3/api-docs', '') + "/api/v1/security/auth/login"
LOGOUT_URL = BASE_URL.replace('/v3/api-docs', '') + "/api/v1/security/auth/logout"

BASIC_AUTH_USERNAME = "admin"
BASIC_AUTH_PASSWORD = "admin"

def test_post_api_v1_security_auth_logout_with_and_without_valid_bearer_token():
    session = requests.Session()
    timeout = 30

    # Step 1: Login to get accessToken and refreshToken
    login_payload = {
        "username": BASIC_AUTH_USERNAME,
        "password": BASIC_AUTH_PASSWORD
    }
    try:
        login_response = session.post(
            LOGIN_URL,
            json=login_payload,
            timeout=timeout
        )
        login_response.raise_for_status()
    except requests.RequestException as e:
        raise AssertionError(f"Login request failed: {e}")

    assert login_response.status_code == 200, f"Expected 200 on login, got {login_response.status_code}"
    login_data = login_response.json()["data"]
    assert "accessToken" in login_data and isinstance(login_data["accessToken"], str)
    assert "refreshToken" in login_data and isinstance(login_data["refreshToken"], str)
    assert "expiresIn" in login_data

    access_token = login_data["accessToken"]
    refresh_token = login_data["refreshToken"]

    # Step 2: Logout with valid bearer token and refreshToken - expect 204 No Content
    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json"
    }
    logout_payload = {
        "refreshToken": refresh_token
    }
    try:
        logout_response = session.post(
            LOGOUT_URL,
            json=logout_payload,
            headers=headers,
            timeout=timeout
        )
    except requests.RequestException as e:
        raise AssertionError(f"Logout request with valid token failed: {e}")

    assert logout_response.status_code == 204, f"Expected 204 on logout with valid token, got {logout_response.status_code}"

    # Step 3: Logout without bearer token - expect 401 Unauthorized
    try:
        logout_no_auth_response = session.post(
            LOGOUT_URL,
            json=logout_payload,
            timeout=timeout
        )
    except requests.RequestException as e:
        raise AssertionError(f"Logout request without token failed: {e}")

    assert logout_no_auth_response.status_code == 401, f"Expected 401 on logout without bearer token, got {logout_no_auth_response.status_code}"

test_post_api_v1_security_auth_logout_with_and_without_valid_bearer_token()
