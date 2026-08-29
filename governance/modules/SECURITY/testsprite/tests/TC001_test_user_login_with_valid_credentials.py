import requests

BASE_URL = "http://localhost:7272"
LOGIN_ENDPOINT = "/api/auth/login"
PROTECTED_ENDPOINT = "/api/users"
TIMEOUT = 30

def test_user_login_with_valid_credentials():
    login_url = BASE_URL + LOGIN_ENDPOINT
    protected_url = BASE_URL + PROTECTED_ENDPOINT

    login_payload = {
        "username": "admin",
        "password": "admin"
    }
    headers = {
        "Content-Type": "application/json"
    }

    try:
        # Step 1: POST /api/auth/login with valid credentials
        resp = requests.post(login_url, json=login_payload, headers=headers, timeout=TIMEOUT)
        assert resp.status_code == 200, f"Expected 200 OK from login, got {resp.status_code}"
        resp_json = resp.json()
        assert resp_json.get("success") is True, "Login response success flag is not True"
        assert "data" in resp_json, "Login response missing 'data' JSON object"
        data = resp_json["data"]
        assert "accessToken" in data and isinstance(data["accessToken"], str) and data["accessToken"], "Missing or invalid accessToken in login response"
        assert "expiresIn" in data and (isinstance(data["expiresIn"], int) or isinstance(data["expiresIn"], float)), "Missing or invalid expiresIn in login response"

        access_token = data["accessToken"]

        # Step 2: Use accessToken to access a protected endpoint GET /api/users
        protected_headers = {
            "Authorization": f"Bearer {access_token}"
        }
        protected_resp = requests.get(protected_url, headers=protected_headers, timeout=TIMEOUT)
        assert protected_resp.status_code == 200, f"Expected 200 OK from protected endpoint, got {protected_resp.status_code}"
        protected_resp_json = protected_resp.json()
        assert protected_resp_json.get("success") is True, "Protected endpoint response success flag is not True"
        assert "data" in protected_resp_json, "Protected endpoint response missing 'data' JSON object"

    except requests.RequestException as e:
        assert False, f"Request failed with exception: {e}"

test_user_login_with_valid_credentials()