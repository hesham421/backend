import requests

BASE_URL = "http://localhost:7272"
LOGIN_PATH = "/api/v1/sec/auth/login"
TIMEOUT = 30

def test_post_api_v1_sec_auth_login_with_invalid_credentials():
    url = BASE_URL + LOGIN_PATH
    headers = {"Content-Type": "application/json"}
    invalid_credentials_list = [
        {"username": "invaliduser", "password": "admin"},
        {"username": "admin", "password": "wrongpassword"},
        {"username": "invaliduser", "password": "wrongpassword"},
        {"username": "", "password": "admin"},
        {"username": "admin", "password": ""},
    ]
    for creds in invalid_credentials_list:
        try:
            response = requests.post(url, json=creds, headers=headers, timeout=TIMEOUT)
        except requests.RequestException as e:
            assert False, f"Request failed: {e}"

        # Assert status code is 401
        assert response.status_code == 401, f"Expected 401 status but got {response.status_code} for credentials {creds}"

        # Assert response body is a JSON with success:false and error.code = SEC-401-INVALID-CREDENTIALS
        try:
            body = response.json()
        except ValueError:
            assert False, "Response is not valid JSON"

        assert isinstance(body, dict), "Response JSON is not a dict"
        assert body.get("success") is False, f"Expected success=false but got {body.get('success')} for credentials {creds}"
        error = body.get("error")
        assert isinstance(error, dict), "Response error field is not a dict"
        code = error.get("code")
        assert code == "SEC-401-INVALID-CREDENTIALS", f"Expected error code SEC-401-INVALID-CREDENTIALS but got {code} for credentials {creds}"

# Run the test function
test_post_api_v1_sec_auth_login_with_invalid_credentials()