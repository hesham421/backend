import requests

BASE_URL = "http://localhost:7272"
LOGIN_PATH = "/api/auth/login"
UPLOAD_TOKEN_PATH = "/api/v1/files/upload-token"
UPLOAD_PATH_TEMPLATE = "/upload/{encryptedToken}"

USERNAME = "admin"
PASSWORD = "admin"
TIMEOUT = 30


def test_issue_file_upload_token_and_upload_file():
    session = requests.Session()

    try:
        # Step 1: Login to get JWT accessToken
        login_url = BASE_URL + LOGIN_PATH
        login_payload = {
            "username": USERNAME,
            "password": PASSWORD
        }
        login_resp = session.post(login_url, json=login_payload, timeout=TIMEOUT)
        assert login_resp.status_code == 200, f"Login failed with status code: {login_resp.status_code}"
        login_json = login_resp.json()
        assert login_json.get("success") is True, "Login response 'success' is not True"
        access_token = login_json.get("data", {}).get("accessToken")
        assert access_token, "accessToken missing in login response"

        auth_headers = {
            "Authorization": f"Bearer {access_token}"
        }

        # Step 2: Issue file upload token
        upload_token_url = BASE_URL + UPLOAD_TOKEN_PATH
        upload_token_payload = {
            "ownerId": 1,
            "ownerType": "USER",
            "moduleCode": "TEST_MODULE",
            "fileCategoryFk": 1
        }
        upload_token_resp = session.post(upload_token_url, json=upload_token_payload, headers=auth_headers, timeout=TIMEOUT)
        assert upload_token_resp.status_code in [200, 201], f"Upload token request failed with status code: {upload_token_resp.status_code}"
        upload_token_json = upload_token_resp.json()
        assert upload_token_json.get("success") is True, "Upload token response 'success' is not True"
        encrypted_token = upload_token_json.get("data", {}).get("encryptedToken")
        assert encrypted_token, "encryptedToken missing in upload token response data"

        # Step 3: Upload file using the encrypted token
        upload_url = BASE_URL + UPLOAD_PATH_TEMPLATE.format(encryptedToken=encrypted_token)
        files = {
            "file": ("testfile.txt", b"Test file content", "text/plain")
        }
        upload_resp = session.post(upload_url, files=files, timeout=TIMEOUT)
        assert upload_resp.status_code in [200, 201], f"File upload failed with status code: {upload_resp.status_code}"
        upload_json = upload_resp.json()
        assert upload_json.get("success") is True, "File upload response 'success' is not True"
        uploaded_file_data = upload_json.get("data")
        assert uploaded_file_data is not None, "File upload response data is missing"

        assert "fileName" in uploaded_file_data or "originalFileName" in uploaded_file_data, "Uploaded file metadata missing file name"

    finally:
        pass


test_issue_file_upload_token_and_upload_file()
