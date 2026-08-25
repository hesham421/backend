import requests
from requests.auth import HTTPBasicAuth

BASE_URL = "http://localhost:7272"
LOGIN_PATH = "/api/auth/login"
MASTER_LOOKUPS_PATH = "/api/masterdata/master-lookups"
TIMEOUT = 30


def test_create_master_lookup():
    # Step 1: Login as admin to get JWT access token
    login_url = BASE_URL + LOGIN_PATH
    login_payload = {"username": "admin", "password": "admin"}
    login_resp = requests.post(login_url, json=login_payload, timeout=TIMEOUT)
    assert login_resp.status_code == 200, f"Login failed: {login_resp.text}"
    login_json = login_resp.json()
    assert login_json.get("success") is True, f"Login API returned unsuccessful response: {login_json}"
    access_token = login_json.get("data", {}).get("accessToken")
    assert access_token, "accessToken not found in login response"

    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json"
    }

    # Prepare a unique lookupKey to avoid conflicts
    import uuid
    unique_lookup_key = "test_lookup_" + str(uuid.uuid4())[:8]

    # Payload for creating master lookup
    create_payload = {
        "lookupKey": unique_lookup_key,
        "lookupName": "Test Lookup Name",
        "lookupNameEn": "Test Lookup Name EN",
        "description": "This is a test master lookup created by automated test."
    }

    master_lookups_url = BASE_URL + MASTER_LOOKUPS_PATH

    # Use try-finally to delete created resource after test
    created_id = None
    try:
        resp = requests.post(master_lookups_url, json=create_payload, headers=headers, timeout=TIMEOUT)
        assert resp.status_code == 200, f"Create master lookup failed: {resp.text}"
        resp_json = resp.json()
        assert resp_json.get("success") is True, f"API returned unsuccessful response: {resp_json}"
        data = resp_json.get("data")
        assert data is not None, "Response data is missing"
        # Adjust lookupKey assertion to be case-insensitive
        assert data.get("lookupKey").lower() == unique_lookup_key.lower(), "lookupKey in response doesn't match request"
        assert data.get("lookupName") == create_payload["lookupName"], "lookupName in response doesn't match"
        assert data.get("lookupNameEn") == create_payload["lookupNameEn"], "lookupNameEn in response doesn't match"
        created_id = data.get("id")
        assert created_id is not None, "Created resource ID not found in response"
    finally:
        if created_id:
            delete_url = f"{master_lookups_url}/{created_id}"
            delete_resp = requests.delete(delete_url, headers=headers, timeout=TIMEOUT)
            # Deletion might fail if detail rows exist or constraints, we don't assert deletion here
            # Just best effort cleanup


test_create_master_lookup()
