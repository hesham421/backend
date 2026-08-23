import requests
from requests.auth import HTTPBasicAuth
import time

BASE_URL = "http://localhost:7272/actuator/health"
API_BASE = "http://localhost:7272"
AUTH_CREDENTIALS = ("admin", "admin")
LOGIN_PATH = "/api/auth/login"
UPLOAD_TOKEN_PATH = "/api/v1/files/upload-token"
TIMEOUT = 30

def get_jwt_token():
    """Authenticate using basic token (username, password) to get JWT access token."""
    login_url = API_BASE + LOGIN_PATH
    payload = {
        "username": AUTH_CREDENTIALS[0],
        "password": AUTH_CREDENTIALS[1]
    }
    try:
        response = requests.post(login_url, json=payload, timeout=TIMEOUT)
    except Exception:
        raise
    assert response.status_code == 200, f"Login failed status {response.status_code}"
    body = response.json()
    assert body.get("success") is True, "Login response success=false"
    data = body.get("data")
    assert data and "accessToken" in data, "accessToken missing in login response"
    return data["accessToken"]

def test_postapiv1filesuploadtokenissueuploadtoken():
    access_token = get_jwt_token()
    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json"
    }

    url = API_BASE + UPLOAD_TOKEN_PATH

    # Valid request data
    valid_payload = {
        "ownerId": 1,
        "ownerType": "TestOwnerType",
        "moduleCode": "TEST_MODULE",
        "fileCategoryFk": 100
    }

    # 1. Test success case with valid data
    try:
        resp = requests.post(url, json=valid_payload, headers=headers, timeout=TIMEOUT)
    except Exception:
        raise
    assert resp.status_code == 201, f"Expected 201 Created, got {resp.status_code}"
    body = resp.json()
    assert body.get("success") is True, "Success flag not true on valid upload token request"
    data = body.get("data")
    assert data, "Data missing in response"
    assert "encryptedToken" in data and isinstance(data["encryptedToken"], str) and data["encryptedToken"], "encryptedToken missing or empty"
    assert "expiresAt" in data and isinstance(data["expiresAt"], str) and data["expiresAt"], "expiresAt missing or empty"

    # 2. Test error responses for missing or invalid data
    error_test_cases = [
        # Missing ownerId
        ({"ownerType": "TestOwnerType", "moduleCode": "TEST_MODULE", "fileCategoryFk": 100}, 400),
        # Missing ownerType
        ({"ownerId": 1, "moduleCode": "TEST_MODULE", "fileCategoryFk": 100}, 400),
        # Missing moduleCode
        ({"ownerId": 1, "ownerType": "TestOwnerType", "fileCategoryFk": 100}, 400),
        # Missing fileCategoryFk
        ({"ownerId": 1, "ownerType": "TestOwnerType", "moduleCode": "TEST_MODULE"}, 400),
        # Invalid ownerId type
        ({"ownerId": "invalid_id", "ownerType": "TestOwnerType", "moduleCode": "TEST_MODULE", "fileCategoryFk": 100}, 400),
        # Invalid fileCategoryFk type
        ({"ownerId": 1, "ownerType": "TestOwnerType", "moduleCode": "TEST_MODULE", "fileCategoryFk": "invalid_fk"}, 400),
        # Empty moduleCode
        ({"ownerId": 1, "ownerType": "TestOwnerType", "moduleCode": "", "fileCategoryFk": 100}, 400),
        # Empty ownerType
        ({"ownerId": 1, "ownerType": "", "moduleCode": "TEST_MODULE", "fileCategoryFk": 100}, 400),
    ]

    for payload, expected_status in error_test_cases:
        try:
            resp = requests.post(url, json=payload, headers=headers, timeout=TIMEOUT)
        except Exception:
            raise
        # The API might respond 400 or 422 or 200 with success false; check for failure
        if resp.status_code == 201:
            body = resp.json()
            assert body.get("success") is False, f"Expected failure success=false but got success=true for payload {payload}"
        else:
            assert resp.status_code == expected_status, f"Expected status {expected_status} for payload {payload}, got {resp.status_code}"

        # When response is json with success false, check error details presence
        try:
            body = resp.json()
        except Exception:
            continue
        if body.get("success") is False:
            error = body.get("error", {})
            assert "code" in error, f"Error code missing in error response for payload {payload}"
            assert "details" in error, f"Error details missing in error response for payload {payload}"
            assert "path" in error, f"Error path missing in error response for payload {payload}"

test_postapiv1filesuploadtokenissueuploadtoken()