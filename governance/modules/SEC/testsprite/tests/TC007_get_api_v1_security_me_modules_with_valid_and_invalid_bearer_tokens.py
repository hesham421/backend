import requests

BASE_URL = "http://localhost:7272"
TIMEOUT = 30

USERNAME = "admin"
PASSWORD = "admin"

def test_get_api_v1_security_me_modules_with_valid_and_invalid_bearer_tokens():
    login_url = f"{BASE_URL}/api/v1/security/auth/login"
    me_modules_url = f"{BASE_URL}/api/v1/security/me/modules"

    # Step 1: Login to get valid bearer token
    login_payload = {
        "username": USERNAME,
        "password": PASSWORD
    }
    try:
        login_response = requests.post(login_url, json=login_payload, timeout=TIMEOUT)
        assert login_response.status_code == 200, f"Login failed with status {login_response.status_code}, response: {login_response.text}"
        login_json = login_response.json()["data"]
        access_token = login_json.get("accessToken")
        assert isinstance(access_token, str) and len(access_token) > 0, "No accessToken received in login response"
    except Exception as e:
        raise AssertionError(f"Login request failed: {e}")

    headers_valid = {
        "Authorization": f"Bearer {access_token}"
    }

    # Step 2: Test GET /api/v1/security/me/modules with valid bearer token
    try:
        response = requests.get(me_modules_url, headers=headers_valid, timeout=TIMEOUT)
        assert response.status_code == 200, f"Expected 200 with valid token, got {response.status_code}, response: {response.text}"
        modules = response.json()["data"]
        assert isinstance(modules, list), "Response is not a list"
        # If list not empty, check fields of first module
        if modules:
            module = modules[0]
            expected_keys = {"id", "moduleCode", "nameAr", "nameEn", "isActiveFl", "createdAt", "createdBy", "updatedAt", "updatedBy"}
            assert expected_keys.issubset(module.keys()), f"Module keys missing expected keys: missing {expected_keys - module.keys()}"
    except Exception as e:
        raise AssertionError(f"GET /api/v1/security/me/modules with valid token failed: {e}")

    # Step 3: Test GET /api/v1/security/me/modules without bearer token
    try:
        response_no_token = requests.get(me_modules_url, timeout=TIMEOUT)
        assert response_no_token.status_code == 401, f"Expected 401 without token, got {response_no_token.status_code}, response: {response_no_token.text}"
    except Exception as e:
        raise AssertionError(f"GET /api/v1/security/me/modules without token failed: {e}")

    # Step 4: Test GET /api/v1/security/me/modules with malformed bearer token
    headers_malformed = {
        "Authorization": "Bearer malformed.token.value"
    }
    try:
        response_malformed = requests.get(me_modules_url, headers=headers_malformed, timeout=TIMEOUT)
        assert response_malformed.status_code == 401, f"Expected 401 with malformed token, got {response_malformed.status_code}, response: {response_malformed.text}"
    except Exception as e:
        raise AssertionError(f"GET /api/v1/security/me/modules with malformed token failed: {e}")

    # Step 5: Test GET /api/v1/security/me/modules with expired bearer token simulation
    # Since we do not have a real expired token, try a random invalid token structure to simulate expiry
    headers_expired = {
        "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid-expired-token"
    }
    try:
        response_expired = requests.get(me_modules_url, headers=headers_expired, timeout=TIMEOUT)
        assert response_expired.status_code == 401, f"Expected 401 with expired token, got {response_expired.status_code}, response: {response_expired.text}"
    except Exception as e:
        raise AssertionError(f"GET /api/v1/security/me/modules with expired token failed: {e}")

test_get_api_v1_security_me_modules_with_valid_and_invalid_bearer_tokens()
