import requests
import uuid

BASE_URL = "http://localhost:7272"
LOGIN_PATH = "/api/auth/login"
LEGAL_ENTITIES_PATH = "/api/v1/org/legal-entities"
TIMEOUT = 30

def test_create_legal_entity():
    # Step 1: Authenticate to get JWT access token
    login_url = BASE_URL + LOGIN_PATH
    login_payload = {
        "username": "admin",
        "password": "admin"
    }
    login_headers = {
        "Content-Type": "application/json"
    }
    try:
        login_resp = requests.post(login_url, json=login_payload, headers=login_headers, timeout=TIMEOUT)
        login_resp.raise_for_status()
    except requests.RequestException as e:
        assert False, f"Login request failed: {e}"
    login_json = login_resp.json()
    assert login_json.get("success") is True, f"Login failed: {login_json}"
    access_token = login_json.get("data", {}).get("accessToken")
    assert access_token, "Login response missing accessToken"

    # Prepare Authorization header for further requests
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {access_token}"
    }

    # Generate a unique suffix for nameEn and nameAr with uuid4 hex
    unique_suffix = uuid.uuid4().hex

    # Legal entity data with unique fields for nameEn and nameAr
    legal_entity_payload = {
        "nameEn": f"TestLegalEntityEn_{unique_suffix}",
        "nameAr": f"TestLegalEntityAr_{unique_suffix}",
        "entityTypeId": 1,  # Assuming entityTypeId 1 is valid; adjust if needed
        "notes": "Automated test creation of legal entity"
    }

    legal_entity_url = BASE_URL + LEGAL_ENTITIES_PATH

    # Step 2: Create legal entity with POST, expect 201 Created
    try:
        create_resp = requests.post(legal_entity_url, json=legal_entity_payload, headers=headers, timeout=TIMEOUT)
    except requests.RequestException as e:
        assert False, f"Create legal entity request failed: {e}"

    if create_resp.status_code == 409:
        # Conflict error: nameEn or nameAr duplicate (should not happen due to unique suffix)
        assert False, f"Conflict error (409) received due to nameEn or nameAr duplication: {create_resp.text}"

    # Assert status code is 201 Created, not 200
    assert create_resp.status_code == 201, f"Expected 201 Created, got {create_resp.status_code}: {create_resp.text}"

    try:
        create_json = create_resp.json()
    except Exception as e:
        assert False, f"Response is not JSON or invalid JSON: {e}"

    # Assert success true in ApiResponse
    assert create_json.get("success") is True, f"API failure: {create_json}"

    # Validate data object exists and has expected LegalEntityResponse fields
    data = create_json.get("data")
    assert isinstance(data, dict), "Response data is not an object"

    # Validate returned fields (at least id and names)
    legal_entity_id = data.get("id")
    assert legal_entity_id is not None, "Response data missing 'id'"
    returned_name_en = data.get("nameEn")
    returned_name_ar = data.get("nameAr")

    # Check returned names equal to the input values
    assert returned_name_en == legal_entity_payload["nameEn"], "Returned nameEn mismatch"
    assert returned_name_ar == legal_entity_payload["nameAr"], "Returned nameAr mismatch"

    # Optional: Validate notes match or exist
    assert data.get("notes") == legal_entity_payload["notes"], "notes mismatch"

test_create_legal_entity()
