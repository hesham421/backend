import requests
import uuid

BASE_URL = "http://localhost:7272"
TIMEOUT = 30

def test_post_api_v1_security_modules_with_valid_data():
    # Step 1: Authenticate and get access token
    login_url = f"{BASE_URL}/api/v1/security/auth/login"
    login_payload = {"username": "admin", "password": "admin"}
    login_headers = {"Content-Type": "application/json"}

    login_resp = requests.post(login_url, json=login_payload, headers=login_headers, timeout=TIMEOUT)
    assert login_resp.status_code == 200, f"Login failed with status {login_resp.status_code}"
    login_data = login_resp.json()
    assert "data" in login_data and "accessToken" in login_data["data"], "accessToken missing in login response"
    access_token = login_data["data"]["accessToken"]

    # Step 2: Prepare unique module data
    unique_suffix = str(uuid.uuid4())
    # Natural-key codes are normalized to UPPERCASE server-side (RULE-SEC-010), so generate uppercase
    module_code = f"TESTMOD_{unique_suffix[:8]}".upper()
    name_ar = f"Test Module AR {unique_suffix[:8]}"
    name_en = f"Test Module EN {unique_suffix[:8]}"

    # Step 3: Create security module
    create_url = f"{BASE_URL}/api/v1/security/modules"
    create_headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {access_token}"
    }
    create_payload = {
        "moduleCode": module_code,
        "nameAr": name_ar,
        "nameEn": name_en
    }

    created_module_id = None
    try:
        create_resp = requests.post(create_url, json=create_payload, headers=create_headers, timeout=TIMEOUT)
        assert create_resp.status_code == 201, f"Module creation failed with status {create_resp.status_code}"
        create_data = create_resp.json()
        assert "data" in create_data, "Response missing 'data' field"
        module_data = create_data["data"]
        assert module_data["moduleCode"] == module_code, "moduleCode mismatch"
        assert module_data["nameAr"] == name_ar, "nameAr mismatch"
        assert module_data["nameEn"] == name_en, "nameEn mismatch"
        created_module_id = module_data.get("id") or module_data.get("moduleId")
        assert created_module_id is not None, "Created module ID is missing"

    finally:
        # Step 4: Cleanup - delete the created module if it was created
        if created_module_id:
            delete_url = f"{BASE_URL}/api/v1/security/modules/{created_module_id}"
            del_headers = {"Authorization": f"Bearer {access_token}"}
            del_resp = requests.delete(delete_url, headers=del_headers, timeout=TIMEOUT)
            assert del_resp.status_code == 204, f"Module deletion failed with status {del_resp.status_code}"

test_post_api_v1_security_modules_with_valid_data()