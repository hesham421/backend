import requests

BASE_URL = "http://localhost:7272"
TIMEOUT = 30

def test_post_api_v1_security_auth_refresh_with_valid_and_invalid_refresh_tokens():
    session = requests.Session()
    headers = {
        "Content-Type": "application/json"
    }

    # Step 1: Obtain a valid refresh token by logging in
    login_url = f"{BASE_URL}/api/v1/security/auth/login"
    login_payload = {
        "username": "admin",
        "password": "admin"
    }
    try:
        login_resp = session.post(login_url, json=login_payload, headers=headers, timeout=TIMEOUT)
        assert login_resp.status_code == 200, f"Login failed with status {login_resp.status_code}"
        login_data = login_resp.json()["data"]
        assert "refreshToken" in login_data and "accessToken" in login_data and "expiresIn" in login_data
        
        valid_refresh_token = login_data["refreshToken"]

        refresh_url = f"{BASE_URL}/api/v1/security/auth/refresh"

        # Test case 1: valid refreshToken -> expect 200 and new tokens
        refresh_payload = {"refreshToken": valid_refresh_token}
        refresh_resp = session.post(refresh_url, json=refresh_payload, headers=headers, timeout=TIMEOUT)
        assert refresh_resp.status_code == 200, f"Valid refresh token failed with status {refresh_resp.status_code}"
        refresh_data = refresh_resp.json()["data"]
        assert "accessToken" in refresh_data and "refreshToken" in refresh_data and "expiresIn" in refresh_data
        new_refresh_token = refresh_data["refreshToken"]

        # Test case 2: missing refreshToken field -> expect 400 validation error
        refresh_resp_missing = session.post(refresh_url, json={}, headers=headers, timeout=TIMEOUT)
        assert refresh_resp_missing.status_code == 400

        # Test case 3: expired, invalid, or already-rotated refreshToken -> expect 422 business-rule
        # violation (REFRESH_TOKEN_REVOKED) — this API reserves 401 for missing/malformed bearer
        # tokens on protected routes, not for domain-level rejection of a bad refresh token.
        # Use the old refresh token which, after rotation, should be invalid
        invalid_tokens = [
            "someExpiredInvalidTokenString1234567890",  # obviously invalid/expired token string
            valid_refresh_token,  # old token after rotation, should be unauthorized
        ]

        for token in invalid_tokens:
            refresh_payload_invalid = {"refreshToken": token}
            resp_invalid = session.post(refresh_url, json=refresh_payload_invalid, headers=headers, timeout=TIMEOUT)
            assert resp_invalid.status_code == 422, f"Invalid token '{token}' did not return 422, got {resp_invalid.status_code}"

        # Also test re-using the new_refresh_token twice (second time should be rejected)
        refresh_payload_new = {"refreshToken": new_refresh_token}
        resp_first = session.post(refresh_url, json=refresh_payload_new, headers=headers, timeout=TIMEOUT)
        assert resp_first.status_code == 200, f"Reusing new refresh token first time failed with {resp_first.status_code}"
        resp_second = session.post(refresh_url, json=refresh_payload_new, headers=headers, timeout=TIMEOUT)
        # The second reuse should fail as already rotated
        assert resp_second.status_code == 422, f"Reusing new refresh token second time did not fail with 422, got {resp_second.status_code}"

    finally:
        session.close()

test_post_api_v1_security_auth_refresh_with_valid_and_invalid_refresh_tokens()