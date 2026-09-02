import requests
from requests.auth import HTTPBasicAuth
import uuid

BASE_URL = "http://localhost:7272"
AUTH_ENDPOINT = "/api/auth/login"
REGION_ENDPOINT = "/api/v1/org/regions"
LOOKUP_ENDPOINT = "/api/lookups/REGION_TYPE"
HEADERS_JSON = {"Content-Type": "application/json"}
TIMEOUT = 30

def test_postapiv1orgregionscreateandverifyregiontype():
    # Authenticate and get JWT token
    auth_response = requests.post(
        f"{BASE_URL}/api/auth/login",
        json={"username": "admin", "password": "admin"},
        timeout=TIMEOUT
    )
    assert auth_response.status_code == 200
    auth_response_json = auth_response.json()
    assert auth_response_json.get("success") is True
    token = auth_response_json["data"]["accessToken"]
    auth_headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }

    # First, get a valid region type from lookup (REGION_TYPE)
    lookup_response = requests.get(f"{BASE_URL}{LOOKUP_ENDPOINT}", headers=auth_headers, timeout=TIMEOUT)
    assert lookup_response.status_code == 200
    lookup_resp_json = lookup_response.json()
    assert lookup_resp_json.get("success") is True
    region_types = lookup_resp_json.get("data")
    assert isinstance(region_types, list) and len(region_types) > 0

    # Use the first active region type id as regionTypeIdFk for the region
    region_type = next((rt for rt in region_types if rt.get("isActive", True) is True), region_types[0])
    region_type_id_fk = region_type.get("id")
    assert region_type_id_fk is not None, "No valid regionTypeIdFk found in REGION_TYPE lookup"

    # To create a region, legalEntityFk is required too; find a legalEntity to use:
    # We'll try to search for a legal entity to use for region creation
    legal_entity_id_fk = None
    legal_entity_search_response = requests.post(
        f"{BASE_URL}/api/v1/org/legal-entities/search",
        headers=auth_headers,
        json={},
        timeout=TIMEOUT
    )
    assert legal_entity_search_response.status_code == 200
    legal_entity_search_json = legal_entity_search_response.json()
    assert legal_entity_search_json.get("success") is True
    legal_entities = legal_entity_search_json.get("data", {}).get("content") if "content" in legal_entity_search_json.get("data", {}) else legal_entity_search_json.get("data")
    if isinstance(legal_entities, list) and len(legal_entities) > 0:
        # Use 'id' key only according to PRD
        legal_entity_id_fk = legal_entities[0].get("id")
    assert legal_entity_id_fk is not None, "No legalEntityFk found to create region"

    region_id = None
    created_region_name_ar = f"TestRegAr-{uuid.uuid4().hex[:6]}"
    created_region_name_en = f"TestRegEn-{uuid.uuid4().hex[:6]}"
    create_payload = {
        "legalEntityFk": legal_entity_id_fk,
        "regionTypeIdFk": region_type_id_fk,
        "nameAr": created_region_name_ar,
        "nameEn": created_region_name_en,
        "notes": "Created by automated TC001 test"
    }

    try:
        # Create the region
        create_response = requests.post(
            f"{BASE_URL}{REGION_ENDPOINT}",
            headers=auth_headers,
            json=create_payload,
            timeout=TIMEOUT
        )
        assert create_response.status_code == 200
        create_resp_json = create_response.json()
        assert create_resp_json.get("success") is True
        data = create_resp_json.get("data")
        assert data is not None
        region_id = data.get("id")
        assert region_id is not None
        # Verify the posted regionTypeIdFk matches
        assert data.get("regionTypeIdFk") == region_type_id_fk
        # Verify that the response includes the persisted region type information
        region_type_info = data.get("regionType")
        assert region_type_info is not None, "Response does not include region type information"
        # The region type info id must match the regionTypeIdFk used
        region_type_info_id = region_type_info.get("id")
        assert region_type_info_id == region_type_id_fk

        # Fetch the region by ID to verify round-trip
        get_response = requests.get(
            f"{BASE_URL}{REGION_ENDPOINT}/{region_id}",
            headers=auth_headers,
            timeout=TIMEOUT
        )
        assert get_response.status_code == 200
        get_resp_json = get_response.json()
        assert get_resp_json.get("success") is True
        get_data = get_resp_json.get("data")
        assert get_data is not None
        assert get_data.get("id") == region_id
        assert get_data.get("regionTypeIdFk") == region_type_id_fk
        get_region_type_info = get_data.get("regionType")
        assert get_region_type_info is not None
        get_region_type_info_id = get_region_type_info.get("id")
        assert get_region_type_info_id == region_type_id_fk
    finally:
        # Cleanup: delete the created region if it exists
        if region_id is not None:
            del_response = requests.delete(
                f"{BASE_URL}{REGION_ENDPOINT}/{region_id}",
                headers=auth_headers,
                timeout=TIMEOUT
            )
            # Some APIs may return 204 No Content or 200 with ApiResponse.success true
            if del_response.status_code == 204:
                pass
            elif del_response.status_code == 200:
                del_resp_json = del_response.json()
                assert del_resp_json.get("success") is True
            else:
                assert False, f"Failed to delete test region with id {region_id}, status: {del_response.status_code}"

test_postapiv1orgregionscreateandverifyregiontype()
