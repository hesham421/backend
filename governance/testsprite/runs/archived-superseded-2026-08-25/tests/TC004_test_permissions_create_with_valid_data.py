import requests
import uuid

BASE_URL = "http://localhost:7272"
AUTH_USERNAME = "admin"
AUTH_PASSWORD = "admin"
TIMEOUT = 30

def test_permissions_create_with_valid_data():
    session = requests.Session()
    try:
        # Step 1: Authenticate to obtain JWT token using /api/auth/login
        login_url = f"{BASE_URL}/api/auth/login"
        login_payload = {"username": AUTH_USERNAME, "password": AUTH_PASSWORD}
        login_resp = session.post(login_url, json=login_payload, timeout=TIMEOUT)
        assert login_resp.status_code == 200, f"Login failed: {login_resp.text}"
        login_data = login_resp.json()
        assert login_data.get("success") is True, "Login response success flag is False"
        access_token = login_data.get("data", {}).get("accessToken")
        assert access_token, "No accessToken in login response"

        headers = {
            "Authorization": f"Bearer {access_token}",
            "Content-Type": "application/json",
            "Accept": "application/json"
        }

        # Step 2: Create a new Page with POST /api/pages to get a valid pageId
        pages_url = f"{BASE_URL}/api/pages"
        unique_page_code = f"testPage_{uuid.uuid4().hex[:8]}"
        create_page_payload = {
            "pageCode": unique_page_code,
            "nameAr": "اختبار صفحة",
            "nameEn": "Test Page",
            "route": f"/test/{unique_page_code.lower()}",
            "icon": "test-icon",
            "module": "TEST_MODULE",
            "parentId": None,
            "displayOrder": 1,
            "active": True,
            "description": "Page created for permission test",
            "suppressPermissionTypes": []
        }
        page_resp = session.post(pages_url, json=create_page_payload, headers=headers, timeout=TIMEOUT)
        assert page_resp.status_code == 201, f"Page creation failed: {page_resp.text}"
        page_resp_json = page_resp.json()
        assert page_resp_json.get("success") is True, "Page creation response success flag is False"
        page_data = page_resp_json.get("data")
        assert page_data and "id" in page_data, "Page ID missing in response"
        page_id = page_data["id"]

        # Step 3: Create a permission with POST /api/permissions using valid name, pageId and permissionType
        permissions_url = f"{BASE_URL}/api/permissions"
        unique_permission_name = f"perm_{uuid.uuid4().hex[:8]}"
        permission_payload = {
            "name": unique_permission_name,
            "pageId": page_id,
            "permissionType": "CREATE"
        }
        perm_resp = session.post(permissions_url, json=permission_payload, headers=headers, timeout=TIMEOUT)
        assert perm_resp.status_code == 201, f"Permission creation failed: {perm_resp.text}"
        perm_resp_json = perm_resp.json()
        assert perm_resp_json.get("success") is True, "Permission creation response success flag is False"
        perm_data = perm_resp_json.get("data")
        assert perm_data is not None, "Permission data missing in response"
        assert perm_data.get("name") == unique_permission_name, "Permission name mismatch"
        assert perm_data.get("pageId") == page_id, "Permission pageId mismatch"
        assert perm_data.get("permissionType") == "CREATE", "Permission type mismatch"
    finally:
        # Cleanup: Delete the created permission and page if exists (best effort)
        try:
            if 'perm_data' in locals() and perm_data and perm_data.get("id"):
                perm_id = perm_data["id"]
                session.delete(f"{BASE_URL}/api/permissions/{perm_id}", headers=headers, timeout=TIMEOUT)
        except Exception:
            pass

        try:
            if 'page_id' in locals() and page_id:
                session.delete(f"{BASE_URL}/api/pages/{page_id}", headers=headers, timeout=TIMEOUT)
        except Exception:
            pass

test_permissions_create_with_valid_data()