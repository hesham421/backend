import requests

BASE_URL = "http://localhost:7272"
USERS_PATH = "/api/v1/security/users"

ADMIN_USERNAME = "admin"
ADMIN_PASSWORD = "admin"


def get_bearer_token(username: str, password: str) -> str:
    login_url = f"{BASE_URL}/api/v1/security/auth/login"
    resp = requests.post(
        login_url,
        json={"username": username, "password": password},
        timeout=30
    )
    resp.raise_for_status()
    data = resp.json()["data"]
    assert "accessToken" in data, "accessToken missing in login response"
    return data["accessToken"]


def test_get_api_v1_security_users_with_and_without_proper_authorization():
    # NOTE: a genuine "lacks PERM_SEC_USERS_VIEW" 403 case would need a second ACTIVE user without
    # that grant. New users are created PENDING_ACTIVATION and only become ACTIVE via the
    # email-delivered activation token, which this environment cannot intercept (no SMTP
    # configured) — so only the two provable authorization boundaries are exercised here:
    # a caller with the permission (admin), and no caller at all.
    token_with_perm = get_bearer_token(ADMIN_USERNAME, ADMIN_PASSWORD)

    endpoint_url = f"{BASE_URL}/api/v1/security/users"

    # Test: access with valid bearer token holding PERM_SEC_USERS_VIEW - expect 200 with paged user list
    headers = {"Authorization": f"Bearer {token_with_perm}"}
    response = requests.get(endpoint_url, headers=headers, timeout=30)
    assert response.status_code == 200, f"Expected 200, got {response.status_code}"
    json_data = response.json()["data"]
    assert isinstance(json_data, dict), "Response should be a paged JSON object"
    assert isinstance(json_data.get("content"), list), "Paged user list ('content') expected to be a list"

    # Test: access without bearer token - expect 401 unauthorized
    response_no_auth = requests.get(endpoint_url, timeout=30)
    assert response_no_auth.status_code == 401, f"Expected 401 unauthorized for missing token, got {response_no_auth.status_code}"


test_get_api_v1_security_users_with_and_without_proper_authorization()