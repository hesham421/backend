import requests

BASE_URL = "http://localhost:7272"
LOGIN_URL = f"{BASE_URL}/api/v1/security/auth/login"
ME_MODULES_URL = f"{BASE_URL}/api/v1/security/me/modules"
TIMEOUT = 30

def test_get_api_v1_security_me_modules_with_valid_bearer_token():
    # Step 1: Authenticate and get accessToken
    login_payload = {
        "username": "admin",
        "password": "admin"
    }
    try:
        login_response = requests.post(LOGIN_URL, json=login_payload, timeout=TIMEOUT)
        assert login_response.status_code == 200, f"Login failed with status code {login_response.status_code}"
        login_json = login_response.json()
        access_token = login_json.get("data", {}).get("accessToken")
        assert access_token and isinstance(access_token, str), "accessToken missing or invalid in login response"
    except requests.RequestException as e:
        assert False, f"Login request failed: {e}"

    headers = {
        "Authorization": f"Bearer {access_token}"
    }

    # Step 2: Access /api/v1/security/me/modules endpoint with valid bearer token
    try:
        modules_response = requests.get(ME_MODULES_URL, headers=headers, timeout=TIMEOUT)
        assert modules_response.status_code == 200, f"Expected 200 OK, got {modules_response.status_code}"
        resp_json = modules_response.json()
        assert "data" in resp_json, "'data' field missing in response"
        modules_list = resp_json["data"]
        assert isinstance(modules_list, list), f"Expected 'data' to be a list, got {type(modules_list)}"
    except requests.RequestException as e:
        assert False, f"Request to get modules failed: {e}"

test_get_api_v1_security_me_modules_with_valid_bearer_token()