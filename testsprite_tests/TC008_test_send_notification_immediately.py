import requests
from requests.auth import HTTPBasicAuth

BASE_URL = "http://localhost:7272/actuator/health"
LOGIN_URL = "http://localhost:7272/api/auth/login"
NOTIFY_SEND_URL = "http://localhost:7272/api/v1/notifications/send"
TIMEOUT = 30

def test_send_notification_immediately():
    # Step 1: Login to get JWT token using basic auth credentials given in instructions
    login_payload = {
        "username": "admin",
        "password": "admin"
    }
    try:
        login_resp = requests.post(
            LOGIN_URL,
            json=login_payload,
            timeout=TIMEOUT
        )
        login_resp.raise_for_status()
    except requests.RequestException as e:
        assert False, f"Login request failed: {e}"

    login_json = login_resp.json()
    assert login_json.get("success") is True, "Login success flag false"
    access_token = login_json.get("data", {}).get("accessToken")
    assert access_token, "Login response missing accessToken"

    # Prepare headers for authenticated request
    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json"
    }

    # Prepare notification payload per API schema
    # Using example valid data (numbers/strings)
    notification_payload = {
        "recipientId": 1,
        "channelHint": ["email", "sms"],
        "templateCode": "NOTIF_TEMPLATE_001",
        "contextData": {"key1": "value1", "key2": 123},
        "priority": "HIGH",
        "moduleCode": "ERP_MODULE",
        "referenceId": 1001,
        "referenceType": "ORDER"
    }

    try:
        send_resp = requests.post(
            NOTIFY_SEND_URL,
            headers=headers,
            json=notification_payload,
            timeout=TIMEOUT
        )
        send_resp.raise_for_status()
    except requests.RequestException as e:
        assert False, f"Notification send request failed: {e}"

    send_json = send_resp.json()
    assert send_json.get("success") is True, "Notification send success false"
    data = send_json.get("data")
    assert data is not None, "Notification send response missing data"

    # Check that data has confirmation fields (flexible as exact fields unknown)
    # We'll assert at least some expected confirmation fields exist
    # Since schema only says ApiResponse.data as NotificationSendConfirmation type
    # We'll verify minimal set is present:
    expected_keys = ["notificationId", "sentAt"]
    # Because exact keys unknown, we'll only check these optional ones if present:
    found_expected_key = any(k in data for k in expected_keys)
    # If no keys found, just assert data is dict and not empty
    assert isinstance(data, dict) and data, "NotificationSendConfirmation data is empty or invalid"

test_send_notification_immediately()