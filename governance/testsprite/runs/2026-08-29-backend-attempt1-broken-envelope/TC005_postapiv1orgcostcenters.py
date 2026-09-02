import requests

BASE_URL = "http://localhost:7272"
AUTH_USERNAME = "admin"
AUTH_PASSWORD = "admin"
TIMEOUT = 30


def test_post_api_v1_org_cost_centers():
    headers = {
        "Content-Type": "application/json"
    }

    # Login to get JWT token
    login_payload = {
        "username": AUTH_USERNAME,
        "password": AUTH_PASSWORD
    }
    login_resp = requests.post(f"{BASE_URL}/api/auth/login", json=login_payload, headers=headers, timeout=TIMEOUT)
    assert login_resp.status_code == 200, f"Login request failed with status {login_resp.status_code}: {login_resp.text}"

    token_json = login_resp.json()
    assert "accessToken" in token_json, f"Login response missing accessToken, got: {token_json}"
    access_token = token_json["accessToken"]

    auth_headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {access_token}"
    }

    # Helper function to create a legal entity to get a valid legalEntityFk
    def create_legal_entity():
        payload = {
            "nameAr": "Test Legal Entity العربية",
            "nameEn": "Test Legal Entity EN",
            "entityTypeId": 1,
            "notes": "Created for cost center test"
        }
        resp = requests.post(f"{BASE_URL}/api/v1/org/legal-entities", json=payload, headers=auth_headers, timeout=TIMEOUT)
        resp.raise_for_status()
        return resp.json()["id"]

    # Helper function to create a branch under the legal entity to get a valid branchFk
    def create_branch(legal_entity_id):
        payload = {
            "legalEntityFk": legal_entity_id,
            "nameAr": "Test Branch العربية",
            "nameEn": "Test Branch EN",
            "branchTypeId": 1,
            "notes": "Created for cost center test"
        }
        resp = requests.post(f"{BASE_URL}/api/v1/org/branches", json=payload, headers=auth_headers, timeout=TIMEOUT)
        resp.raise_for_status()
        return resp.json()["id"]

    # Helper function to create a cost center
    def create_cost_center(branch_id, parent_id=None, name_suffix=""):
        payload = {
            "branchFk": branch_id,
            "nameAr": f"Test Cost Center العربية{name_suffix}",
            "nameEn": f"Test Cost Center EN{name_suffix}",
            "nodeTypeId": 1,
            "costCenterTypeId": 1,
            "notes": "Created in test"
        }
        if parent_id is not None:
            payload["parentCostCenterFk"] = parent_id

        resp = requests.post(f"{BASE_URL}/api/v1/org/cost-centers", json=payload, headers=auth_headers, timeout=TIMEOUT)
        return resp

    # Helper function to delete cost center by id
    def delete_cost_center(cost_center_id):
        try:
            requests.delete(f"{BASE_URL}/api/v1/org/cost-centers/{cost_center_id}", headers=auth_headers, timeout=TIMEOUT)
        except Exception:
            pass

    # Helper function to delete branch by id
    def delete_branch(branch_id):
        try:
            requests.delete(f"{BASE_URL}/api/v1/org/branches/{branch_id}", headers=auth_headers, timeout=TIMEOUT)
        except Exception:
            pass

    # Helper function to delete legal entity by id
    def delete_legal_entity(legal_entity_id):
        try:
            requests.delete(f"{BASE_URL}/api/v1/org/legal-entities/{legal_entity_id}", headers=auth_headers, timeout=TIMEOUT)
        except Exception:
            pass

    legal_entity_id = None
    branch_id = None
    parent_cost_center_id = None
    child_cost_center_id = None

    try:
        # Create legal entity and branch to use in cost centers
        legal_entity_id = create_legal_entity()
        branch_id = create_branch(legal_entity_id)

        # Create a parent cost center
        resp_parent = create_cost_center(branch_id, None, " Parent")
        resp_parent.raise_for_status()
        parent_cost_center_id = resp_parent.json()["id"]
        assert resp_parent.json()["parentCostCenterFk"] is None
        assert resp_parent.json()["branchFk"] == branch_id

        # Create a child cost center with valid parentCostCenterFk
        resp_child = create_cost_center(branch_id, parent_cost_center_id, " Child")
        resp_child.raise_for_status()
        child_cost_center_id = resp_child.json()["id"]
        assert resp_child.json()["parentCostCenterFk"] == parent_cost_center_id
        assert resp_child.json()["branchFk"] == branch_id

        # Test circular parentCostCenterFk reference rejection:
        # Attempt to create a cost center whose parentCostCenterFk is itself
        payload_circular_self = {
            "branchFk": branch_id,
            "nameAr": "Circular Self العربية",
            "nameEn": "Circular Self EN",
            "nodeTypeId": 1,
            "costCenterTypeId": 1,
            "notes": "Circular parent test",
            # Intentionally set parentCostCenterFk to a temporary invalid ID to be replaced later
            # We'll create it in two steps: first create with no parent, then try to update
        }
        # First create a cost center with no parent
        resp_circular_base = requests.post(f"{BASE_URL}/api/v1/org/cost-centers", json=payload_circular_self, headers=auth_headers, timeout=TIMEOUT)
        resp_circular_base.raise_for_status()
        circular_id = resp_circular_base.json()["id"]

        # Try to update it to have parentCostCenterFk as itself, which should raise validation error
        payload_update_circular = {
            "branchFk": branch_id,
            "nameAr": "Circular Self العربية",
            "nameEn": "Circular Self EN",
            "nodeTypeId": 1,
            "costCenterTypeId": 1,
            "notes": "Circular parent test",
            "parentCostCenterFk": circular_id
        }
        update_resp = requests.put(f"{BASE_URL}/api/v1/org/cost-centers/{circular_id}", json=payload_update_circular, headers=auth_headers, timeout=TIMEOUT)

        assert update_resp.status_code >= 400, "API should reject circular parentCostCenterFk references"
        error_json = update_resp.json()
        assert any(k in error_json for k in ["success", "error", "message", "validationErrors"]), "Error response should contain error details"

        # Also try to create a cost center with parentCostCenterFk that would create a cycle indirectly:
        # For example, attempt to create a cost center that is parent of its own ancestor

        # Attempting to create direct cycle with parentCostCenterFk = child_cost_center_id for parent_cost_center_id should be rejected
        payload_cycle = {
            "branchFk": branch_id,
            "nameAr": "Cycle Test العربية",
            "nameEn": "Cycle Test EN",
            "nodeTypeId": 1,
            "costCenterTypeId": 1,
            "notes": "Cycle parent test",
            "parentCostCenterFk": child_cost_center_id  # child as parent of its ancestor (parent_cost_center_id)
        }
        resp_cycle = requests.post(f"{BASE_URL}/api/v1/org/cost-centers", json=payload_cycle, headers=auth_headers, timeout=TIMEOUT)
        assert resp_cycle.status_code >= 400, "API should reject circular parentCostCenterFk referencing an existing child cost center"
        cycle_error_json = resp_cycle.json()
        assert any(k in cycle_error_json for k in ["success", "error", "message", "validationErrors"]), "Error response should contain error details"

    finally:
        # Clean up created cost centers, branch, legal entity
        if child_cost_center_id:
            delete_cost_center(child_cost_center_id)
        if parent_cost_center_id:
            delete_cost_center(parent_cost_center_id)
        if 'circular_id' in locals():
            delete_cost_center(circular_id)
        if branch_id:
            delete_branch(branch_id)
        if legal_entity_id:
            delete_legal_entity(legal_entity_id)


test_post_api_v1_org_cost_centers()
