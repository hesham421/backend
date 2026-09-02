import requests
from requests.auth import HTTPBasicAuth

base_url = "http://localhost:7272"
auth_credentials = ("admin", "admin")
timeout = 30

def test_put_api_v1_org_regions_id_update_and_verify_region_type():
    session = requests.Session()
    session.auth = HTTPBasicAuth(*auth_credentials)
    headers = {"Content-Type": "application/json"}

    # First, we need to create any required resources: a legal entity and a region type to create a region

    # 1. Create a legal entity (required to create a region)
    legal_entity_payload = {
        "nameAr": "TestLegalEntityAr",
        "nameEn": "TestLegalEntityEn",
        "entityTypeId": 1,
        "notes": "For region update test"
    }
    res = session.post(f"{base_url}/api/v1/org/legal-entities", json=legal_entity_payload, headers=headers, timeout=timeout)
    assert res.status_code == 200
    data = res.json()
    assert data["success"]
    legal_entity = data["data"]
    legal_entity_id = legal_entity["id"]

    # 2. Get an existing active region type from lookups (lookupCode=REGION_TYPE)
    res = session.get(f"{base_url}/api/lookups/REGION_TYPE", headers=headers, timeout=timeout)
    assert res.status_code == 200
    data = res.json()
    assert data["success"]
    region_types = data["data"]
    assert isinstance(region_types, list) and len(region_types) > 0
    region_type = region_types[0]
    region_type_id_fk = region_type.get("id")
    assert region_type_id_fk is not None

    # 3. Create a region with the legalEntityFk and regionTypeIdFk
    region_create_payload = {
        "legalEntityFk": legal_entity_id,
        "regionTypeIdFk": region_type_id_fk,
        "nameAr": "TestRegionAr",
        "nameEn": "TestRegionEn",
        "notes": "Initial region for update test"
    }
    res = session.post(f"{base_url}/api/v1/org/regions", json=region_create_payload, headers=headers, timeout=timeout)
    assert res.status_code == 200
    data = res.json()
    assert data["success"]
    region = data["data"]
    region_id = region["id"]
    assert region["regionTypeIdFk"] == region_type_id_fk
    # The returned region data must include region type info; check presence of any nested regionType info keys
    assert "regionTypeIdFk" in region
    # Optional: Check that nested region type data is consistent if present
    if "regionType" in region:
        assert region["regionType"].get("id") == region_type_id_fk

    try:
        # 4. Update the region's attributes, e.g. change nameEn and notes but keep regionTypeIdFk unchanged
        updated_name_en = "UpdatedRegionNameEn"
        updated_notes = "Updated notes for region test"
        update_payload = {
            "legalEntityFk": legal_entity_id,  # Usually immutable but include for completeness
            "regionTypeIdFk": region_type_id_fk,
            "nameAr": region["nameAr"],
            "nameEn": updated_name_en,
            "notes": updated_notes
        }
        res = session.put(f"{base_url}/api/v1/org/regions/{region_id}", json=update_payload, headers=headers, timeout=timeout)
        assert res.status_code == 200
        data = res.json()
        assert data["success"]
        updated_region = data["data"]
        # Verify updated attributes
        assert updated_region["nameEn"] == updated_name_en
        assert updated_region["notes"] == updated_notes
        # Verify regionTypeIdFk remains same
        assert updated_region["regionTypeIdFk"] == region_type_id_fk
        if "regionType" in updated_region:
            assert updated_region["regionType"].get("id") == region_type_id_fk

        # 5. GET the region by id and verify consistency of regionTypeIdFk and related regionType info
        res = session.get(f"{base_url}/api/v1/org/regions/{region_id}", headers=headers, timeout=timeout)
        assert res.status_code == 200
        data = res.json()
        assert data["success"]
        region_get = data["data"]
        # All relevant values should match
        assert region_get["id"] == region_id
        assert region_get["regionTypeIdFk"] == region_type_id_fk
        assert region_get["nameEn"] == updated_name_en
        assert region_get["notes"] == updated_notes
        if "regionType" in region_get:
            assert region_get["regionType"].get("id") == region_type_id_fk

    finally:
        # Clean up: delete the created region and legal entity
        # Delete region
        del_res = session.delete(f"{base_url}/api/v1/org/regions/{region_id}", headers=headers, timeout=timeout)
        # It might either be 204 No Content or 200 with ApiResponse, accept success codes only, ignore error to not break test
        if del_res.status_code not in [200, 204]:
            pass

        # Delete legal entity
        del_res = session.delete(f"{base_url}/api/v1/org/legal-entities/{legal_entity_id}", headers=headers, timeout=timeout)
        if del_res.status_code not in [200, 204]:
            pass

test_put_api_v1_org_regions_id_update_and_verify_region_type()
