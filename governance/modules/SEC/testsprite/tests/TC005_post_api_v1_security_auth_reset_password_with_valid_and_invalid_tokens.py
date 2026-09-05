import requests

BASE_URL = "http://localhost:7272"
TIMEOUT = 30


def test_post_api_v1_security_auth_reset_password_with_valid_and_invalid_tokens():
    url = f"{BASE_URL}/api/v1/security/auth/reset-password"
    headers = {"Content-Type": "application/json"}

    strong_new_password = "StrongP@ssw0rd123!"

    # NOTE: a genuinely valid reset token can only be obtained via the forgot-password email flow,
    # which this environment cannot intercept (no SMTP configured) — so only the invalid/expired/
    # malformed-token paths (the security-relevant ones: does the endpoint correctly reject anything
    # that isn't a real, unexpired, unused token?) are exercised here.
    # Non-empty but bogus tokens fail the business-rule check (PASSWORD_RESET_TOKEN_USED) -> 422.
    for token in ["expired-token-placeholder", "invalid-token-placeholder"]:
        invalid_payload = {
            "token": token,
            "newPassword": strong_new_password
        }
        response_invalid = requests.post(url, json=invalid_payload, headers=headers, timeout=TIMEOUT)
        assert response_invalid.status_code == 422, f"Expected 422 for invalid/expired token '{token}', got {response_invalid.status_code}"
        json_invalid = response_invalid.json()
        assert json_invalid.get("success") is False
        assert "error" in json_invalid and "code" in json_invalid["error"]

    # An empty token fails @Valid bean validation before reaching business logic -> 400.
    blank_payload = {"token": "", "newPassword": strong_new_password}
    response_blank = requests.post(url, json=blank_payload, headers=headers, timeout=TIMEOUT)
    assert response_blank.status_code == 400, f"Expected 400 for blank token, got {response_blank.status_code}"


test_post_api_v1_security_auth_reset_password_with_valid_and_invalid_tokens()
