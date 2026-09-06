import requests
import uuid

BASE_URL = "http://localhost:7272"
LOGIN_PATH = "/api/v1/security/auth/login"
MODULES_PATH = "/api/v1/security/modules"
PAGES_PATH = "/api/v1/security/pages"

TIMEOUT = 30


def test_post_api_v1_security_pages_with_valid_data():
    # Authenticate and get Bearer token
    login_url = BASE_URL + LOGIN_PATH
    auth_payload = {"username": "admin", "password": "admin"}
    login_resp = requests.post(login_url, json=auth_payload, timeout=TIMEOUT)
    assert login_resp.status_code == 200, f"Login failed: {login_resp.text}"
    login_data = login_resp.json()
    access_token = login_data.get("data", {}).get("accessToken")
    assert access_token, "No accessToken in login response"

    headers = {"Authorization": f"Bearer {access_token}", "Content-Type": "application/json"}

    # Create unique security module first to get moduleFk
    unique_module_code = f"MOD_{uuid.uuid4().hex[:8]}"
    module_payload = {
        "moduleCode": unique_module_code,
        "nameAr": "وصف الوحدة " + unique_module_code,
        "nameEn": "Module Description " + unique_module_code,
        "isActiveFl": True
    }
    module_resp = requests.post(BASE_URL + MODULES_PATH, json=module_payload, headers=headers, timeout=TIMEOUT)
    assert module_resp.status_code == 201, f"Module creation failed: {module_resp.text}"
    module_data = module_resp.json()
    module = module_data.get("data")
    assert module and "id" in module, "Module creation response missing id"
    module_fk = module["id"]

    # Now create a page with valid data using the moduleFk
    # Natural-key codes are normalized to UPPERCASE server-side (RULE-SEC-010), so generate uppercase
    unique_page_code = f"PAGE_{uuid.uuid4().hex[:8]}".upper()
    page_payload = {
        "pageCode": unique_page_code,
        "nameAr": "صفحة اختبار " + unique_page_code,
        "nameEn": "Test Page " + unique_page_code,
        "moduleFk": module_fk,
        "isActiveFl": True
    }

    try:
        page_resp = requests.post(BASE_URL + PAGES_PATH, json=page_payload, headers=headers, timeout=TIMEOUT)
        assert page_resp.status_code == 201, f"Page creation failed: {page_resp.text}"
        page_data = page_resp.json()
        page = page_data.get("data")
        assert page, "Response missing 'data'"
        assert page.get("pageCode") == unique_page_code, "Page code mismatch"
        assert page.get("moduleFk") == module_fk, "Module foreign key mismatch"
        # Validate that permissions were generated: typically permissions are auto-generated - presence of permissions field or permission count > 0 can be checked if available
        # Since PRD mentions generated permissions, we check if response contains 'permissions' or similar
        # But given no explicit permissions field stated, just assert page has an id and created
        assert "id" in page, "Created page missing id"
    finally:
        # Cleanup: delete the created page and module to avoid residual data
        # Delete page (soft delete)
        if 'page' in locals() and page and "id" in page:
            page_id = page["id"]
            del_page_resp = requests.delete(f"{BASE_URL}{PAGES_PATH}/{page_id}", headers=headers, timeout=TIMEOUT)
            # Delete returns 204 No Content on success or 404 if already deleted
            assert del_page_resp.status_code in (204, 404), f"Failed to delete page: {del_page_resp.text}"
        # Delete module (soft delete)
        if 'module_fk' in locals() and module_fk:
            del_mod_resp = requests.delete(f"{BASE_URL}{MODULES_PATH}/{module_fk}", headers=headers, timeout=TIMEOUT)
            assert del_mod_resp.status_code in (204, 404), f"Failed to delete module: {del_mod_resp.text}"


test_post_api_v1_security_pages_with_valid_data()