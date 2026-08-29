import requests
from requests.auth import HTTPBasicAuth

BASE_URL = "http://localhost:7272"
TIMEOUT = 30
AUTH_USERNAME = "admin"
AUTH_PASSWORD = "admin"

def test_post_api_auth_reset_password_with_invalid_token():
    url = f"{BASE_URL}/api/auth/reset-password"
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Basic {requests.auth._basic_auth_str(AUTH_USERNAME, AUTH_PASSWORD).split(' ')[1]}"
    }
    # Use an invalid or expired token deliberately
    payload = {
        "token": "invalid_or_expired_token_example",
        "newPassword": "NewPassword123!"
    }
    try:
        response = requests.post(url, json=payload, headers=headers, timeout=TIMEOUT)
        # Expected to fail with 400 or 401 status codes
        assert response.status_code in (400, 401), f"Expected 400 or 401, got {response.status_code}"
        # Response body might contain error detail, but at least check no 200
        assert response.text, "Response body should not be empty"
    except requests.RequestException as e:
        assert False, f"Request failed: {e}"

test_post_api_auth_reset_password_with_invalid_token()