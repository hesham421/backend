import requests

BASE_URL = "http://localhost:7272/actuator/health"
LOGIN_URL = "http://localhost:7272/api/auth/login"
PROTECTED_URL = "http://localhost:7272/api/menu/user-menu"
TIMEOUT = 30

def test_login_with_invalid_credentials_returns_401():
    # Attempt to login with invalid credentials
    invalid_credentials = {
        "username": "invalid_user",
        "password": "wrong_password"
    }
    try:
        login_response = requests.post(
            LOGIN_URL,
            json=invalid_credentials,
            timeout=TIMEOUT
        )
    except requests.RequestException as e:
        assert False, f"Login request failed unexpectedly: {e}"

    # Assert HTTP 401 Unauthorized or 400 with success false (per PRD it may be 401 or 400)
    # But test case expects 401, accept 401 only
    assert login_response.status_code == 401, \
        f"Expected status code 401, got {login_response.status_code}"

    # The response is a JSON ApiResponse envelope; parse it
    try:
        resp_json = login_response.json()
    except ValueError:
        assert False, "Login response is not valid JSON"

    # Validate ApiResponse envelope has success: false
    assert isinstance(resp_json, dict), "Response JSON is not an object"
    assert resp_json.get("success") is False, f"Expected success: false, got: {resp_json.get('success')}"

    # Now test that protected endpoint rejects request without Authorization header
    try:
        protected_response = requests.get(PROTECTED_URL, timeout=TIMEOUT)
    except requests.RequestException as e:
        assert False, f"Protected endpoint request failed unexpectedly: {e}"

    # Protected endpoints require valid Authorization header; expect 401 or 403
    assert protected_response.status_code in (401, 403), \
        f"Expected 401 or 403 when accessing protected endpoint without auth, got {protected_response.status_code}"

    # Optionally, verify response is an ApiResponse envelope with success false
    try:
        protected_json = protected_response.json()
        assert isinstance(protected_json, dict), "Protected endpoint response JSON not an object"
        assert protected_json.get("success") is False, \
            f"Expected success: false on protected endpoint without auth, got: {protected_json.get('success')}"
    except ValueError:
        # Some servers might respond with empty body or HTML error page - accept JSON only if present
        pass

test_login_with_invalid_credentials_returns_401()