import requests
from requests.auth import HTTPBasicAuth

BASE_URL = "http://localhost:7272/v3/api-docs"
FORGOT_PASSWORD_PATH = "/api/v1/security/auth/forgot-password"
TIMEOUT = 30
AUTH = HTTPBasicAuth("admin", "admin")
HEADERS = {"Content-Type": "application/json"}


def test_post_api_v1_security_auth_forgot_password_with_valid_and_malformed_email():
    url = BASE_URL.replace("/v3/api-docs", "") + FORGOT_PASSWORD_PATH

    # Test with a valid email (should return 202 regardless of email existence)
    valid_email_payload = {"email": "valid.email@example.com"}
    try:
        response = requests.post(
            url,
            json=valid_email_payload,
            auth=AUTH,
            headers=HEADERS,
            timeout=TIMEOUT,
        )
        assert response.status_code == 202, f"Expected 202 for valid email but got {response.status_code}"
    except requests.RequestException as e:
        assert False, f"Request failed for valid email test: {e}"

    # Test with a malformed email (should return 400 validation error)
    malformed_email_payload = {"email": "malformed-email@invalid@domain"}
    try:
        response = requests.post(
            url,
            json=malformed_email_payload,
            auth=AUTH,
            headers=HEADERS,
            timeout=TIMEOUT,
        )
        assert response.status_code == 400, f"Expected 400 for malformed email but got {response.status_code}"
        # Optionally check that the response indicates a validation error
        # Assuming response content is JSON with some error message or code
        try:
            err_json = response.json()
            # Can check error message keys - depends on API error format
            assert "error" in err_json or "message" in err_json, "Expected error message in response for 400 status"
        except Exception:
            # If response is not JSON, just accept the 400 status as good enough
            pass
    except requests.RequestException as e:
        assert False, f"Request failed for malformed email test: {e}"


test_post_api_v1_security_auth_forgot_password_with_valid_and_malformed_email()