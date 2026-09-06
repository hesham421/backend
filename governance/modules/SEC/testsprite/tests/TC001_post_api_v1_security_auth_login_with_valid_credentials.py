import requests

def test_post_api_v1_security_auth_login_with_valid_credentials():
    base_url = "http://localhost:7272"
    login_url = f"{base_url}/api/v1/security/auth/login"
    login_payload = {
        "username": "admin",
        "password": "admin"
    }
    headers = {
        "Content-Type": "application/json"
    }
    try:
        response = requests.post(login_url, json=login_payload, headers=headers, timeout=30)
    except requests.RequestException as e:
        assert False, f"Request to login endpoint failed: {e}"

    assert response.status_code == 200, f"Expected 200 OK, got {response.status_code}"
    try:
        json_response = response.json()
    except ValueError:
        assert False, "Response is not a valid JSON"

    assert "data" in json_response, "'data' field missing in response"
    data = json_response["data"]
    assert isinstance(data, dict), "'data' field is not a dictionary"

    assert "accessToken" in data, "'accessToken' missing in response data"
    assert isinstance(data["accessToken"], str) and data["accessToken"], "Invalid 'accessToken'"

    assert "refreshToken" in data, "'refreshToken' missing in response data"
    assert isinstance(data["refreshToken"], str) and data["refreshToken"], "Invalid 'refreshToken'"

    assert "expiresIn" in data, "'expiresIn' missing in response data"
    assert isinstance(data["expiresIn"], int) and data["expiresIn"] > 0, "Invalid 'expiresIn' value"

test_post_api_v1_security_auth_login_with_valid_credentials()