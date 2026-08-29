import requests

BASE_URL = "http://localhost:7272/actuator/health"
LOGIN_URL = BASE_URL.replace("/actuator/health", "") + "/api/auth/login"
TIMEOUT = 30

def verify_login_endpoint_with_valid_and_invalid_credentials():
    headers = {
        "Content-Type": "application/json"
    }

    # Test with valid credentials
    valid_payload = {
        "username": "admin",
        "password": "admin"
    }

    try:
        valid_response = requests.post(
            LOGIN_URL,
            json=valid_payload,
            headers=headers,
            timeout=TIMEOUT
        )
    except requests.RequestException as e:
        assert False, f"Valid credentials login request failed with exception: {e}"

    # Validate valid response
    assert valid_response.status_code == 200, f"Expected 200, got {valid_response.status_code}"
    try:
        valid_json = valid_response.json()
    except ValueError:
        assert False, "Valid login response is not valid JSON"

    assert valid_json.get("success") is True, "Valid login response success flag is not True"
    data = valid_json.get("data")
    assert isinstance(data, dict), "Valid login response data is not a dict"
    assert "accessToken" in data and isinstance(data["accessToken"], str) and data["accessToken"], "accessToken missing or invalid in valid login response"
    assert "expiresIn" in data and (isinstance(data["expiresIn"], int) or isinstance(data["expiresIn"], float)), "expiresIn missing or invalid in valid login response"

    # Test with invalid credentials (wrong password)
    invalid_payload = {
        "username": "admin",
        "password": "wrongpassword"
    }

    try:
        invalid_response = requests.post(
            LOGIN_URL,
            json=invalid_payload,
            headers=headers,
            timeout=TIMEOUT
        )
    except requests.RequestException as e:
        assert False, f"Invalid credentials login request failed with exception: {e}"

    # Validate invalid response status code (401 or 400)
    assert invalid_response.status_code in (400, 401), f"Expected 400 or 401, got {invalid_response.status_code}"
    try:
        invalid_json = invalid_response.json()
    except ValueError:
        assert False, "Invalid login response is not valid JSON"

    assert invalid_json.get("success") is False, "Invalid login response success flag is not False"
    error = invalid_json.get("error")
    assert isinstance(error, dict), "Invalid login response error is not a dict"
    assert "code" in error and isinstance(error["code"], str), "Error code missing or invalid in invalid login response"
    assert "details" in error and isinstance(error["details"], str) and error["details"], "Error details missing or invalid in invalid login response"

verify_login_endpoint_with_valid_and_invalid_credentials()