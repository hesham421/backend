import requests

base_url = "http://localhost:7272"
timeout = 30


def get_access_token():
    login_payload = {
        "username": "admin",
        "password": "admin"
    }
    headers = {"Content-Type": "application/json"}
    r = requests.post(
        f"{base_url}/api/auth/login",
        json=login_payload,
        headers=headers,
        timeout=timeout,
    )
    assert r.status_code == 200, f"Login failed: {r.text}"
    data = r.json()
    access_token = data.get('accessToken')
    assert access_token is not None, "No accessToken in login response"
    return access_token


def test_putapiv1orgbranchesiddeactivate_rejects_with_dependent_entities():
    access_token = get_access_token()
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {access_token}"
    }

    # Step 1: Create a legal entity (needed as parent for branch)
    legal_entity_payload = {
        "nameAr": "Test Legal Entity AR",
        "nameEn": "Test Legal Entity EN",
        "entityTypeId": 1,
        "notes": "Legal entity for test"
    }
    legal_entity_id = None
    branch_id = None
    department_id = None
    cost_center_id = None
    location_site_id = None

    try:
        r = requests.post(
            f"{base_url}/api/v1/org/legal-entities",
            json=legal_entity_payload,
            headers=headers,
            timeout=timeout,
        )
        assert r.status_code == 200, f"Failed creating legal entity: {r.text}"
        legal_entity = r.json()
        legal_entity_id = legal_entity.get("id")
        assert legal_entity_id is not None

        # Step 2: Create a branch under the legal entity
        branch_payload = {
            "legalEntityFk": legal_entity_id,
            "nameAr": "Test Branch AR",
            "nameEn": "Test Branch EN",
            "branchTypeId": 1,
            "notes": "Branch for test"
        }
        r = requests.post(
            f"{base_url}/api/v1/org/branches",
            json=branch_payload,
            headers=headers,
            timeout=timeout,
        )
        assert r.status_code == 200, f"Failed creating branch: {r.text}"
        branch = r.json()
        branch_id = branch.get("id")
        assert branch_id is not None

        # Step 3: Create a department under the branch (to serve as dependent entity)
        department_payload = {
            "branchFk": branch_id,
            "nameAr": "Test Department AR",
            "nameEn": "Test Department EN",
            "parentDepartmentFk": None,
            "nodeTypeId": 1,
            "notes": "Department dependent for test",
            "isActive": True
        }
        r = requests.post(
            f"{base_url}/api/v1/org/departments",
            json=department_payload,
            headers=headers,
            timeout=timeout,
        )
        assert r.status_code == 200, f"Failed creating department: {r.text}"
        department = r.json()
        department_id = department.get("id")
        assert department_id is not None

        # Step 4: Create a cost center under the branch (dependent entity)
        cost_center_payload = {
            "branchFk": branch_id,
            "nameAr": "Test Cost Center AR",
            "nameEn": "Test Cost Center EN",
            "parentCostCenterFk": None,
            "nodeTypeId": 1,
            "costCenterTypeId": 1,
            "notes": "Cost center dependent for test",
            "isActive": True
        }
        r = requests.post(
            f"{base_url}/api/v1/org/cost-centers",
            json=cost_center_payload,
            headers=headers,
            timeout=timeout,
        )
        assert r.status_code == 200, f"Failed creating cost center: {r.text}"
        cost_center = r.json()
        cost_center_id = cost_center.get("id")
        assert cost_center_id is not None

        # Step 5: Create a location site under the branch (dependent entity)
        location_site_payload = {
            "branchFk": branch_id,
            "nameAr": "Test Location Site AR",
            "nameEn": "Test Location Site EN",
            "siteTypeId": 1,
            "notes": "Location site dependent for test",
            "isActive": True
        }
        r = requests.post(
            f"{base_url}/api/v1/org/location-sites",
            json=location_site_payload,
            headers=headers,
            timeout=timeout,
        )
        assert r.status_code == 200, f"Failed creating location site: {r.text}"
        location_site = r.json()
        location_site_id = location_site.get("id")
        assert location_site_id is not None

        # Step 6: Attempt to deactivate the branch - should fail with HTTP 409 conflict
        r = requests.put(
            f"{base_url}/api/v1/org/branches/{branch_id}/deactivate",
            headers=headers,
            timeout=timeout,
        )
        # We expect a 409 conflict due to existing dependent departments, cost centers, or location sites
        assert r.status_code == 409, f"Expected 409 conflict on deactivation but got {r.status_code}: {r.text}"

    finally:
        # Cleanup: Delete created dependent entities and branch/legal_entity if possible
        # Delete location site
        if location_site_id is not None:
            try:
                requests.delete(
                    f"{base_url}/api/v1/org/location-sites/{location_site_id}",
                    headers=headers,
                    timeout=timeout,
                )
            except Exception:
                pass
        # Delete cost center
        if cost_center_id is not None:
            try:
                requests.delete(
                    f"{base_url}/api/v1/org/cost-centers/{cost_center_id}",
                    headers=headers,
                    timeout=timeout,
                )
            except Exception:
                pass
        # Delete department
        if department_id is not None:
            try:
                requests.delete(
                    f"{base_url}/api/v1/org/departments/{department_id}",
                    headers=headers,
                    timeout=timeout,
                )
            except Exception:
                pass
        # Delete branch
        if branch_id is not None:
            try:
                requests.delete(
                    f"{base_url}/api/v1/org/branches/{branch_id}",
                    headers=headers,
                    timeout=timeout,
                )
            except Exception:
                pass
        # Delete legal entity
        if legal_entity_id is not None:
            try:
                requests.delete(
                    f"{base_url}/api/v1/org/legal-entities/{legal_entity_id}",
                    headers=headers,
                    timeout=timeout,
                )
            except Exception:
                pass


test_putapiv1orgbranchesiddeactivate_rejects_with_dependent_entities()
