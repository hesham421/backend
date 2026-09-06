import requests
import uuid

BASE_URL = "http://localhost:7272"
LOGIN_URL = f"{BASE_URL}/api/v1/security/auth/login"
ROLES_URL = f"{BASE_URL}/api/v1/security/roles"
TIMEOUT = 30

def test_post_api_v1_security_roles_with_valid_data():
    # Step 1: Authenticate and get Bearer token
    login_payload = {"username": "admin", "password": "admin"}
    login_resp = requests.post(LOGIN_URL, json=login_payload, timeout=TIMEOUT)
    assert login_resp.status_code == 200, f"Login failed: {login_resp.text}"
    login_json = login_resp.json()
    access_token = login_json.get("data", {}).get("accessToken")
    assert access_token, "No accessToken received in login response"

    headers = {"Authorization": f"Bearer {access_token}"}

    # Generate unique roleCode to avoid duplicates
    # Natural-key codes are normalized to UPPERCASE server-side (RULE-SEC-010), so generate uppercase
    unique_role_code = ("TEST_ROLE_" + str(uuid.uuid4())).upper()
    payload = {
        "roleCode": unique_role_code,
        "nameAr": "الاختبار",
        "nameEn": "Test Role",
        "isActiveFl": True
    }

    created_role_id = None
    try:
        # Step 2: POST to create role
        resp = requests.post(ROLES_URL, json=payload, headers=headers, timeout=TIMEOUT)
        assert resp.status_code == 201, f"Failed to create role: {resp.text}"
        resp_json = resp.json()
        data = resp_json.get("data")
        assert data is not None, "Response JSON missing data field"
        assert data.get("roleCode") == unique_role_code, "roleCode mismatch in response"
        assert data.get("nameEn") == "Test Role", "nameEn mismatch in response"
        assert data.get("nameAr") == "الاختبار", "nameAr mismatch in response"
        created_role_id = data.get("id")
        assert created_role_id is not None, "Created role has no id"
    finally:
        # Cleanup: Delete the created role if created
        if created_role_id is not None:
            delete_url = f"{ROLES_URL}/{created_role_id}"
            del_resp = requests.delete(delete_url, headers=headers, timeout=TIMEOUT)
            assert del_resp.status_code == 204, f"Failed to delete role: {del_resp.text}"

test_post_api_v1_security_roles_with_valid_data()