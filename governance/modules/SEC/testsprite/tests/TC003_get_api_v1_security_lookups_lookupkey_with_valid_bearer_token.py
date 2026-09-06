import requests

BASE_URL = "http://localhost:7272"
LOGIN_URL = f"{BASE_URL}/api/v1/security/auth/login"
LOOKUP_URL_TEMPLATE = f"{BASE_URL}/api/v1/security/lookups/{{lookupKey}}"
TIMEOUT = 30


def test_get_api_v1_security_lookups_lookupkey_with_valid_bearer_token():
    login_payload = {"username": "admin", "password": "admin"}

    try:
        # Authenticate to get access token
        login_resp = requests.post(
            LOGIN_URL, json=login_payload, timeout=TIMEOUT
        )
        assert login_resp.status_code == 200, f"Login failed: {login_resp.text}"
        login_data = login_resp.json()
        access_token = login_data.get("data", {}).get("accessToken")
        assert access_token, "accessToken not found in login response"

        # Use a valid lookupKey for security lookups (registered keys: SEC_USER_STATUS, SEC_PREFERRED_LANG)
        lookup_key = "SEC_USER_STATUS"

        headers = {
            "Authorization": f"Bearer {access_token}"
        }

        # Make GET request to lookup endpoint
        lookup_resp = requests.get(
            LOOKUP_URL_TEMPLATE.format(lookupKey=lookup_key),
            headers=headers,
            timeout=TIMEOUT
        )

        assert lookup_resp.status_code == 200, f"Lookup request failed: {lookup_resp.text}"
        lookup_data = lookup_resp.json()
        assert "data" in lookup_data, "Response missing 'data' field"
        assert isinstance(lookup_data["data"], list), "'data' field is not a list"
        assert len(lookup_data["data"]) > 0, "Lookup values list is empty"

    except requests.RequestException as e:
        assert False, f"RequestException occurred: {str(e)}"


test_get_api_v1_security_lookups_lookupkey_with_valid_bearer_token()
