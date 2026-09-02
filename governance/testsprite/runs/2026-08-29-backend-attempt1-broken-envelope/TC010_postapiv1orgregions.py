import requests
import uuid

BASE_URL = "http://localhost:7272"
AUTH_USERNAME = "admin"
AUTH_PASSWORD = "admin"
TIMEOUT = 30


def test_post_api_v1_org_regions_round_trip_and_deactivate_department():
    # Step 0: Obtain JWT token via login
    login_payload = {
        "username": AUTH_USERNAME,
        "password": AUTH_PASSWORD
    }
    login_resp = requests.post(
        f"{BASE_URL}/api/auth/login",
        json=login_payload,
        headers={"Accept": "application/json"},
        timeout=TIMEOUT
    )
    assert login_resp.status_code == 200, f"Login failed: {login_resp.text}"
    login_data = login_resp.json()
    access_token = login_data.get("accessToken")
    assert access_token, f"Login response missing accessToken, response keys: {list(login_data.keys())}"

    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {access_token}"
    }

    legal_entity_id = None
    branch_id = None
    region_id = None
    department_id = None
    try:
        # Create Legal Entity
        legal_entity_payload = {
            "nameAr": f"TestLegalEntityAr-{uuid.uuid4()}",
            "nameEn": f"TestLegalEntityEn-{uuid.uuid4()}",
            "entityTypeId": 1,
            "notes": "Test legal entity for region creation"
        }
        le_resp = requests.post(
            f"{BASE_URL}/api/v1/org/legal-entities",
            json=legal_entity_payload,
            headers=headers,
            timeout=TIMEOUT,
        )
        assert le_resp.status_code == 200, f"LegalEntity creation failed: {le_resp.text}"
        le_data = le_resp.json()
        legal_entity_id = le_data.get("id")
        assert legal_entity_id is not None, "LegalEntityResponse missing id"

        # Get region types from master lookup REGION_TYPE for valid regionTypeIdFk
        lookup_resp = requests.get(
            f"{BASE_URL}/api/lookups/REGION_TYPE",
            headers=headers,
            timeout=TIMEOUT,
        )
        assert lookup_resp.status_code == 200, f"Lookup REGION_TYPE failed: {lookup_resp.text}"
        lookup_data = lookup_resp.json()
        region_types = lookup_data if isinstance(lookup_data, list) else lookup_data.get("items", [])
        assert region_types, "No REGION_TYPE lookup data found"
        region_type = region_types[0]
        region_type_id_fk = region_type.get("id")
        assert region_type_id_fk is not None, "Region type id missing"

        # Create Branch under legal entity (needed for department)
        branch_payload = {
            "legalEntityFk": legal_entity_id,
            "nameAr": f"TestBranchAr-{uuid.uuid4()}",
            "nameEn": f"TestBranchEn-{uuid.uuid4()}",
            "branchTypeId": 1,
            "notes": "Test branch for region and department"
        }
        branch_resp = requests.post(
            f"{BASE_URL}/api/v1/org/branches",
            json=branch_payload,
            headers=headers,
            timeout=TIMEOUT,
        )
        assert branch_resp.status_code == 200, f"Branch creation failed: {branch_resp.text}"
        branch_data = branch_resp.json()
        branch_id = branch_data.get("id")
        assert branch_id is not None, "BranchResponse missing id"

        # POST /api/v1/org/regions with legalEntityFk, regionTypeIdFk, nameAr, nameEn, notes.
        region_payload = {
            "legalEntityFk": legal_entity_id,
            "regionTypeIdFk": region_type_id_fk,
            "nameAr": f"TestRegionAr-{uuid.uuid4()}",
            "nameEn": f"TestRegionEn-{uuid.uuid4()}",
            "notes": "Region created by TC010 test"
        }
        region_resp = requests.post(
            f"{BASE_URL}/api/v1/org/regions",
            json=region_payload,
            headers=headers,
            timeout=TIMEOUT,
        )
        assert region_resp.status_code == 200, f"Region creation failed: {region_resp.text}"
        region_data = region_resp.json()
        region_id = region_data.get("id")
        assert region_id is not None, "RegionResponse missing id"
        # Verify returned regionTypeId matches regionTypeIdFk sent
        returned_region_type = region_data.get("regionType")
        assert returned_region_type, "RegionResponse missing regionType"
        returned_region_type_id = returned_region_type.get("id") or returned_region_type.get("regionTypeId")
        assert returned_region_type_id == region_type_id_fk, "regionTypeId in response does not match request"

        # Verify GET /api/v1/org/regions/{id} returns regionTypeId correctly
        get_region_resp = requests.get(
            f"{BASE_URL}/api/v1/org/regions/{region_id}",
            headers=headers,
            timeout=TIMEOUT,
        )
        assert get_region_resp.status_code == 200, f"Region GET failed: {get_region_resp.text}"
        get_region_data = get_region_resp.json()
        get_region_type = get_region_data.get("regionType")
        assert get_region_type, "GET Region response missing regionType"
        get_region_type_id = get_region_type.get("id") or get_region_type.get("regionTypeId")
        assert get_region_type_id == region_type_id_fk, "regionTypeId in GET response does not match request"

        # Create a department under branch to test PUT deactivate and isActive flip
        department_payload = {
            "branchFk": branch_id,
            "nameAr": f"TestDepartmentAr-{uuid.uuid4()}",
            "nameEn": f"TestDepartmentEn-{uuid.uuid4()}",
            "parentDepartmentFk": None,
            "nodeTypeId": 1,
            "notes": "Department for deactivate test"
        }
        department_resp = requests.post(
            f"{BASE_URL}/api/v1/org/departments",
            json=department_payload,
            headers=headers,
            timeout=TIMEOUT,
        )
        assert department_resp.status_code == 200, f"Department creation failed: {department_resp.text}"
        department_data = department_resp.json()
        department_id = department_data.get("id")
        assert department_id is not None, "DepartmentResponse missing id"

        # Initially department should be active; isActive is expected true
        assert department_data.get("isActive") is True or department_data.get("isActive") == True

        # PUT /api/v1/org/departments/{id}/deactivate to flip isActive to false
        deactivate_resp = requests.put(
            f"{BASE_URL}/api/v1/org/departments/{department_id}/deactivate",
            headers=headers,
            timeout=TIMEOUT,
        )
        assert deactivate_resp.status_code == 200, f"Department deactivate failed: {deactivate_resp.text}"
        deactivate_data = deactivate_resp.json()
        assert deactivate_data.get("isActive") is False or deactivate_data.get("isActive") == False

        # GET /api/v1/org/departments/{id} to verify isActive=false
        get_dept_resp = requests.get(
            f"{BASE_URL}/api/v1/org/departments/{department_id}",
            headers=headers,
            timeout=TIMEOUT,
        )
        assert get_dept_resp.status_code == 200, f"Department GET after deactivate failed: {get_dept_resp.text}"
        get_dept_data = get_dept_resp.json()
        assert get_dept_data.get("isActive") is False or get_dept_data.get("isActive") == False

        # POST /api/v1/org/departments/search with branchFk and isActive filters to verify department is inactive in search
        search_payload = {
            "branchFk": branch_id,
            "isActiveFl": False
        }
        search_resp = requests.post(
            f"{BASE_URL}/api/v1/org/departments/search",
            json=search_payload,
            headers=headers,
            timeout=TIMEOUT,
        )
        assert search_resp.status_code == 200, f"Department search failed: {search_resp.text}"
        search_data = search_resp.json()
        items = search_data.get("items") or search_data if isinstance(search_data, list) else []
        assert any(item.get("id") == department_id for item in items), "Deactivated department not found in search results"

    finally:
        # Cleanup resources created
        # Delete department is not detailed in PRD, skipping delete for department
        # Delete region
        if region_id:
            requests.delete(
                f"{BASE_URL}/api/v1/org/regions/{region_id}",
                headers=headers,
                timeout=TIMEOUT,
            )
        # Delete branch
        if branch_id:
            requests.delete(
                f"{BASE_URL}/api/v1/org/branches/{branch_id}",
                headers=headers,
                timeout=TIMEOUT,
            )
        # Delete legal entity
        if legal_entity_id:
            requests.delete(
                f"{BASE_URL}/api/v1/org/legal-entities/{legal_entity_id}",
                headers=headers,
                timeout=TIMEOUT,
            )


test_post_api_v1_org_regions_round_trip_and_deactivate_department()
