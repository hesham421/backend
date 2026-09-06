import requests

BASE_URL = "http://localhost:7272"
LOGIN_ENDPOINT = "/api/v1/security/auth/login"
PERMISSIONS_ENDPOINT = "/api/v1/security/permissions"
REQUEST_TIMEOUT = 30


def test_get_permissions_with_filters_and_bearer_token():
    # Step 1: Login and get the access token
    login_url = BASE_URL + LOGIN_ENDPOINT
    login_payload = {"username": "admin", "password": "admin"}
    try:
        login_resp = requests.post(login_url, json=login_payload, timeout=REQUEST_TIMEOUT)
        login_resp.raise_for_status()
    except requests.RequestException as e:
        assert False, f"Login request failed: {e}"
    login_json = login_resp.json()
    assert "data" in login_json and "accessToken" in login_json["data"], "No accessToken in login response"
    access_token = login_json["data"]["accessToken"]

    # Step 2: Prepare headers with Bearer token
    headers = {
        "Authorization": f"Bearer {access_token}"
    }

    # Step 3: Prepare filter query parameters - optional filters; demonstrate usage with sample filters
    # Example filters to test (can be adjusted or removed for empty filters)
    params = {
        "pageFk": None,
        "moduleFk": None,
        "permissionType": "VIEW",
        "page": 0,
        "size": 20
    }
    # Remove keys with None values since they are optional and should not be sent as query params if None
    query_params = {k: v for k, v in params.items() if v is not None}

    # Step 4: Make GET request to permissions endpoint with filters and bearer token
    permissions_url = BASE_URL + PERMISSIONS_ENDPOINT
    try:
        resp = requests.get(permissions_url, headers=headers, params=query_params, timeout=REQUEST_TIMEOUT)
        resp.raise_for_status()
    except requests.RequestException as e:
        assert False, f"Permissions GET request failed: {e}"

    # Step 5: Validate response
    resp_json = resp.json()
    assert resp.status_code == 200, f"Expected status code 200 but got {resp.status_code}"
    # The API wraps successful response in {"data": ...}
    assert "data" in resp_json, "Response JSON has no 'data' field"
    data = resp_json["data"]
    # Validate pagination page info keys inside data (typical Page object keys)
    required_keys = ["content", "pageable", "totalElements", "totalPages", "number", "size"]
    for key in required_keys:
        assert key in data, f"Missing pagination key '{key}' in response data"
    # Validate content is a list (list of permissions)
    assert isinstance(data["content"], list), "Response data.content is not a list"

    # Optional: check filtered permissionType values if content is non-empty
    for permission in data["content"]:
        # permissionType may be under permission dict; check if present and match filter
        if "permissionType" in permission:
            assert permission["permissionType"] == query_params.get("permissionType"), \
                f"PermissionType mismatch: expected {query_params.get('permissionType')} but got {permission['permissionType']}"


test_get_permissions_with_filters_and_bearer_token()