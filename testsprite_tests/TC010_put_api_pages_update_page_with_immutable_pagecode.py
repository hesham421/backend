import requests

BASE_URL = "http://localhost:7272"
LOGIN_URL = f"{BASE_URL}/api/auth/login"
PAGES_URL = f"{BASE_URL}/api/pages"

AUTH_USERNAME = "admin"
AUTH_PASSWORD = "admin"
TIMEOUT = 30

def test_put_api_pages_update_page_with_immutable_pagecode():
    session = requests.Session()
    headers = {}
    try:
        # Step 1: Login to get JWT token
        login_payload = {"username": AUTH_USERNAME, "password": AUTH_PASSWORD}
        login_resp = session.post(LOGIN_URL, json=login_payload, timeout=TIMEOUT)
        assert login_resp.status_code == 200, f"Login failed: {login_resp.text}"
        login_data = login_resp.json()
        access_token = login_data.get("accessToken")
        assert access_token, "No accessToken received from login"
        headers = {
            "Authorization": f"Bearer {access_token}",
            "Content-Type": "application/json"
        }

        # Step 2: Create a new page to update (since no page ID provided)
        create_payload = {
            "pageCode": "immutableCodeTest123",
            "nameAr": "اختبار الصفحة",
            "nameEn": "Test Page",
            "route": "/test-page",
            "icon": "test-icon",
            "module": "TestModule",
            "parentId": None,
            "displayOrder": 1000,
            "active": True,
            "description": "Page created for immutable pageCode update test",
            "suppressPermissionTypes": []
        }
        create_resp = session.post(PAGES_URL, json=create_payload, headers=headers, timeout=TIMEOUT)
        assert create_resp.status_code == 200, f"Page creation failed: {create_resp.text}"
        created_page = create_resp.json()
        page_id = created_page.get("id")
        assert page_id is not None, "Created page has no id"
        original_page_code = created_page.get("pageCode")
        assert original_page_code == create_payload["pageCode"], "Created pageCode mismatch"

        # Step 3: Update page details without changing pageCode (should succeed)
        update_payload_valid = {
            "nameAr": "تحديث الصفحة",
            "nameEn": "Updated Test Page",
            "route": "/updated-test-page",
            "icon": "updated-icon",
            "module": "UpdatedModule",
            "parentId": None,
            "displayOrder": 2000,
            "active": False,
            "description": "Page updated without changing pageCode",
            "suppressPermissionTypes": ["UPDATE"]
        }
        update_resp_valid = session.put(f"{PAGES_URL}/{page_id}", json=update_payload_valid, headers=headers, timeout=TIMEOUT)
        assert update_resp_valid.status_code == 200, f"Page update without pageCode change failed: {update_resp_valid.text}"
        updated_page_valid = update_resp_valid.json()
        # Confirm updates applied
        for key in update_payload_valid:
            # suppressPermissionTypes can be empty or unchanged, ensure contains updated value
            if key == "suppressPermissionTypes":
                assert set(updated_page_valid.get(key, [])) == set(update_payload_valid[key]), f"{key} not updated correctly"
            else:
                assert updated_page_valid.get(key) == update_payload_valid[key], f"{key} not updated correctly"
        # Confirm pageCode unchanged
        assert updated_page_valid.get("pageCode") == original_page_code, "pageCode changed unexpectedly"

        # Step 4: Attempt to update pageCode in PUT (should be rejected or ignored)
        update_payload_invalid = {
            "pageCode": "newImmutableCode999",
            "nameAr": "محاولة تغيير pageCode",
            "nameEn": "Trying to change pageCode",
            "route": "/try-change-pagecode",
            "icon": "icon-change",
            "module": "ModuleChange",
            "parentId": None,
            "displayOrder": 3000,
            "active": True,
            "description": "Attempt to change immutable pageCode",
            "suppressPermissionTypes": []
        }
        update_resp_invalid = session.put(f"{PAGES_URL}/{page_id}", json=update_payload_invalid, headers=headers, timeout=TIMEOUT)
        # According to PRD, attempt to change pageCode may cause validation failure, or pageCode must remain original
        # Accept 200 with pageCode unchanged or validation error (likely 400)
        if update_resp_invalid.status_code == 200:
            updated_page_invalid = update_resp_invalid.json()
            # Confirm pageCode was NOT changed
            assert updated_page_invalid.get("pageCode") == original_page_code, "pageCode should be immutable and not changed"
            # Other fields may be updated except pageCode
            for key in update_payload_invalid:
                if key == "pageCode":
                    continue
                if key == "suppressPermissionTypes":
                    assert set(updated_page_invalid.get(key, [])) == set(update_payload_invalid[key])
                else:
                    assert updated_page_invalid.get(key) == update_payload_invalid[key]
        else:
            # Expect validation error, probably 400
            assert update_resp_invalid.status_code in (400, 422), f"Unexpected status code on pageCode change attempt: {update_resp_invalid.status_code}"
        # End test steps

    finally:
        # Cleanup: delete the created page
        if 'page_id' in locals() and headers:
            del_resp = session.delete(f"{PAGES_URL}/{page_id}", headers=headers, timeout=TIMEOUT)
            # Accept 204 No Content or 200 OK for deletion (not specified in PRD, so accept 204 ideally)
            assert del_resp.status_code in (200, 204), f"Failed to delete page, status: {del_resp.status_code}, response: {del_resp.text}"

test_put_api_pages_update_page_with_immutable_pagecode()
