import requests
from requests.auth import HTTPBasicAuth
import uuid
from datetime import datetime, timedelta

BASE_URL = "http://localhost:7272"
AUTH_USERNAME = "admin"
AUTH_PASSWORD = "admin"
TIMEOUT = 30


def get_jwt_token():
    login_url = f"{BASE_URL}/api/auth/login"
    login_payload = {
        "username": AUTH_USERNAME,
        "password": AUTH_PASSWORD
    }
    resp = requests.post(login_url, json=login_payload, timeout=TIMEOUT)
    resp.raise_for_status()
    json_resp = resp.json()
    assert json_resp.get("success") is True
    data = json_resp.get("data")
    assert "accessToken" in data
    return data["accessToken"]


def test_notifications_send_and_schedule():
    token = get_jwt_token()
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }

    # Prepare a minimal viable notification payload
    # Using a unique referenceId per run to avoid collisions
    unique_reference_id = uuid.uuid4().int >> 64  # large int for referenceId
    recipient_id = 1  # Assuming 1 is a valid recipientId in the system; adjust if needed
    channel_hint = ["EMAIL"]
    template_code = "DEFAULT_TEMPLATE"  # Assumed to exist; adjust if needed
    context_data = {"exampleKey": "exampleValue"}
    priority = "NORMAL"
    module_code = "NOTIFICATION_MODULE"
    reference_type = "TEST_CASE"

    send_url = f"{BASE_URL}/api/v1/notifications/send"
    schedule_url = f"{BASE_URL}/api/v1/notifications/schedule"

    # Test immediate send
    send_payload = {
        "recipientId": recipient_id,
        "channelHint": channel_hint,
        "templateCode": template_code,
        "contextData": context_data,
        "priority": priority,
        "moduleCode": module_code,
        "referenceId": unique_reference_id,
        "referenceType": reference_type
    }
    send_resp = requests.post(send_url, headers=headers, json=send_payload, timeout=TIMEOUT)
    assert send_resp.status_code == 201, f"Unexpected status code for send: {send_resp.status_code}"
    send_json = send_resp.json()
    assert send_json.get("success") is True, f"Send failure: {send_json}"
    send_data = send_json.get("data")
    assert send_data is not None
    assert isinstance(send_data, dict)

    # Test scheduled send with scheduledAt in near future (1 minute ahead)
    scheduled_at = (datetime.utcnow() + timedelta(minutes=1)).isoformat() + "Z"

    schedule_payload = {
        "recipientId": recipient_id,
        "channelHint": channel_hint,
        "templateCode": template_code,
        "contextData": context_data,
        "priority": priority,
        "moduleCode": module_code,
        "referenceId": unique_reference_id + 1,  # different id to distinguish
        "referenceType": reference_type,
        "scheduledAt": scheduled_at
    }
    schedule_resp = requests.post(schedule_url, headers=headers, json=schedule_payload, timeout=TIMEOUT)
    assert schedule_resp.status_code == 201, f"Unexpected status code for schedule: {schedule_resp.status_code}"
    schedule_json = schedule_resp.json()
    assert schedule_json.get("success") is True, f"Schedule failure: {schedule_json}"
    schedule_data = schedule_json.get("data")
    assert schedule_data is not None

    # According to PRD, scheduled notifications are dispatched immediately despite scheduledAt

    # Done


test_notifications_send_and_schedule()
