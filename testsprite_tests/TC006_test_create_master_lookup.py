import requests
import uuid

BASE_URL = "http://localhost:7272"
LOGIN_ENDPOINT = "/api/auth/login"
MASTER_LOOKUPS_ENDPOINT = "/api/masterdata/master-lookups"

USERNAME = "admin"
PASSWORD = "admin"
TIMEOUT = 30

def test_create_master_lookup():
    # Step 1: Authenticate to get JWT bearer token
    login_url = BASE_URL + LOGIN_ENDPOINT
    auth_payload = {"username": USERNAME, "password": PASSWORD}
    login_resp = requests.post(login_url, json=auth_payload, timeout=TIMEOUT)
    assert login_resp.status_code == 200, f"Login failed: {login_resp.text}"
    login_json = login_resp.json()
    assert login_json.get("success") is True, f"Login success flag false: {login_json}"
    access_token = login_json.get("data", {}).get("accessToken")
    assert access_token and isinstance(access_token, str), "No accessToken in login response"

    # Step 2: Prepare unique lookupKey and names
    unique_suffix = uuid.uuid4().hex[:8]
    lookup_key_raw = f"testLookupKey_{unique_suffix}"
    lookup_key_upper = lookup_key_raw.upper()
    lookup_name = f"Test Lookup Name {unique_suffix}"
    lookup_name_en = f"Test Lookup Name En {unique_suffix}"
    description = "Test Description for master lookup creation"

    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json"
    }

    url = BASE_URL + MASTER_LOOKUPS_ENDPOINT
    payload = {
        "lookupKey": lookup_key_raw,
        "lookupName": lookup_name,
        "lookupNameEn": lookup_name_en,
        "description": description
    }

    # Step 3: Send POST request to create master lookup
    resp = requests.post(url, json=payload, headers=headers, timeout=TIMEOUT)

    # Step 4: Assert response status code is 201 Created
    assert resp.status_code == 201, f"Expected 201 Created, got {resp.status_code}, body={resp.text}"

    # Step 5: Assert response JSON follows ApiResponse envelope and data fields
    resp_json = resp.json()
    assert resp_json.get("success") is True, f"API success flag false: {resp_json}"
    data = resp_json.get("data")
    assert data is not None, "Response data is missing"

    # Step 6: Assert returned lookupKey equals input lookupKey uppercased
    returned_lookup_key = data.get("lookupKey")
    assert returned_lookup_key == lookup_key_upper, (
        f"Returned lookupKey '{returned_lookup_key}' does not match uppercased input '{lookup_key_upper}'"
    )

    # Step 7: Assert other returned fields match input loosely (lookupName, lookupNameEn, description)
    assert data.get("lookupName") == lookup_name
    assert data.get("lookupNameEn") == lookup_name_en
    assert "description" in data and isinstance(data["description"], str)

# Execute test function
test_create_master_lookup()