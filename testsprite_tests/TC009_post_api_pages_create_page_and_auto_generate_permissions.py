import requests
import uuid

BASE_URL = "http://localhost:7272"
LOGIN_URL = f"{BASE_URL}/api/auth/login"
PAGES_URL = f"{BASE_URL}/api/pages"
PERMISSIONS_URL = f"{BASE_URL}/api/permissions"

USERNAME = "admin"
PASSWORD = "admin"
TIMEOUT = 30

def test_post_api_pages_create_page_and_auto_generate_permissions():
    # Step 1: Login to get JWT token
    login_payload = {
        "username": USERNAME,
        "password": PASSWORD
    }
    login_response = requests.post(LOGIN_URL, json=login_payload, timeout=TIMEOUT)
    assert login_response.status_code == 200, f"Login failed: {login_response.text}"
    login_data = login_response.json()
    access_token = login_data.get("accessToken")
    assert access_token and isinstance(access_token, str), "accessToken missing in login response"

    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json"
    }

    # Unique pageCode to avoid conflicts
    unique_page_code = f"testpage_{uuid.uuid4().hex[:8]}"

    # Prepare page metadata payload
    page_payload = {
        "pageCode": unique_page_code,
        "nameAr": "اختبار الصفحة",
        "nameEn": "Test Page",
        "route": f"/test-page-{unique_page_code}",
        "icon": "test-icon",
        "module": "test-module",
        "parentId": None,
        "displayOrder": 100,
        "active": True,
        "description": "This is a test page created by automated test.",
        "suppressPermissionTypes": []
    }

    created_page_id = None

    try:
        # Step 2: Create new page
        create_response = requests.post(PAGES_URL, json=page_payload, headers=headers, timeout=TIMEOUT)
        assert create_response.status_code == 200, f"Page creation failed: {create_response.text}"
        page_response = create_response.json()

        # Verify that response contains the expected fields
        created_page_id = page_response.get("id")
        assert created_page_id is not None, "Created page 'id' missing in response"
        assert page_response.get("pageCode") == unique_page_code, "pageCode mismatch in response"
        assert page_response.get("nameEn") == page_payload["nameEn"], "nameEn mismatch in response"

        # Step 3: Verify permissions VIEW, CREATE, UPDATE, DELETE are auto-generated for new page
        # Query permissions by pageId and permissionType via search endpoint
        search_payload = {
            "pageId": created_page_id,
            "name": None,
            "module": None
        }
        # Since /api/permissions/search accepts name/module filters (no explicit pageId in schema), 
        # we instead call GET /api/permissions?pageId=... is not specified.
        # We'll retrieve permissions by searching all and then filtering locally by pageId in test.

        permissions_search_payload = {"name": None, "module": None}
        permissions_search_resp = requests.post(f"{BASE_URL}/api/permissions/search", json=permissions_search_payload, headers=headers, timeout=TIMEOUT)
        assert permissions_search_resp.status_code == 200, f"Permissions search failed: {permissions_search_resp.text}"
        permissions_list = permissions_search_resp.json().get("content", [])
        # Filter permissions for the created page id
        page_permissions = [p for p in permissions_list if p.get("pageId") == created_page_id]

        expected_permission_types = {"VIEW", "CREATE", "UPDATE", "DELETE"}

        permission_types_found = {perm.get("permissionType") for perm in page_permissions}

        missing_permissions = expected_permission_types - permission_types_found
        assert not missing_permissions, f"Missing permissions for page {unique_page_code}: {missing_permissions}"

    finally:
        if created_page_id:
            # Cleanup: Delete the created page (if API supports it; else deactivate)
            # The PRD does not have DELETE for pages, but deactivation is available:
            deactivate_url = f"{PAGES_URL}/{created_page_id}/deactivate"
            try:
                resp_deactivate = requests.put(deactivate_url, headers=headers, timeout=TIMEOUT)
                assert resp_deactivate.status_code == 200, f"Failed to deactivate page during cleanup: {resp_deactivate.text}"
            except Exception:
                pass

test_post_api_pages_create_page_and_auto_generate_permissions()