import requests

BASE_URL = "http://localhost:7272/actuator/health"
LOGIN_PATH = "/api/auth/login"
TIMEOUT = 30

def test_post_api_auth_login_returns_access_token_on_valid_credentials():
    url = BASE_URL.replace('/actuator/health', '') + LOGIN_PATH
    payload = {
        "username": "admin",
        "password": "admin"
    }
    headers = {
        "Content-Type": "application/json"
    }

    try:
        response = requests.post(url, json=payload, headers=headers, timeout=TIMEOUT)
    except requests.RequestException as e:
        assert False, f"Request to {url} failed: {e}"

    assert response.status_code == 200, f"Expected status code 200, got {response.status_code}"
    try:
        resp_json = response.json()
    except ValueError:
        assert False, "Response is not valid JSON"

    assert "success" in resp_json, "Response JSON missing 'success' key"
    assert resp_json["success"] is True, f"Expected success=True, got {resp_json.get('success')}"
    data = resp_json.get("data")
    assert data is not None and isinstance(data, dict), "Response JSON 'data' missing or not an object"
    assert "accessToken" in data and isinstance(data["accessToken"], str) and len(data["accessToken"]) > 0, "Missing or invalid accessToken"
    assert "expiresIn" in data and (isinstance(data["expiresIn"], int) or isinstance(data["expiresIn"], float)), "Missing or invalid expiresIn"

    # Optionally, verify token format or perform a subsequent authenticated call if desired here

test_post_api_auth_login_returns_access_token_on_valid_credentials()