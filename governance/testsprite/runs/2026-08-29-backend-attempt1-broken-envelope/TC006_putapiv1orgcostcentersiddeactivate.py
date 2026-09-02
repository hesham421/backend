import requests
from requests.auth import HTTPBasicAuth

BASE_URL = "http://localhost:7272"
AUTH_USERNAME = "admin"
AUTH_PASSWORD = "admin"
TIMEOUT = 30

def get_auth_token():
    login_url = f"{BASE_URL}/api/auth/login"
    response = requests.post(login_url, json={"username": AUTH_USERNAME, "password": AUTH_PASSWORD}, timeout=TIMEOUT)
    response.raise_for_status()
    data = response.json()
    assert "accessToken" in data, "accessToken not in login response"
    return data["accessToken"]


def create_legal_entity(headers):
    url = f"{BASE_URL}/api/v1/org/legal-entities"
    payload = {
        "nameAr": "Test LegalEntity AR",
        "nameEn": "Test LegalEntity EN",
        "entityTypeId": 1,
        "notes": "Temporary legal entity for test"
    }
    resp = requests.post(url, json=payload, headers=headers, timeout=TIMEOUT)
    resp.raise_for_status()
    return resp.json()["id"] if "id" in resp.json() else resp.json().get("legalEntityPk") or resp.json().get("id")


def create_branch(legal_entity_id, headers):
    url = f"{BASE_URL}/api/v1/org/branches"
    payload = {
        "legalEntityFk": legal_entity_id,
        "nameAr": "Test Branch AR",
        "nameEn": "Test Branch EN",
        "branchTypeId": 1,
        "notes": "Temporary branch for test"
    }
    resp = requests.post(url, json=payload, headers=headers, timeout=TIMEOUT)
    resp.raise_for_status()
    return resp.json()["id"] if "id" in resp.json() else resp.json().get("branchPk") or resp.json().get("id")


def create_cost_center(branch_id, parent_id, name_suffix, headers):
    url = f"{BASE_URL}/api/v1/org/cost-centers"
    payload = {
        "branchFk": branch_id,
        "nameAr": f"Test CostCenter AR {name_suffix}",
        "nameEn": f"Test CostCenter EN {name_suffix}",
        "parentCostCenterFk": parent_id,
        "nodeTypeId": 1,
        "costCenterTypeId": 1,
        "notes": f"Temporary cost center {name_suffix} for test"
    }
    resp = requests.post(url, json=payload, headers=headers, timeout=TIMEOUT)
    resp.raise_for_status()
    return resp.json()["id"] if "id" in resp.json() else resp.json().get("costCenterPk") or resp.json().get("id")


def delete_cost_center(cost_center_id, headers):
    url = f"{BASE_URL}/api/v1/org/cost-centers/{cost_center_id}/deactivate"
    try:
        requests.put(url, headers=headers, timeout=TIMEOUT)
    except Exception:
        pass
    url_del = f"{BASE_URL}/api/v1/org/cost-centers/{cost_center_id}"
    try:
        requests.delete(url_del, headers=headers, timeout=TIMEOUT)
    except Exception:
        pass


def delete_branch(branch_id, headers):
    url = f"{BASE_URL}/api/v1/org/branches/{branch_id}/deactivate"
    try:
        requests.put(url, headers=headers, timeout=TIMEOUT)
    except Exception:
        pass
    url_del = f"{BASE_URL}/api/v1/org/branches/{branch_id}"
    try:
        requests.delete(url_del, headers=headers, timeout=TIMEOUT)
    except Exception:
        pass


def delete_legal_entity(legal_entity_id, headers):
    url = f"{BASE_URL}/api/v1/org/legal-entities/{legal_entity_id}/deactivate"
    try:
        requests.put(url, headers=headers, timeout=TIMEOUT)
    except Exception:
        pass
    url_del = f"{BASE_URL}/api/v1/org/legal-entities/{legal_entity_id}"
    try:
        requests.delete(url_del, headers=headers, timeout=TIMEOUT)
    except Exception:
        pass


def test_putapiv1orgcostcentersiddeactivate_rejects_if_child_cost_centers_exist():
    token = get_auth_token()
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }

    legal_entity_id = None
    branch_id = None
    parent_cost_center_id = None
    child_cost_center_id = None

    try:
        # Create legal entity
        url_le = f"{BASE_URL}/api/v1/org/legal-entities"
        payload_le = {
            "nameAr": "Test LegalEntity AR",
            "nameEn": "Test LegalEntity EN",
            "entityTypeId": 1,
            "notes": "Temporary legal entity for test"
        }
        resp_le = requests.post(url_le, json=payload_le, headers=headers, timeout=TIMEOUT)
        resp_le.raise_for_status()
        legal_entity_id = resp_le.json().get("id") or resp_le.json().get("legalEntityPk") or resp_le.json().get("legalEntityId")
        assert legal_entity_id is not None, "Failed to get legal entity ID"

        # Create branch under legal entity
        url_branch = f"{BASE_URL}/api/v1/org/branches"
        payload_branch = {
            "legalEntityFk": legal_entity_id,
            "nameAr": "Test Branch AR",
            "nameEn": "Test Branch EN",
            "branchTypeId": 1,
            "notes": "Temporary branch for test"
        }
        resp_branch = requests.post(url_branch, json=payload_branch, headers=headers, timeout=TIMEOUT)
        resp_branch.raise_for_status()
        branch_id = resp_branch.json().get("id") or resp_branch.json().get("branchPk") or resp_branch.json().get("branchId")
        assert branch_id is not None, "Failed to get branch ID"

        # Create parent cost center (no parent)
        url_cc = f"{BASE_URL}/api/v1/org/cost-centers"
        payload_parent_cc = {
            "branchFk": branch_id,
            "nameAr": "Parent CostCenter AR",
            "nameEn": "Parent CostCenter EN",
            "parentCostCenterFk": None,
            "nodeTypeId": 1,
            "costCenterTypeId": 1,
            "notes": "Temporary parent cost center for test"
        }
        resp_parent_cc = requests.post(url_cc, json=payload_parent_cc, headers=headers, timeout=TIMEOUT)
        resp_parent_cc.raise_for_status()
        parent_cost_center_id = resp_parent_cc.json().get("id") or resp_parent_cc.json().get("costCenterPk") or resp_parent_cc.json().get("costCenterId")
        assert parent_cost_center_id is not None, "Failed to get parent cost center ID"

        # Create child cost center with parent set
        payload_child_cc = {
            "branchFk": branch_id,
            "nameAr": "Child CostCenter AR",
            "nameEn": "Child CostCenter EN",
            "parentCostCenterFk": parent_cost_center_id,
            "nodeTypeId": 1,
            "costCenterTypeId": 1,
            "notes": "Temporary child cost center for test"
        }
        resp_child_cc = requests.post(url_cc, json=payload_child_cc, headers=headers, timeout=TIMEOUT)
        resp_child_cc.raise_for_status()
        child_cost_center_id = resp_child_cc.json().get("id") or resp_child_cc.json().get("costCenterPk") or resp_child_cc.json().get("costCenterId")
        assert child_cost_center_id is not None, "Failed to get child cost center ID"

        # Attempt to deactivate parent cost center
        url_deactivate = f"{BASE_URL}/api/v1/org/cost-centers/{parent_cost_center_id}/deactivate"
        resp_deactivate = requests.put(url_deactivate, headers=headers, timeout=TIMEOUT)

        # Expect failure due to dependent child cost centers
        assert resp_deactivate.status_code >= 400, "Expected error status code when deactivating parent with children"

        # Parse error response to verify business rule rejection
        try:
            err_json = resp_deactivate.json()
            # Could verify error message or code if present:
            assert any(k in err_json for k in ("error", "message", "detail", "code")), "No error details found in response JSON"
        except Exception:
            # If no JSON, just fail if status code is not 200
            assert False, "Expected JSON error response on deactivation failure"

    finally:
        # Cleanup: delete child cost center if created
        if child_cost_center_id is not None:
            try:
                url_del_child = f"{BASE_URL}/api/v1/org/cost-centers/{child_cost_center_id}"
                requests.delete(url_del_child, headers=headers, timeout=TIMEOUT)
            except Exception:
                pass

        # Cleanup: delete parent cost center if created
        if parent_cost_center_id is not None:
            try:
                url_del_parent = f"{BASE_URL}/api/v1/org/cost-centers/{parent_cost_center_id}"
                requests.delete(url_del_parent, headers=headers, timeout=TIMEOUT)
            except Exception:
                pass

        # Cleanup: delete branch if created
        if branch_id is not None:
            try:
                url_del_branch = f"{BASE_URL}/api/v1/org/branches/{branch_id}"
                requests.delete(url_del_branch, headers=headers, timeout=TIMEOUT)
            except Exception:
                pass

        # Cleanup: delete legal entity if created
        if legal_entity_id is not None:
            try:
                url_del_legal = f"{BASE_URL}/api/v1/org/legal-entities/{legal_entity_id}"
                requests.delete(url_del_legal, headers=headers, timeout=TIMEOUT)
            except Exception:
                pass

test_putapiv1orgcostcentersiddeactivate_rejects_if_child_cost_centers_exist()
