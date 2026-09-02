import requests

BASE_URL = "http://localhost:7272"
LOGIN_URL = f"{BASE_URL}/api/auth/login"
LEGAL_ENTITIES_URL = f"{BASE_URL}/api/v1/org/legal-entities"
TIMEOUT = 30

USERNAME = "admin"
PASSWORD = "admin"


def login():
    login_payload = {"username": USERNAME, "password": PASSWORD}
    headers = {"Content-Type": "application/json"}
    try:
        response = requests.post(LOGIN_URL, json=login_payload, headers=headers, timeout=TIMEOUT)
        response.raise_for_status()
        data = response.json()
        access_token = data.get("accessToken")
        assert access_token is not None, f"Login failed to return accessToken, response: {data}"
        return access_token
    except Exception as e:
        raise AssertionError(f"Login failed: {e}")


def test_post_api_v1_org_legal_entities_create():
    token = login()
    headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}

    # Sample valid payload for legal entity creation
    payload = {
        "nameAr": "كيان قانوني اختبار",
        "nameEn": "Test Legal Entity",
        "entityTypeId": 1,
        "notes": "Created by automated test TC007"
    }

    legal_entity_id = None
    try:
        # Create legal entity POST
        post_resp = requests.post(LEGAL_ENTITIES_URL, json=payload, headers=headers, timeout=TIMEOUT)
        assert post_resp.status_code == 200, f"Expected 200, got {post_resp.status_code}"
        resp_data = post_resp.json()

        # Check response fields
        legal_entity_id = resp_data.get("id")
        assert legal_entity_id is not None, "Response missing legal entity id"

        # Verify returned data matches input (at least names and entityTypeId)
        assert resp_data.get("nameAr") == payload["nameAr"], "nameAr mismatch in response"
        assert resp_data.get("nameEn") == payload["nameEn"], "nameEn mismatch in response"
        assert resp_data.get("entityTypeId") == payload["entityTypeId"], "entityTypeId mismatch in response"
        assert "notes" in resp_data, "Response missing notes field"

        # GET the created resource and verify same data
        get_resp = requests.get(f"{LEGAL_ENTITIES_URL}/{legal_entity_id}", headers=headers, timeout=TIMEOUT)
        assert get_resp.status_code == 200, f"Expected 200 on GET, got {get_resp.status_code}"
        get_data = get_resp.json()

        assert get_data.get("id") == legal_entity_id, "GET response id mismatch"
        assert get_data.get("nameAr") == payload["nameAr"], "nameAr mismatch on GET"
        assert get_data.get("nameEn") == payload["nameEn"], "nameEn mismatch on GET"
        assert get_data.get("entityTypeId") == payload["entityTypeId"], "entityTypeId mismatch on GET"

    finally:
        # Cleanup: delete the created legal entity if created
        if legal_entity_id:
            try:
                requests.delete(f"{LEGAL_ENTITIES_URL}/{legal_entity_id}", headers=headers, timeout=TIMEOUT)
            except Exception:
                pass


test_post_api_v1_org_legal_entities_create()
