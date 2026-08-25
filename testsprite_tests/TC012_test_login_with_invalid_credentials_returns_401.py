import requests

BASE_URL = "http://localhost:7272"

def test_login_with_invalid_credentials_returns_401():
    login_url = f"{BASE_URL}/api/auth/login"
    invalid_credentials = {
        "username": "invalid_user",
        "password": "wrong_password"
    }
    headers = {
        "Content-Type": "application/json"
    }
    timeout = 30

    # Attempt login with invalid credentials
    response = requests.post(login_url, json=invalid_credentials, headers=headers, timeout=timeout)
    # Expect 401 Unauthorized or error response with success: false
    assert response.status_code == 401 or response.status_code == 400, f"Expected 401 or 400, got {response.status_code}"
    json_response = response.json()
    assert not json_response.get("success", True), "Expected success:false in response"
    assert "error" in json_response or "message" in json_response, "Expected error or message in response"

    # Attempt to access a protected endpoint without Authorization header
    protected_url = f"{BASE_URL}/api/users"
    protected_response = requests.get(protected_url, timeout=timeout)
    # Expect 401 or 403 Forbidden
    assert protected_response.status_code in (401, 403), f"Expected 401 or 403 for unauthorized access, got {protected_response.status_code}"

test_login_with_invalid_credentials_returns_401()