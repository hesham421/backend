import requests

BASE_URL = "http://localhost:7272"
API_PATH = "/api/v1/security/auth/activate"
TIMEOUT = 30


def test_activate_endpoint_valid_and_invalid_tokens():
    headers = {
        "Content-Type": "application/json"
    }

    # NOTE: a genuinely valid activation token is only ever delivered via the account-activation
    # email, which this environment cannot intercept (no SMTP configured) — so only the
    # invalid/expired/blank-token rejection paths are exercised here.

    # Non-empty but bogus tokens fail the business-rule check (ACCOUNT_ACTIVATION_TOKEN_USED) -> 422.
    for token in ["expired_token_example", "invalid_token_example"]:
        payload_invalid = {
            "token": token,
            "newPassword": "Str0ngP@ssw0rd!"
        }
        response_invalid = requests.post(
            f"{BASE_URL}{API_PATH}",
            headers=headers,
            json=payload_invalid,
            timeout=TIMEOUT
        )
        assert response_invalid.status_code == 422, (
            f"Expected 422 for invalid/expired token '{token}', got {response_invalid.status_code}"
        )
        resp_json = response_invalid.json()
        assert resp_json.get("success") is False
        assert "error" in resp_json and "code" in resp_json["error"]

    # An empty token fails @Valid bean validation before reaching business logic -> 400.
    payload_blank = {"token": "", "newPassword": "Str0ngP@ssw0rd!"}
    response_blank = requests.post(
        f"{BASE_URL}{API_PATH}",
        headers=headers,
        json=payload_blank,
        timeout=TIMEOUT
    )
    assert response_blank.status_code == 400, f"Expected 400 for blank token, got {response_blank.status_code}"


test_activate_endpoint_valid_and_invalid_tokens()
