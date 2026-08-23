import requests

BASE_URL = "http://localhost:7272/actuator/health"
LOGIN_PATH = "/api/auth/login"
TIMEOUT = 30

def test_auth_login_with_valid_credentials():
    url = BASE_URL.replace("/actuator/health", "") + LOGIN_PATH
    payload = {
        "username": "admin",
        "password": "admin"
    }
    headers = {
        "Content-Type": "application/json"
    }
    try:
        response = requests.post(url, json=payload, headers=headers, timeout=TIMEOUT)
        assert response.status_code == 200, f"Unexpected status code: {response.status_code}"
        json_response = response.json()
        assert "success" in json_response and json_response["success"] is True, "Expected success true in response"
        data = json_response.get("data")
        assert data is not None, "Response data is missing"
        assert "accessToken" in data and isinstance(data["accessToken"], str) and data["accessToken"], "accessToken missing or invalid"
        assert "expiresIn" in data and (isinstance(data["expiresIn"], int) or isinstance(data["expiresIn"], float)), "expiresIn missing or invalid"
    except requests.RequestException as e:
        assert False, f"Request failed: {e}"

test_auth_login_with_valid_credentials()