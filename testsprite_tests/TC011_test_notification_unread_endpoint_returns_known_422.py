import requests
from requests.auth import HTTPBasicAuth

def test_notification_unread_endpoint_returns_known_422():
    base_url = "http://localhost:7272"
    login_url = f"{base_url}/api/auth/login"
    unread_notifications_url = f"{base_url}/api/v1/notifications/unread"

    username = "admin"
    password = "admin"
    timeout = 30

    # Login to get JWT access token
    login_payload = {
        "username": username,
        "password": password
    }
    try:
        login_response = requests.post(login_url, json=login_payload, timeout=timeout)
        login_response.raise_for_status()
    except requests.RequestException as e:
        raise AssertionError(f"Login request failed: {e}")

    login_json = login_response.json()
    assert login_json.get("success") is True, "Login was not successful"
    access_token = login_json.get("data", {}).get("accessToken")
    assert access_token, "No accessToken found in login response"

    headers = {
        "Authorization": f"Bearer {access_token}",
        "Accept": "application/json"
    }

    # Call the GET /api/v1/notifications/unread endpoint expecting HTTP 422
    response = requests.get(unread_notifications_url, headers=headers, timeout=timeout)

    # Assert that status code is 422 (Unprocessable Entity)
    assert response.status_code == 422, f"Expected status code 422, got {response.status_code}"

    try:
        response_json = response.json()
    except ValueError:
        raise AssertionError("Response is not valid JSON")

    # According to documented error format, the error code NOTIF_READ_TRACKING_UNAVAILABLE should be indicated
    error_code = response_json.get("error", {}).get("code") or response_json.get("error", {}).get("errorCode")
    if not error_code:
        # Sometimes error code might be directly under error or under a message, fallback extraction
        error_code = response_json.get("error")

    assert error_code == "NOTIF_READ_TRACKING_UNAVAILABLE", \
        f"Expected error code 'NOTIF_READ_TRACKING_UNAVAILABLE', got '{error_code}'"

test_notification_unread_endpoint_returns_known_422()