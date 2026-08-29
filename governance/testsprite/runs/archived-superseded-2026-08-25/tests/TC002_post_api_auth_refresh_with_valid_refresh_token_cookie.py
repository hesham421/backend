import requests

BASE_URL = "http://localhost:7272"
LOGIN_PATH = "/api/auth/login"
REFRESH_PATH = "/api/auth/refresh"
MENU_PATH = "/api/menu/user-menu"
TIMEOUT = 30


def test_post_api_auth_refresh_with_valid_refresh_token_cookie():
    session = requests.Session()

    try:
        # Step 1: Login to get refresh-token cookie set by server
        login_url = BASE_URL + LOGIN_PATH
        login_payload = {
            "username": "admin",
            "password": "admin"
        }
        login_response = session.post(login_url, json=login_payload, timeout=TIMEOUT)
        assert login_response.status_code == 200, f"Login failed with status {login_response.status_code}"
        login_json = login_response.json()
        assert "accessToken" in login_json, "Login response missing accessToken"
        assert "expiresIn" in login_json, "Login response missing expiresIn"

        # The refresh-token cookie should be set in session cookies
        refresh_token_cookie = None
        for cookie in session.cookies:
            if cookie.name == "refresh-token":
                refresh_token_cookie = cookie.value
                break
        assert refresh_token_cookie is not None, "Refresh-token cookie not set after login"

        # Step 2: Call /api/auth/refresh with the valid refresh-token cookie
        refresh_url = BASE_URL + REFRESH_PATH
        refresh_response = session.post(refresh_url, timeout=TIMEOUT)
        assert refresh_response.status_code == 200, f"Refresh token request failed with status {refresh_response.status_code}"
        refresh_json = refresh_response.json()
        assert "accessToken" in refresh_json, "Refresh response missing accessToken"
        assert isinstance(refresh_json["accessToken"], str) and refresh_json["accessToken"], "Invalid accessToken in refresh response"
        assert "expiresIn" in refresh_json, "Refresh response missing expiresIn"
        assert isinstance(refresh_json["expiresIn"], (int, float)), "Invalid expiresIn in refresh response"

        # Step 3: Use new accessToken to access a protected endpoint and verify 200 status
        access_token = refresh_json["accessToken"]
        headers = {"Authorization": f"Bearer {access_token}"}
        protected_response = requests.get(BASE_URL + MENU_PATH, headers=headers, timeout=TIMEOUT)
        assert protected_response.status_code == 200, f"Accessing protected endpoint failed with status {protected_response.status_code}"

    finally:
        session.close()


test_post_api_auth_refresh_with_valid_refresh_token_cookie()
