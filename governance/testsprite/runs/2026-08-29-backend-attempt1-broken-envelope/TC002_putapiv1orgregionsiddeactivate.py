import requests

BASE_URL = "http://localhost:7272"
JWT_TOKEN = "YOUR_VALID_JWT_TOKEN"  # Replace with a valid JWT token for authentication
TIMEOUT = 30
HEADERS = {
    'Content-Type': 'application/json',
    'Authorization': f'Bearer {JWT_TOKEN}'
}

def test_put_api_v1_org_regions_id_deactivate_rejects_when_active_branches_exist():
    # Step 1: Create a legal entity (required for branch and region)
    legal_entity_payload = {
        "nameAr": "TestLegalEntAr",
        "nameEn": "TestLegalEntEn",
        "entityTypeId": 1,
        "notes": "Test legal entity for region/branch test"
    }
    legal_entity_id = None
    region_id = None
    branch_id = None
    try:
        resp = requests.post(f"{BASE_URL}/api/v1/org/legal-entities", json=legal_entity_payload, headers=HEADERS, timeout=TIMEOUT)
        assert resp.status_code == 200, f"Failed to create legal entity: {resp.text}"
        legal_entity_id = resp.json().get("id")
        assert legal_entity_id is not None, "Legal entity ID not returned"

        # Step 2: Get valid regionTypeId from lookup to ensure valid regionTypeIdFk for region creation
        lookup_resp = requests.get(f"{BASE_URL}/api/lookups/REGION_TYPE", headers=HEADERS, timeout=TIMEOUT)
        assert lookup_resp.status_code == 200, f"Failed getting REGION_TYPE lookup: {lookup_resp.text}"
        region_types = lookup_resp.json()
        assert isinstance(region_types, list) and len(region_types) > 0, "No region types found"
        region_type_id_fk = region_types[0].get("id") or region_types[0].get("code") or region_types[0].get("value") or region_types[0].get("key") or None
        assert region_type_id_fk is not None, "No valid regionTypeIdFk in REGION_TYPE lookup"

        # Step 3: Create the region with legalEntityFk and regionTypeIdFk
        region_payload = {
            "legalEntityFk": legal_entity_id,
            "regionTypeIdFk": region_type_id_fk,
            "nameAr": "TestRegionAr",
            "nameEn": "TestRegionEn",
            "notes": "Test region for deactivation rule"
        }
        resp = requests.post(f"{BASE_URL}/api/v1/org/regions", json=region_payload, headers=HEADERS, timeout=TIMEOUT)
        assert resp.status_code == 200, f"Failed to create region: {resp.text}"
        region = resp.json()
        region_id = region.get("id")
        assert region_id is not None, "Region ID not returned"
        # Verify round-trip of regionTypeIdFk
        returned_regionTypeIdFk = region.get("regionTypeIdFk") or region.get("regionTypeId") or region.get("regionType", {}).get("id") or region.get("regionType", {}).get("regionTypeIdFk")
        assert returned_regionTypeIdFk == region_type_id_fk, f"regionTypeIdFk mismatch on creation response. Expected: {region_type_id_fk}, Got: {returned_regionTypeIdFk}"

        # Step 4: Create a branch referencing this region, activate it to force dependency
        # Need to get branchTypeId from lookup
        branch_type_resp = requests.get(f"{BASE_URL}/api/lookups/BRANCH_TYPE", headers=HEADERS, timeout=TIMEOUT)
        assert branch_type_resp.status_code == 200, f"Failed getting BRANCH_TYPE lookup: {branch_type_resp.text}"
        branch_types = branch_type_resp.json()
        assert isinstance(branch_types, list) and len(branch_types) > 0, "No branch types found"
        branch_type_id = branch_types[0].get("id") or branch_types[0].get("code") or branch_types[0].get("value") or branch_types[0].get("key") or None
        assert branch_type_id is not None, "No valid branchTypeId in BRANCH_TYPE lookup"

        # Branch payload with reference to region
        branch_payload = {
            "legalEntityFk": legal_entity_id,
            "regionFk": region_id,
            "branchTypeId": branch_type_id,
            "nameAr": "TestBranchAr",
            "nameEn": "TestBranchEn",
            "notes": "Test branch referencing region for deactivation test"
        }
        resp = requests.post(f"{BASE_URL}/api/v1/org/branches", json=branch_payload, headers=HEADERS, timeout=TIMEOUT)
        assert resp.status_code == 200, f"Failed to create branch: {resp.text}"
        branch = resp.json()
        branch_id = branch.get("id")
        assert branch_id is not None, "Branch ID not returned"

        # Step 5: Activate the branch (if not auto-active)
        if branch.get("isActive") is False:
            activate_resp = requests.put(f"{BASE_URL}/api/v1/org/branches/{branch_id}/activate", headers=HEADERS, timeout=TIMEOUT)
            assert activate_resp.status_code == 200, f"Failed to activate branch: {activate_resp.text}"

        # Step 6: Attempt to deactivate the region - expect rejection with error (business rule)
        deactivate_resp = requests.put(f"{BASE_URL}/api/v1/org/regions/{region_id}/deactivate", headers=HEADERS, timeout=TIMEOUT)

        # Expect error status code (likely 409 Conflict or 400 Bad Request)
        assert deactivate_resp.status_code in (400, 409), f"Expected error on deactivating region with active branch referencing it, got status {deactivate_resp.status_code}"

        # Validate error response contains business-rule rejection indication
        error_json = None
        try:
            error_json = deactivate_resp.json()
        except Exception:
            error_json = None
        assert error_json and ("error" in error_json or "message" in error_json or "details" in error_json), "Error response does not contain expected error details"

    finally:
        # Cleanup: Delete branch, region, legal entity if created
        if branch_id:
            try:
                requests.delete(f"{BASE_URL}/api/v1/org/branches/{branch_id}", headers=HEADERS, timeout=TIMEOUT)
            except Exception:
                pass
        if region_id:
            try:
                requests.delete(f"{BASE_URL}/api/v1/org/regions/{region_id}", headers=HEADERS, timeout=TIMEOUT)
            except Exception:
                pass
        if legal_entity_id:
            try:
                requests.delete(f"{BASE_URL}/api/v1/org/legal-entities/{legal_entity_id}", headers=HEADERS, timeout=TIMEOUT)
            except Exception:
                pass

test_put_api_v1_org_regions_id_deactivate_rejects_when_active_branches_exist()
