import requests
import uuid

BASE_URL = "http://localhost:7272"
AUTH_USERNAME = "admin"
AUTH_PASSWORD = "admin"
TIMEOUT = 30

def test_postapi_v1_org_regions_getregionbyid():
    """
    Verify GET /api/v1/org/regions/{id} returns 200 with the region data including the persisted region type information.
    Focus on round-tripping regionTypeId correctly in create and subsequent GET.
    """
    session = requests.Session()
    headers = {"Content-Type": "application/json"}

    # Authenticate to get JWT bearer token
    auth_payload = {"username": AUTH_USERNAME, "password": AUTH_PASSWORD}
    auth_resp = session.post(f"{BASE_URL}/api/auth/login", json=auth_payload, headers=headers, timeout=TIMEOUT)
    assert auth_resp.status_code == 200, f"Authentication failed with status {auth_resp.status_code}"
    auth_json = auth_resp.json()
    access_token = auth_json.get("accessToken")
    assert access_token, "accessToken not found in login response"
    headers["Authorization"] = f"Bearer {access_token}"

    # Helper to create a legal entity (required for region)
    def create_legal_entity():
        legal_entity_payload = {
            "nameAr": "LegalEntity-Ar-" + str(uuid.uuid4())[:8],
            "nameEn": "LegalEntity-En-" + str(uuid.uuid4())[:8],
            "entityTypeId": 1,  # Assuming 1 is valid entityTypeId
            "notes": "Test legal entity for region creation"
        }
        resp = session.post(f"{BASE_URL}/api/v1/org/legal-entities", json=legal_entity_payload, headers=headers, timeout=TIMEOUT)
        resp.raise_for_status()
        data = resp.json()
        return data.get("id") or data.get("legalEntityPk") or data.get("legalEntityId")

    # Helper to get at least one valid region type id from lookups (REGION_TYPE)
    def get_region_type_id():
        resp = session.get(f"{BASE_URL}/api/lookups/REGION_TYPE", headers=headers, timeout=TIMEOUT)
        resp.raise_for_status()
        values = resp.json()
        # Expecting a list of active region types
        if isinstance(values, list) and values:
            return values[0].get("id") or values[0].get("lookupDetailId")
        else:
            raise Exception("No regionTypeId found in REGION_TYPE lookup")

    region_id = None
    legal_entity_id = None
    region_type_id = None

    try:
        # 1. Create legal entity required for region
        legal_entity_id = create_legal_entity()
        assert legal_entity_id is not None, "Failed to create legal entity for region"

        # 2. Get a valid regionTypeIdFk
        region_type_id = get_region_type_id()
        assert region_type_id is not None, "Failed to obtain regionTypeId from REGION_TYPE lookup"

        # 3. Create a region with the legalEntityFk and regionTypeIdFk
        unique_suffix = str(uuid.uuid4())[:8]
        region_payload = {
            "legalEntityFk": legal_entity_id,
            "regionTypeIdFk": region_type_id,
            "nameAr": "Region-Ar-" + unique_suffix,
            "nameEn": "Region-En-" + unique_suffix,
            "notes": "Test region creation with regionTypeId round trip"
        }
        create_resp = session.post(f"{BASE_URL}/api/v1/org/regions", json=region_payload, headers=headers, timeout=TIMEOUT)
        assert create_resp.status_code == 200, f"Expected 200 on create region, got {create_resp.status_code}"
        create_data = create_resp.json()
        region_id = create_data.get("id") or create_data.get("regionPk") or create_data.get("regionId")
        assert region_id is not None, "Created region response missing id"
        # Validate regionTypeId round-trip on create response
        assert create_data.get("regionTypeIdFk") == region_type_id or create_data.get("regionTypeId") == region_type_id, \
            "regionTypeIdFk not round-tripped in create response"

        # 4. GET the created region by id
        get_resp = session.get(f"{BASE_URL}/api/v1/org/regions/{region_id}", headers=headers, timeout=TIMEOUT)
        assert get_resp.status_code == 200, f"Expected 200 on get region by id, got {get_resp.status_code}"
        get_data = get_resp.json()
        # Validate regionTypeId presence and correctness
        assert get_data.get("regionTypeIdFk") == region_type_id or get_data.get("regionTypeId") == region_type_id, \
            "regionTypeIdFk not present or does not match in get region by id response"
        assert get_data.get("legalEntityFk") == legal_entity_id, "legalEntityFk does not match in get region response"
        assert get_data.get("nameAr") == region_payload["nameAr"], "nameAr mismatch in get region response"
        assert get_data.get("nameEn") == region_payload["nameEn"], "nameEn mismatch in get region response"

        # 5. Search regions filtered by legalEntityFk and regionTypeIdFk to verify round-trip on search endpoint optional (if needed)
        search_payload = {
            "legalEntityFk": legal_entity_id,
            "regionTypeIdFk": region_type_id,
            "page": 0,
            "size": 10
        }
        search_resp = session.post(f"{BASE_URL}/api/v1/org/regions/search", json=search_payload, headers=headers, timeout=TIMEOUT)
        assert search_resp.status_code == 200, f"Expected 200 on search regions, got {search_resp.status_code}"
        search_data = search_resp.json()
        items = search_data.get("content") or search_data.get("regions") or []
        found = False
        for item in items:
            if item.get("id") == region_id or item.get("regionPk") == region_id:
                found = True
                assert item.get("regionTypeIdFk") == region_type_id or item.get("regionTypeId") == region_type_id, \
                    "regionTypeIdFk does not match in search result"
                break
        assert found, "Created region not found in search results"

    finally:
        # Cleanup: delete the created region and legal entity if applicable
        if region_id is not None:
            session.delete(f"{BASE_URL}/api/v1/org/regions/{region_id}", headers=headers, timeout=TIMEOUT)
        if legal_entity_id is not None:
            # Deactivate legal entity before delete if required (depends on API rules)
            try:
                session.put(f"{BASE_URL}/api/v1/org/legal-entities/{legal_entity_id}/deactivate", headers=headers, timeout=TIMEOUT)
            except Exception:
                pass
            session.delete(f"{BASE_URL}/api/v1/org/legal-entities/{legal_entity_id}", headers=headers, timeout=TIMEOUT)

test_postapi_v1_org_regions_getregionbyid()
