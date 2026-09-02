import requests
from requests.auth import HTTPBasicAuth

BASE_URL = "http://localhost:7272"
LOGIN_PATH = "/api/auth/login"
REGIONS_PATH = "/api/v1/org/regions"
REGIONS_DEACTIVATE_SUFFIX = "/deactivate"
REGIONS_SEARCH_PATH = "/api/v1/org/regions/search"

AUTH_CREDENTIALS = {"username": "admin", "password": "admin"}


def authenticate():
    response = requests.post(
        BASE_URL + LOGIN_PATH,
        json={"username": AUTH_CREDENTIALS["username"], "password": AUTH_CREDENTIALS["password"]},
        timeout=30,
    )
    response.raise_for_status()
    token = response.json()["data"]["accessToken"]
    return token


def create_required_resources(headers):
    # To create a region, we need a valid legalEntityFk and regionTypeIdFk.
    # So we create or get a legal entity and a region type lookup first.

    # 1. Create or find a legal entity
    legal_entity_payload = {
        "nameAr": "Test Legal Entity AR",
        "nameEn": "Test Legal Entity EN",
        "entityTypeId": 1,
        "notes": "Test legal entity for region creation",
    }
    legal_entity_resp = requests.post(
        BASE_URL + "/api/v1/org/legal-entities",
        headers=headers,
        json=legal_entity_payload,
        timeout=30,
    )
    legal_entity_resp.raise_for_status()
    legal_entity = legal_entity_resp.json()["data"]
    legal_entity_id = legal_entity["id"]

    # 2. Retrieve REGION_TYPE lookup to get at least one regionTypeIdFk
    lookup_resp = requests.get(
        BASE_URL + "/api/lookups/REGION_TYPE",
        headers=headers,
        timeout=30,
    )
    lookup_resp.raise_for_status()
    region_types = lookup_resp.json()["data"]
    if not region_types:
        raise Exception("No region types found in lookup REGION_TYPE")
    region_type = region_types[0]
    region_type_id_fk = region_type["id"]

    # 3. Create a region to test deactivation on
    region_payload = {
        "legalEntityFk": legal_entity_id,
        "regionTypeIdFk": region_type_id_fk,
        "nameAr": "Test Region AR",
        "nameEn": "Test Region EN",
        "notes": "Test region creation for deactivation business rule",
    }
    region_resp = requests.post(
        BASE_URL + REGIONS_PATH,
        headers=headers,
        json=region_payload,
        timeout=30,
    )
    region_resp.raise_for_status()
    region = region_resp.json()["data"]
    region_id = region["id"]

    return legal_entity_id, region_type_id_fk, region_id


def test_putapiv1orgregionsiddeactivatewithbusinessrulecheck():
    # Authenticate and get Bearer token
    token = authenticate()
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json",
        "Accept": "application/json",
    }

    legal_entity_id = None
    region_type_id_fk = None
    region_id = None

    # Create legal entity, region type, and region resource to test
    legal_entity_id, region_type_id_fk, region_id = create_required_resources(headers)

    try:
        # Attempt to deactivate the region expecting a business rule rejection error
        deactivate_url = f"{BASE_URL}{REGIONS_PATH}/{region_id}/deactivate"
        deactivate_resp = requests.put(deactivate_url, headers=headers, timeout=30)

        # API returns 200 even for business rule error with success=false in ApiResponse envelope
        json_resp = deactivate_resp.json()
        assert not json_resp["success"], "Expected success=false due to business rule rejection"

        data = json_resp.get("data", {})
        message = json_resp.get("message", "")

        # Validate error message or business rule details present in message or data
        assert ("business-rule" in message.lower() or "referenced" in message.lower() or "rejection" in message.lower()) or (
            isinstance(data, dict) and (
                "businessRuleViolation" in data or "referenced" in data or "conflict" in data or "error" in data
            )
        ), f"Expected business rule rejection details in response message or data, got message: {message}, data: {data}"

        # GET region to verify regionTypeId round-tripping and isActive not flipped to false
        get_region_url = f"{BASE_URL}{REGIONS_PATH}/{region_id}"
        get_resp = requests.get(get_region_url, headers=headers, timeout=30)
        get_resp.raise_for_status()
        region_data = get_resp.json()["data"]

        # Validate regionTypeId round-tripped correctly
        assert region_data.get("regionTypeIdFk") == region_type_id_fk, "regionTypeIdFk mismatch after deactivation attempt"

        # Validate isActive flag remains true (not flipped to false due to rejection)
        assert region_data.get("isActive", True) is True, "isActive incorrectly flipped to false after failed deactivate"

    finally:
        # Cleanup: delete the created region and legal entity

        # Delete region (assuming DELETE endpoint exists)
        try:
            requests.delete(f"{BASE_URL}{REGIONS_PATH}/{region_id}", headers=headers, timeout=30)
        except Exception:
            pass

        # Delete legal entity (assuming DELETE endpoint exists)
        if legal_entity_id:
            try:
                requests.delete(f"{BASE_URL}/api/v1/org/legal-entities/{legal_entity_id}", headers=headers, timeout=30)
            except Exception:
                pass


test_putapiv1orgregionsiddeactivatewithbusinessrulecheck()