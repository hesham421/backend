import requests

BASE_URL = "http://localhost:7272"
LOGIN_PATH = "/api/auth/login"
LOGOUT_PATH = "/api/auth/logout"
REFRESH_PATH = "/api/auth/refresh"
TIMEOUT = 30

def test_post_api_auth_logout_invalidates_refresh_token():
    login_url = BASE_URL + LOGIN_PATH
    logout_url = BASE_URL + LOGOUT_PATH
    refresh_url = BASE_URL + REFRESH_PATH

    session = requests.Session()
    try:
        # Step 1: Login to get accessToken and refresh-token cookie
        login_response = session.post(
            login_url,
            json={"username": "admin", "password": "admin"},
            timeout=TIMEOUT
        )
        assert login_response.status_code == 200, f"Login failed: {login_response.text}"
        login_json = login_response.json()
        assert "accessToken" in login_json and isinstance(login_json["accessToken"], str)
        assert "expiresIn" in login_json and (isinstance(login_json["expiresIn"], int) or isinstance(login_json["expiresIn"], float))

        # Confirm that refresh-token cookie is set
        refresh_token_cookie = None
        for cookie in session.cookies:
            if cookie.name.lower() == "refresh-token":
                refresh_token_cookie = cookie.value
                break
        assert refresh_token_cookie is not None, "Refresh token cookie not set after login"

        # Step 2: Logout - this should invalidate the refresh token, returns 204 No Content
        logout_response = session.post(logout_url, timeout=TIMEOUT)
        assert logout_response.status_code == 204, f"Logout did not return 204: {logout_response.status_code}"

        # Step 3: Attempt to refresh token with the now invalidated refresh token cookie
        # The session still holds the old refresh-token cookie
        refresh_response = session.post(refresh_url, timeout=TIMEOUT)
        assert refresh_response.status_code == 401, (
            f"Refresh after logout did not return 401 as expected, status code: {refresh_response.status_code}"
        )
    finally:
        session.close()

test_post_api_auth_logout_invalidates_refresh_token()