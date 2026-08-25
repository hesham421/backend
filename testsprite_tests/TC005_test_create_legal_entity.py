import requests

BASE_URL = "http://localhost:7272/actuator/health"
LOGIN_URL = BASE_URL.replace('/actuator/health', '') + "/api/auth/login"
LEGAL_ENTITY_URL = BASE_URL.replace('/actuator/health', '') + "/api/v1/org/legal-entities"
TIMEOUT = 30

USERNAME = "admin"
PASSWORD = "admin"

def test_create_legal_entity():
    # Login to get JWT token
    login_payload = {
        "username": USERNAME,
        "password": PASSWORD
    }
    try:
        login_resp = requests.post(
            LOGIN_URL,
            json=login_payload,
            timeout=TIMEOUT
        )
        login_resp.raise_for_status()
    except Exception as e:
        assert False, f"Login request failed: {e}"
    login_json = login_resp.json()
    assert login_json.get("success") is True, f"Login failed: {login_json}"
    access_token = login_json.get("data", {}).get("accessToken")
    assert access_token, "Login response missing accessToken"

    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json"
    }

    # Prepare payload for creating legal entity
    legal_entity_payload = {
        "nameAr": "كيان قانوني اختبار",
        "nameEn": "Test Legal Entity",
        "entityTypeId": 1,
        "notes": "Created by automated test case TC005"
    }

    created_entity_id = None
    try:
        resp = requests.post(
            LEGAL_ENTITY_URL,
            json=legal_entity_payload,
            headers=headers,
            timeout=TIMEOUT
        )
        resp.raise_for_status()
    except Exception as e:
        assert False, f"Create legal entity request failed: {e}"

    resp_json = resp.json()
    assert resp_json.get("success") is True, f"API returned failure: {resp_json}"
    data = resp_json.get("data")
    assert data, "Response missing data"
    # Validate that returned data has expected fields (at least id and nameEn)
    assert "id" in data, "LegalEntityResponse missing 'id'"
    assert data.get("nameEn") == legal_entity_payload["nameEn"], "nameEn does not match"
    created_entity_id = data.get("id")
    # Optionally check other fields if present
    assert data.get("nameAr") == legal_entity_payload["nameAr"], "nameAr does not match"
    assert data.get("entityTypeId") == legal_entity_payload["entityTypeId"], "entityTypeId does not match"
    assert "notes" in data  # notes might be returned or not, just check presence anyway

    # Cleanup: deactivate legal entity to keep environment clean if applicable
    if created_entity_id is not None:
        try:
            deactivate_url = f"{LEGAL_ENTITY_URL}/{created_entity_id}/deactivate"
            deactivate_resp = requests.put(deactivate_url, headers=headers, timeout=TIMEOUT)
            if deactivate_resp.status_code != 200:
                # Not critical but report if deactivate fails
                print(f"Warning: Failed to deactivate legal entity with id {created_entity_id}")
        except Exception:
            # Suppress exception in cleanup
            pass

test_create_legal_entity()
