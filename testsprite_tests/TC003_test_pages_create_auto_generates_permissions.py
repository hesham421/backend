import requests
import uuid
import random
import string

BASE_URL = "http://localhost:7272"
AUTH_CREDENTIALS = ("admin", "admin")
TIMEOUT = 30


def get_basic_auth_token():
    # Login to get JWT access token via /api/auth/login (since API docs say JWT bearer auth)
    url = f"{BASE_URL}/api/auth/login"
    payload = {
        "username": AUTH_CREDENTIALS[0],
        "password": AUTH_CREDENTIALS[1]
    }
    headers = {"Content-Type": "application/json"}
    response = requests.post(url, json=payload, headers=headers, timeout=TIMEOUT)
    assert response.status_code == 200, f"Login failed with status {response.status_code}, body: {response.text}"
    resp_json = response.json()
    assert resp_json.get("success") is True
    data = resp_json.get("data")
    assert "accessToken" in data
    return data["accessToken"]


def test_pages_create_auto_generates_permissions():
    access_token = get_basic_auth_token()
    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json"
    }

    # Prepare unique page data
    unique_suffix = uuid.uuid4().hex[:8]
    page_code = f"testpage_{unique_suffix}"
    page_name_ar = f"صفحة اختبار {unique_suffix}"
    page_name_en = f"Test Page {unique_suffix}"
    route = f"/test-route-{unique_suffix}"
    module = "testModule"
    icon = "test-icon"
    parent_id = None
    display_order = random.randint(1, 1000)
    active = True
    description = "Page created by automated test for permissions check"
    suppress_permission_types = []

    page_payload = {
        "pageCode": page_code,
        "nameAr": page_name_ar,
        "nameEn": page_name_en,
        "route": route,
        "icon": icon,
        "module": module,
        "parentId": parent_id,
        "displayOrder": display_order,
        "active": active,
        "description": description,
        "suppressPermissionTypes": suppress_permission_types
    }

    created_page_id = None

    try:
        # Create the page
        create_url = f"{BASE_URL}/api/pages"
        response = requests.post(create_url, json=page_payload, headers=headers, timeout=TIMEOUT)
        # According to PRD, success code is 201 for creation of page
        assert response.status_code == 201, f"Page creation failed: {response.status_code} {response.text}"
        resp_json = response.json()
        assert resp_json.get("success") is True
        data = resp_json.get("data")
        assert data is not None
        created_page_id = data.get("id")

        assert created_page_id is not None, "Created page ID not found in response data"

        # Verify the returned PageResponse has the expected pageCode
        assert data.get("pageCode") == page_code

        # Now verify that VIEW, CREATE, UPDATE, DELETE permissions auto-generated for that page exist
        # We'll search permissions by pageId and permission types

        # Permissions endpoint: POST /api/permissions/search
        search_url = f"{BASE_URL}/api/permissions/search"
        search_payload = {
            "pageId": created_page_id
        }

        response = requests.post(search_url, json=search_payload, headers=headers, timeout=TIMEOUT)
        assert response.status_code == 200, f"Permissions search failed: {response.status_code} {response.text}"
        resp_json = response.json()
        assert resp_json.get("success") is True
        page_data = resp_json.get("data")
        assert page_data is not None
        permissions_list = page_data.get("content") or page_data.get("items") or page_data.get("data") or []

        # Collect permission types found in permissions list for the page
        permission_types_found = set()
        for perm in permissions_list:
            if perm.get("pageId") == created_page_id:
                permission_type = perm.get("permissionType")
                if permission_type:
                    permission_types_found.add(permission_type.upper())

        expected_permission_types = {"VIEW", "CREATE", "UPDATE", "DELETE"}

        # Check that all expected permissions exist
        missing_permissions = expected_permission_types - permission_types_found
        assert not missing_permissions, f"Missing expected permissions: {missing_permissions}"

    finally:
        # Cleanup: Deactivate the page if created
        if created_page_id:
            delete_url = f"{BASE_URL}/api/pages/{created_page_id}/deactivate"
            resp = requests.put(delete_url, headers=headers, timeout=TIMEOUT)


test_pages_create_auto_generates_permissions()
