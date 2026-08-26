import requests
from requests.auth import HTTPBasicAuth

base_url = "http://localhost:7272"
login_url = f"{base_url}/api/auth/login"
upload_token_url = f"{base_url}/api/v1/files/upload-token"

auth_cred = HTTPBasicAuth("admin", "admin")
timeout_sec = 30


def test_issue_file_upload_token_and_upload_file():
    # Step 1: Login to get JWT access token
    login_payload = {"username": "admin", "password": "admin"}
    login_headers = {"Content-Type": "application/json"}
    login_response = requests.post(
        login_url, json=login_payload, headers=login_headers, auth=auth_cred, timeout=timeout_sec
    )
    assert login_response.status_code == 200, f"Login failed: {login_response.text}"
    login_json = login_response.json()
    assert login_json.get("success") is True, f"Login not successful: {login_json}"
    access_token = login_json.get("data", {}).get("accessToken")
    assert access_token, "No accessToken received in login response"

    auth_headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json",
    }

    # Step 2: Prepare upload token request payload
    # Using ownerId as 1 (admin user), ownerType 'USER', moduleCode 'TEST_MODULE', fileCategoryFk 1 (example)
    upload_token_payload = {
        "ownerId": 1,
        "ownerType": "USER",
        "moduleCode": "TEST_MODULE",
        "fileCategoryFk": 1
    }

    # Step 3: POST to /api/v1/files/upload-token to get encrypted upload token
    resp_upload_token = requests.post(
        upload_token_url,
        json=upload_token_payload,
        headers=auth_headers,
        timeout=timeout_sec
    )
    assert resp_upload_token.status_code == 201, f"Expected status 201, got {resp_upload_token.status_code}, content: {resp_upload_token.text}"
    resp_upload_token_json = resp_upload_token.json()
    assert resp_upload_token_json.get("success") is True, f"Upload token response unsuccessful: {resp_upload_token_json}"
    encrypted_token = resp_upload_token_json.get("data", {}).get("encryptedToken")
    assert encrypted_token, "No encryptedToken in upload token response data"

    # Step 4: POST multipart file to /upload/{encryptedToken}
    upload_url = f"{base_url}/upload/{encrypted_token}"

    # Prepare a dummy file to upload (in-memory bytes with a filename)
    file_content = b"Test file content for upload token test."
    files = {
        "file": ("test_upload.txt", file_content, "text/plain")
    }

    # No Authorization header for /upload call (auth bypassed; token gating via encryptedToken)
    resp_upload = requests.post(
        upload_url,
        files=files,
        timeout=timeout_sec
    )
    assert resp_upload.status_code == 201, f"Expected status 201, got {resp_upload.status_code}, content: {resp_upload.text}"
    resp_upload_json = resp_upload.json()
    assert resp_upload_json.get("success") is True, f"File upload unsuccessful: {resp_upload_json}"
    file_data = resp_upload_json.get("data")
    assert file_data is not None, "No data object in file upload response"
    assert "fileNameOriginal" in file_data, f"'fileNameOriginal' key missing in upload response data: {file_data}"
    assert file_data["fileNameOriginal"] == "test_upload.txt", f"Unexpected fileNameOriginal: {file_data['fileNameOriginal']}"


test_issue_file_upload_token_and_upload_file()