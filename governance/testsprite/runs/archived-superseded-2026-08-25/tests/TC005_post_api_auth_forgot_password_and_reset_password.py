import requests
import time

BASE_URL = "http://localhost:7272"
FORGOT_PASSWORD_PATH = "/api/auth/forgot-password"
RESET_PASSWORD_PATH = "/api/auth/reset-password"
LOGIN_PATH = "/api/auth/login"

TIMEOUT = 30


def test_post_api_auth_forgot_password_and_reset_password():
    session = requests.Session()

    # Use admin credentials to login and get a valid JWT token (simulate to get reset token)
    login_payload = {
        "username": "admin",
        "password": "admin"
    }
    login_url = f"{BASE_URL}/api/auth/login"
    try:
        login_resp = session.post(login_url, json=login_payload, timeout=TIMEOUT)
        assert login_resp.status_code == 200, f"Login failed with status {login_resp.status_code}"
        login_data = login_resp.json()
        access_token = login_data.get("accessToken")
        assert access_token, "accessToken not found in login response"
    except Exception as e:
        raise AssertionError(f"Login failed: {e}")

    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json"
    }

    # Step 1: POST /api/auth/forgot-password with an email (existing and non-existing)
    forgot_password_url = f"{BASE_URL}{FORGOT_PASSWORD_PATH}"
    test_emails = ["admin@example.com", "nonexistent@example.com"]
    for email in test_emails:
        payload = {"email": email}
        try:
            resp = requests.post(forgot_password_url, json=payload, timeout=TIMEOUT)
            assert resp.status_code == 200, f"Forgot password returned {resp.status_code} for email {email}"
        except Exception as e:
            raise AssertionError(f"Forgot password request failed for email {email}: {e}")

    # Create a new user to generate a reset token scenario
    signup_url = f"{BASE_URL}/api/auth/signup"
    new_username = f"testuser_{int(time.time())}"
    new_email = f"{new_username}@example.com"
    new_password = "InitialPass123!"
    new_password_reset = "NewPass123!"

    signup_payload = {
        "username": new_username,
        "email": new_email,
        "password": new_password
    }
    try:
        signup_resp = requests.post(signup_url, json=signup_payload, timeout=TIMEOUT)
        assert signup_resp.status_code == 200, f"Signup failed with status {signup_resp.status_code}"
    except Exception as e:
        raise AssertionError(f"Signup request failed: {e}")

    # Call forgot-password with the new user email (should return 200 always)
    try:
        forgot_resp = requests.post(forgot_password_url, json={"email": new_email}, timeout=TIMEOUT)
        assert forgot_resp.status_code == 200, "Forgot-password request did not return 200 OK"
    except Exception as e:
        raise AssertionError(f"Forgot-password request failed: {e}")

    # Reset password with the assumed valid token
    valid_reset_token = "valid-reset-token-placeholder"

    reset_password_url = f"{BASE_URL}{RESET_PASSWORD_PATH}"
    reset_payload = {
        "token": valid_reset_token,
        "newPassword": new_password_reset
    }
    try:
        reset_resp = requests.post(reset_password_url, json=reset_payload, timeout=TIMEOUT)
        if reset_resp.status_code != 200:
            raise AssertionError(f"Reset password failed with status {reset_resp.status_code} and body: {reset_resp.text}")
    except Exception as e:
        raise AssertionError(f"Reset password request failed: {e}")


test_post_api_auth_forgot_password_and_reset_password()