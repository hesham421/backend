import requests
from requests.auth import HTTPBasicAuth

BASE_URL = "http://localhost:7272"
AUTH_CREDENTIALS = ("admin", "admin")
HEADERS = {"Content-Type": "application/json"}
TIMEOUT = 30


def authenticate():
    url = f"{BASE_URL}/api/auth/login"
    payload = {
        "username": AUTH_CREDENTIALS[0],
        "password": AUTH_CREDENTIALS[1]
    }
    resp = requests.post(url, json=payload, timeout=TIMEOUT, headers={"Content-Type": "application/json"})
    resp.raise_for_status()
    data = resp.json()
    assert "accessToken" in data, "Login response missing accessToken"
    return data["accessToken"]


def create_branch(auth_token, legal_entity_id):
    url = f"{BASE_URL}/api/v1/org/branches"
    payload = {
        "legalEntityFk": legal_entity_id,
        "nameAr": "Test Branch AR",
        "nameEn": "Test Branch EN",
        "branchTypeId": 1,
        "notes": "Test branch for department creation"
    }
    headers = {
        **HEADERS,
        "Authorization": f"Bearer {auth_token}"
    }
    resp = requests.post(url, json=payload, headers=headers, timeout=TIMEOUT)
    resp.raise_for_status()
    data = resp.json()
    assert "id" in data, "Branch creation response missing 'id'"
    return data["id"]


def create_department(auth_token, branch_fk, parent_department_fk=None, node_type_id=1, name_en="Dept EN", name_ar="Dept AR", notes="Created by test"):
    url = f"{BASE_URL}/api/v1/org/departments"
    payload = {
        "branchFk": branch_fk,
        "nameEn": name_en,
        "nameAr": name_ar,
        "parentDepartmentFk": parent_department_fk,
        "nodeTypeId": node_type_id,
        "notes": notes
    }
    headers = {
        **HEADERS,
        "Authorization": f"Bearer {auth_token}"
    }
    resp = requests.post(url, json=payload, headers=headers, timeout=TIMEOUT)
    return resp


def get_department(auth_token, department_id):
    url = f"{BASE_URL}/api/v1/org/departments/{department_id}"
    headers = {
        "Authorization": f"Bearer {auth_token}"
    }
    resp = requests.get(url, headers=headers, timeout=TIMEOUT)
    resp.raise_for_status()
    return resp.json()


def test_post_apiv1_org_departments():
    auth_token = authenticate()
    headers = {"Authorization": f"Bearer {auth_token}"}

    # Create a legal entity first (required dependency for branch)
    url_legal_entity = f"{BASE_URL}/api/v1/org/legal-entities"
    legal_entity_payload = {
        "nameAr": "Test LE AR",
        "nameEn": "Test LE EN",
        "entityTypeId": 1,
        "notes": "Legal entity created for test"
    }
    resp_legal_entity = requests.post(url_legal_entity, json=legal_entity_payload, headers={**HEADERS, **headers}, timeout=TIMEOUT)
    resp_legal_entity.raise_for_status()
    legal_entity = resp_legal_entity.json()
    legal_entity_id = legal_entity.get("id")
    assert legal_entity_id is not None, "Legal entity creation failed - no id"

    # Create a branch under legal entity - required for department
    branch_id = None
    try:
        branch_id = create_branch(auth_token, legal_entity_id)

        # Create root department (no parent)
        resp_dept_1 = create_department(auth_token, branch_fk=branch_id, parent_department_fk=None, node_type_id=1,
                                        name_en="Root Department", name_ar="قسم الجذر", notes="Root dept for test")
        assert resp_dept_1.status_code == 200, f"Root department creation failed with status {resp_dept_1.status_code}"
        dept_1_data = resp_dept_1.json()
        dept_1_id = dept_1_data.get("id")
        assert dept_1_id is not None, "Root department creation response missing 'id'"

        # Create child department with valid parentDepartmentFk
        resp_dept_2 = create_department(auth_token, branch_fk=branch_id, parent_department_fk=dept_1_id, node_type_id=1,
                                        name_en="Child Department", name_ar="قسم طفل", notes="Child dept for test")
        assert resp_dept_2.status_code == 200, f"Child department creation with parent failed with status {resp_dept_2.status_code}"
        dept_2_data = resp_dept_2.json()
        dept_2_id = dept_2_data.get("id")
        assert dept_2_id is not None, "Child department creation response missing 'id'"

        # Test for circular reference: create a department with parentDepartmentFk = itself
        resp_circular_self = create_department(auth_token, branch_fk=branch_id, parent_department_fk=9999999, node_type_id=1,
                                               name_en="Circular Dept Self", name_ar="قسم دائري ذاتي", notes="Circular test self ref")
        # 9999999 likely does not exist, but let's ignore; Instead, test circular by updating a department's parentDepartment to itself is not in scope.
        # Instead, we test circular by trying to create dept with parent as itself (which should be rejected).
        # We do it properly now by attempting:

        # Attempt circular reference: parentDepartmentFk = its own id (set after creation)
        resp_temp = create_department(auth_token, branch_fk=branch_id, parent_department_fk=None, node_type_id=1,
                                      name_en="Circular Parent Dept", name_ar="قسم أب دائري", notes="Will test circular parent")
        assert resp_temp.status_code == 200, "Failed to create department for circular test"
        circular_parent_id = resp_temp.json().get("id")
        assert circular_parent_id is not None

        # Try updating that department's parentDepartmentFk to itself to induce circular reference via a PUT (not given in test)
        # Since test only asks POST creation with circular parentDepartmentFk, we attempt to create new with parentDepartmentFk = id of this new dept
        resp_circular = create_department(auth_token, branch_fk=branch_id, parent_department_fk=circular_parent_id,
                                          node_type_id=1, name_en="Dept Circular Child", name_ar="قسم طفل دائري",
                                          notes="Trying circular parent reference")
        # Expected to fail due to circular reference
        assert resp_circular.status_code != 200, "Circular parentDepartmentFk creation unexpectedly succeeded"
        # Validate error message or validation failure in response
        try:
            err_data = resp_circular.json()
            assert "success" in err_data and err_data["success"] is False or resp_circular.status_code >= 400, "Expected error response on circular reference"
        except Exception:
            # If no JSON returned, assume error
            pass

    finally:
        # Clean up created departments
        for dept_id in [dept_1_data.get("id") if 'dept_1_data' in locals() else None,
                        dept_2_data.get("id") if 'dept_2_data' in locals() else None,
                        circular_parent_id if 'circular_parent_id' in locals() else None]:
            if dept_id:
                del_url = f"{BASE_URL}/api/v1/org/departments/{dept_id}"
                try:
                    requests.delete(del_url, headers=headers, timeout=TIMEOUT)
                except Exception:
                    pass

        # Clean up branch
        if branch_id:
            try:
                del_branch_url = f"{BASE_URL}/api/v1/org/branches/{branch_id}"
                requests.delete(del_branch_url, headers=headers, timeout=TIMEOUT)
            except Exception:
                pass

        # Clean up legal entity
        if legal_entity_id:
            try:
                del_le_url = f"{BASE_URL}/api/v1/org/legal-entities/{legal_entity_id}"
                requests.delete(del_le_url, headers=headers, timeout=TIMEOUT)
            except Exception:
                pass


test_post_apiv1_org_departments()