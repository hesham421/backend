import requests
from requests.auth import HTTPBasicAuth

BASE_URL = "http://localhost:7272"
LOGIN_PATH = "/api/auth/login"
NOTIFICATIONS_UNREAD_PATH = "/api/v1/notifications/unread"
USERNAME = "admin"
PASSWORD = "admin"
TIMEOUT = 30


def test_notification_unread_endpoint_returns_known_422():
    try:
        # Step 1: Login to get JWT access token
        login_url = BASE_URL + LOGIN_PATH
        login_payload = {
            "username": USERNAME,
            "password": PASSWORD
        }
        login_response = requests.post(
            login_url,
            json=login_payload,
            timeout=TIMEOUT,
            auth=HTTPBasicAuth(USERNAME, PASSWORD)
        )
        assert login_response.status_code == 200, f"Login failed with status {login_response.status_code}"
        login_json = login_response.json()
        assert login_json.get("success") is True, "Login response success flag is not True"
        access_token = login_json.get("data", {}).get("accessToken")
        assert access_token, "Access token not found in login response"

        # Step 2: Call GET /api/v1/notifications/unread with Authorization header including access token
        unread_url = BASE_URL + NOTIFICATIONS_UNREAD_PATH
        headers = {
            "Authorization": f"Bearer {access_token}"
        }
        unread_response = requests.get(unread_url, headers=headers, timeout=TIMEOUT)

        # Step 3: Assert that response status code is 422
        assert unread_response.status_code == 422, f"Expected status code 422, got {unread_response.status_code}"

        # Step 4: Assert error code NOTIF_READ_TRACKING_UNAVAILABLE in response body error or message
        try:
            resp_json = unread_response.json()
        except ValueError:
            resp_json = {}

        error_code = None
        # error may be in top-level "error" or message contains error code
        if "error" in resp_json:
            error_val = resp_json.get("error")
            if isinstance(error_val, dict):
                error_code = error_val.get("code")
            else:
                error_code = error_val
        elif "message" in resp_json:
            if "NOTIF_READ_TRACKING_UNAVAILABLE" in resp_json.get("message", ""):
                error_code = "NOTIF_READ_TRACKING_UNAVAILABLE"
        assert error_code == "NOTIF_READ_TRACKING_UNAVAILABLE", f"Expected error code NOTIF_READ_TRACKING_UNAVAILABLE, got {error_code}"

    except requests.RequestException as e:
        assert False, f"RequestException raised: {e}"


test_notification_unread_endpoint_returns_known_422()