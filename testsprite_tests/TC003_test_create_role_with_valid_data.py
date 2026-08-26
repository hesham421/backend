import requests
import random
import string

BASE_URL = "http://localhost:7272"
LOGIN_ENDPOINT = "/api/auth/login"
ROLES_ENDPOINT = "/api/roles"

ADMIN_USERNAME = "admin"
ADMIN_PASSWORD = "admin"
TIMEOUT = 30


def generate_role_code_suffix(length=8):
    hex_digits = string.hexdigits.upper()
    allowed_chars = "0123456789ABCDEF"
    # Generate uppercase hex/digits only suffix
    suffix = ''.join(random.choice(allowed_chars) for _ in range(length))
    return suffix


def login_and_get_token():
    url = BASE_URL + LOGIN_ENDPOINT
    auth_payload = {"username": ADMIN_USERNAME, "password": ADMIN_PASSWORD}
    resp = requests.post(url, json=auth_payload, timeout=TIMEOUT)
    resp.raise_for_status()
    json_resp = resp.json()
    assert json_resp.get("success") is True, "Login unsuccessful"
    access_token = json_resp.get("data", {}).get("accessToken")
    assert access_token, "No accessToken in login response"
    return access_token


def test_create_role_with_valid_data():
    access_token = login_and_get_token()
    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json"
    }

    suffix = generate_role_code_suffix()
    role_code = f"TEST_ROLE_{suffix}"
    role_name = f"Test Role {suffix}"
    description = f"Description for {role_name}"

    create_role_payload = {
        "roleCode": role_code,
        "roleName": role_name,
        "description": description
    }

    role_id = None

    try:
        create_resp = requests.post(BASE_URL + ROLES_ENDPOINT, json=create_role_payload, headers=headers, timeout=TIMEOUT)
        # Should return 201 Created
        assert create_resp.status_code == 201, f"Unexpected status code: {create_resp.status_code}, response text: {create_resp.text}"
        create_json = create_resp.json()
        assert create_json.get("success") is True, "Role creation failed with success != True"
        role_data = create_json.get("data")
        assert role_data, "No data field in role creation response"
        # Validate roleCode pattern server side also returns roleCode matching input (case sensitive)
        returned_role_code = role_data.get("roleCode")
        assert returned_role_code == role_code, f"Returned roleCode mismatch: expected {role_code}, got {returned_role_code}"
        # Check returned data contains id
        role_id = role_data.get("id")
        assert role_id is not None, "Role ID missing in response data"
    finally:
        # Always try to delete created role to avoid test data leakage
        if role_id:
            delete_resp = requests.delete(f"{BASE_URL}{ROLES_ENDPOINT}/{role_id}", headers=headers, timeout=TIMEOUT)
            # Deletion should be 204 No Content
            assert delete_resp.status_code == 204, f"Role deletion failed with status {delete_resp.status_code}, response: {delete_resp.text}"


test_create_role_with_valid_data()