import requests
import uuid

BASE_URL = "http://localhost:7272"
TIMEOUT = 30

def test_post_api_auth_signup_account():
    signup_url = f"{BASE_URL}/api/auth/signup"

    # Generate unique username and email to avoid conflicts
    unique_suffix = uuid.uuid4().hex[:8]
    username = f"testuser_{unique_suffix}"
    email = f"testuser_{unique_suffix}@example.com"
    password = "TestPassword123!"

    signup_payload = {
        "username": username,
        "email": email,
        "password": password
    }

    try:
        # POST /api/auth/signup with valid data
        signup_response = requests.post(
            signup_url,
            json=signup_payload,
            timeout=TIMEOUT
        )
        assert signup_response.status_code == 200, f"Unexpected status code: {signup_response.status_code}, body: {signup_response.text}"
        signup_json = signup_response.json()
        # Ensure response is a JSON object
        assert isinstance(signup_json, dict), "Signup response is not a JSON object"
    except requests.exceptions.RequestException as e:
        assert False, f"Request exception during signup: {str(e)}"


test_post_api_auth_signup_account()