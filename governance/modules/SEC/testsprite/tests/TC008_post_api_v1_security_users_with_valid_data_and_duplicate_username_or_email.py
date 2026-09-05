import requests
from requests.auth import HTTPBasicAuth
import uuid

TOKEN_AUTH_URL = "http://localhost:7272/api/v1/security/auth/login"
USERS_URL = "http://localhost:7272/api/v1/security/users"

AUTH_CREDENTIALS = ("admin", "admin")
TIMEOUT = 30


def test_post_api_v1_security_users_with_valid_and_duplicate_and_invalid_data():
    # Step 1: Authenticate with basic token to get bearer token for subsequent requests
    auth_response = requests.post(
        TOKEN_AUTH_URL,
        json={"username": AUTH_CREDENTIALS[0], "password": AUTH_CREDENTIALS[1]},
        timeout=TIMEOUT,
    )
    assert auth_response.status_code == 200, f"Login failed with status {auth_response.status_code} and body {auth_response.text}"
    auth_json = auth_response.json()["data"]
    access_token = auth_json.get("accessToken")
    assert access_token, "accessToken missing in login response"

    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json",
    }

    created_user_ids = []

    def create_user(payload):
        resp = requests.post(USERS_URL, json=payload, headers=headers, timeout=TIMEOUT)
        return resp

    try:
        # Test 1: Create user with valid data -> Expect 201 with created user response
        unique_suffix = str(uuid.uuid4())[:8]
        valid_user_payload = {
            "username": f"testuser_{unique_suffix}",
            "email": f"testuser_{unique_suffix}@example.com",
            "phone": "1234567890",
            "fullName": "Test User",
            "preferredLangId": "en"
        }
        resp_valid = create_user(valid_user_payload)
        assert resp_valid.status_code == 201, f"Expected 201 for valid user creation but got {resp_valid.status_code}, response: {resp_valid.text}"
        user_created = resp_valid.json()["data"]
        for key in ["id", "username", "email", "phone", "fullName", "preferredLangId", "userStatusId", "failedLoginCount", "lockedUntil", "isActiveFl", "createdAt", "createdBy", "updatedAt", "updatedBy"]:
            assert key in user_created, f"Key '{key}' missing in created user response"
        created_user_ids.append(user_created["id"])

        # Test 2: Create user with duplicate username -> Expect 409 conflict
        duplicate_username_payload = {
            "username": valid_user_payload["username"],  # duplicate username
            "email": f"uniqueemail_{unique_suffix}@example.com",
            "phone": "0987654321",
            "fullName": "Duplicate User",
            "preferredLangId": "en"
        }
        resp_dup_username = create_user(duplicate_username_payload)
        assert resp_dup_username.status_code == 409, f"Expected 409 for duplicate username but got {resp_dup_username.status_code}, response: {resp_dup_username.text}"

        # Test 3: Create user with duplicate email -> Expect 409 conflict
        duplicate_email_payload = {
            "username": f"uniqueuser_{unique_suffix}",
            "email": valid_user_payload["email"],  # duplicate email
            "phone": "0987654321",
            "fullName": "Duplicate Email User",
            "preferredLangId": "en"
        }
        resp_dup_email = create_user(duplicate_email_payload)
        assert resp_dup_email.status_code == 409, f"Expected 409 for duplicate email but got {resp_dup_email.status_code}, response: {resp_dup_email.text}"

        # Test 4: Create user with missing required fields -> Expect 400 validation error
        invalid_payloads = [
            {},  # completely empty
            {"username": "someuser"},  # missing email and other required
            {"email": "emailonly@example.com"},  # missing username and others
            {"username": "", "email": ""},  # empty strings as required fields
            {"username": "user", "email": None},  # email null
        ]
        for invalid_payload in invalid_payloads:
            resp_invalid = create_user(invalid_payload)
            assert resp_invalid.status_code == 400, f"Expected 400 for invalid payload {invalid_payload} but got {resp_invalid.status_code}, response: {resp_invalid.text}"

    finally:
        # Cleanup: Delete created users (soft-deactivate)
        for user_id in created_user_ids:
            try:
                del_resp = requests.delete(f"{USERS_URL}/{user_id}", headers=headers, timeout=TIMEOUT)
                # Per PRD, expect 204 no content on successful deletion
                assert del_resp.status_code == 204, f"User cleanup deletion failed with status {del_resp.status_code} for user id {user_id}, response: {del_resp.text}"
            except Exception:
                # ignore cleanup errors to avoid masking test results
                pass


test_post_api_v1_security_users_with_valid_and_duplicate_and_invalid_data()