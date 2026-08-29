import requests

def test_post_api_auth_login_token_returns_full_user_info():
    base_url = "http://localhost:7272"
    endpoint = "/api/auth/login-token"
    url = base_url.rstrip("/") + endpoint

    payload = {
        "username": "admin",
        "password": "admin"
    }

    headers = {
        "Content-Type": "application/json"
    }

    try:
        response = requests.post(url, json=payload, headers=headers, timeout=30)
    except requests.RequestException as e:
        assert False, f"Request failed: {e}"

    assert response.status_code == 200, f"Expected status code 200, got {response.status_code}"
    try:
        json_response = response.json()
    except ValueError:
        assert False, "Response is not a valid JSON"

    assert "success" in json_response, "Response missing 'success' key"
    assert json_response["success"] is True, "Response 'success' is not True"
    assert "data" in json_response, "Response missing 'data' key"

    data = json_response["data"]

    expected_keys = [
        "accessToken",
        "refreshToken",
        "userId",
        "username",
        "enabled",
        "roles",
        "permissions",
        "expiresIn",
        "refreshExpiresIn"
    ]

    for key in expected_keys:
        assert key in data, f"Response data missing '{key}'"

    assert isinstance(data["accessToken"], str) and data["accessToken"], "Invalid accessToken"
    assert isinstance(data["refreshToken"], str) and data["refreshToken"], "Invalid refreshToken"
    assert isinstance(data["userId"], int), "userId is not an int"
    assert data["username"] == payload["username"], "Username in response does not match request"
    assert isinstance(data["enabled"], bool), "enabled is not a boolean"
    assert isinstance(data["roles"], list), "roles is not a list"
    assert all(isinstance(role, str) for role in data["roles"]), "One or more roles is not a string"
    assert isinstance(data["permissions"], list), "permissions is not a list"
    assert all(isinstance(perm, str) for perm in data["permissions"]), "One or more permissions is not a string"
    assert isinstance(data["expiresIn"], (int, float)), "expiresIn is not a number"
    assert isinstance(data["refreshExpiresIn"], (int, float)), "refreshExpiresIn is not a number"


test_post_api_auth_login_token_returns_full_user_info()
