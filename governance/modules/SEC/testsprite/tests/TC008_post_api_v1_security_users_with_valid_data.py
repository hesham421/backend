import requests
import uuid

BASE_URL = "http://localhost:7272"
LOGIN_URL = f"{BASE_URL}/api/v1/security/auth/login"
USERS_URL = f"{BASE_URL}/api/v1/security/users"

TimeoutSeconds = 30


def test_post_api_v1_security_users_with_valid_data():
    # Step 1: Authenticate to get Bearer token
    login_payload = {"username": "admin", "password": "admin"}
    try:
        login_resp = requests.post(LOGIN_URL, json=login_payload, timeout=TimeoutSeconds)
        assert login_resp.status_code == 200, f"Login failed with status {login_resp.status_code}"
        login_data = login_resp.json()
        access_token = login_data.get("data", {}).get("accessToken")
        assert access_token, "Missing accessToken in login response"
    except Exception as ex:
        raise AssertionError(f"Authentication step failed: {ex}")

    headers = {"Authorization": f"Bearer {access_token}"}

    # Generate unique username to avoid duplicate-key conflicts
    unique_username = f"jdoe_{uuid.uuid4().hex[:8]}"
    user_payload = {
        "username": unique_username,
        "email": f"{unique_username}@example.com",
        "phone": "+1234567890",
        "fullName": "John Doe Test",
        "preferredLangId": "EN"
    }

    created_user_id = None

    try:
        resp = requests.post(USERS_URL, json=user_payload, headers=headers, timeout=TimeoutSeconds)
        assert resp.status_code == 201, f"Create user failed with status {resp.status_code}"
        resp_data = resp.json()
        user_data = resp_data.get("data")
        assert user_data is not None, "No user data found in response"
        created_user_id = user_data.get("id")
        assert created_user_id is not None, "Created user id not returned"
        # Check that userStatusId indicates pending-activation or similar (if this field is present)
        # No explicit field was given, so just key presence
        assert user_data.get("username") == unique_username
    except Exception as ex:
        raise AssertionError(f"User creation failed: {ex}")
    finally:
        # Clean up: Delete the created user (soft delete: 204 expected)
        if created_user_id:
            try:
                delete_url = f"{USERS_URL}/{created_user_id}"
                del_resp = requests.delete(delete_url, headers=headers, timeout=TimeoutSeconds)
                assert del_resp.status_code == 204, f"Delete user failed with status {del_resp.status_code}"
            except Exception as del_ex:
                # Log or ignore deletion failure as it is cleanup
                pass


test_post_api_v1_security_users_with_valid_data()