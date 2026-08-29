import requests
from requests.auth import HTTPBasicAuth
import uuid

BASE_ENDPOINT = "http://localhost:7272"
LOGIN_URL = f"{BASE_ENDPOINT}/api/auth/login"
USERS_URL = f"{BASE_ENDPOINT}/api/users"
NOTIFICATION_SEND_URL = f"{BASE_ENDPOINT}/api/v1/notifications/send"
TIMEOUT = 30

def test_send_notification_immediately():
    # Step 1: Login to get JWT access token
    login_payload = {"username": "admin", "password": "admin"}
    login_resp = requests.post(LOGIN_URL, json=login_payload, timeout=TIMEOUT, auth=HTTPBasicAuth('admin', 'admin'))
    assert login_resp.status_code == 200, f"Login failed: {login_resp.text}"
    login_data = login_resp.json()
    assert login_data.get("success") is True, f"Login not successful: {login_data}"
    access_token = login_data.get("data", {}).get("accessToken")
    assert access_token, "No access token in login response"

    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json"
    }

    # Step 2: Ensure there is an existing user to reference as recipientId
    users_resp = requests.get(USERS_URL, headers=headers, timeout=TIMEOUT)
    assert users_resp.status_code == 200, f"Get users failed: {users_resp.text}"
    users_data = users_resp.json()
    assert users_data.get("success") is True, f"Get users not successful: {users_data}"
    users_list = users_data.get("data", {}).get("content") or users_data.get("data")
    assert users_list, "Users list is empty or not found in response"
    
    # Obtain first user id
    recipient_id = None
    # users_list might be a list directly or wrapped inside "content" key in page data
    if isinstance(users_list, list) and users_list:
        # Try to get the first user id
        recipient_id = users_list[0].get("id") if isinstance(users_list[0], dict) else None
    
    assert recipient_id is not None, "No valid recipient user ID found"

    # Step 3: Construct valid notification payload
    # Provide non-blank strings for moduleCode and templateCode, uppercase channelHint list, priority one of HIGH/MEDIUM/LOW
    notification_payload = {
        "recipientId": recipient_id,
        "channelHint": ["EMAIL"],
        "templateCode": "TEST_TEMPLATE",
        "contextData": {},  # Empty context object
        "priority": "HIGH",
        "moduleCode": "TEST_MODULE",
        "referenceId": 123,
        "referenceType": "TEST_REFERENCE"
    }

    # Step 4: POST /api/v1/notifications/send with the payload
    response = requests.post(NOTIFICATION_SEND_URL, json=notification_payload, headers=headers, timeout=TIMEOUT)
    # The requirement says it returns 201 Created NOT 200
    assert response.status_code == 201, f"Unexpected status code: {response.status_code}, body: {response.text}"
    resp_json = response.json()
    assert resp_json.get("success") is True, f"Notification send failed: {resp_json}"
    data = resp_json.get("data")
    assert data is not None, "Response data missing"

    # Validate data as NotificationSendConfirmation containing a non-empty logEntryIds array
    log_entry_ids = data.get("logEntryIds")
    assert isinstance(log_entry_ids, list), "logEntryIds field missing or not a list"
    assert len(log_entry_ids) > 0, "logEntryIds list is empty"

test_send_notification_immediately()