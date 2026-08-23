import requests
import time

BASE_URL = "http://localhost:7272"
AUTH_ENDPOINT = "/api/auth/login"
UPLOAD_TOKEN_ENDPOINT = "/api/v1/files/upload-token"
ACCESS_TOKEN_ENDPOINT_TEMPLATE = "/api/v1/files/{fileDocumentPk}/access-token"
UPLOAD_ENDPOINT_TEMPLATE = "/upload/{encryptedToken}"
DOWNLOAD_ENDPOINT_TEMPLATE = "/download/{encryptedToken}"
DELETE_ENDPOINT_TEMPLATE = "/{encryptedToken}"

USERNAME = "admin"
PASSWORD = "admin"

FILE_CONTENT = b"Sample file content for testing."
FILE_NAME = "testfile.txt"

def verify_file_upload_download_delete_token_validation():
    session = requests.Session()
    timeout = 30

    # 1. Login to get JWT token
    login_payload = {"username": USERNAME, "password": PASSWORD}
    login_resp = session.post(f"{BASE_URL}/api/auth/login", json=login_payload, timeout=timeout)
    assert login_resp.status_code == 200, f"Login failed with status {login_resp.status_code}"
    login_json = login_resp.json()
    assert login_json.get("success") is True, "Login response success false"
    access_token = login_json["data"]["accessToken"]
    assert access_token, "No accessToken received"

    headers = {
        "Authorization": f"Bearer {access_token}"
    }

    # For issuing upload token, need ownerId, ownerType, moduleCode, fileCategoryFk
    # Since test does not provide them, attempt generic placeholders; adapt as needed.
    # We assume 1 for ownerId and fileCategoryFk, "USER" for ownerType, "TEST_MODULE" for moduleCode for test purpose.
    upload_token_payload = {
        "ownerId": 1,
        "ownerType": "USER",
        "moduleCode": "TEST_MODULE",
        "fileCategoryFk": 1
    }

    upload_token_resp = session.post(f"{BASE_URL}{UPLOAD_TOKEN_ENDPOINT}", json=upload_token_payload, headers=headers, timeout=timeout)
    assert upload_token_resp.status_code == 200, f"Upload token request failed: {upload_token_resp.status_code}"
    upload_token_json = upload_token_resp.json()
    assert upload_token_json.get("success") is True, "Upload token response success false"
    encrypted_upload_token = upload_token_json["data"]["encryptedToken"]
    assert encrypted_upload_token, "No encryptedToken in upload token response"

    # 2. Upload a file with the encrypted token
    upload_url = f"{BASE_URL}{UPLOAD_ENDPOINT_TEMPLATE.format(encryptedToken=encrypted_upload_token)}"
    files = {"file": (FILE_NAME, FILE_CONTENT, "text/plain")}
    upload_resp = requests.post(upload_url, files=files, timeout=timeout)
    assert upload_resp.status_code == 200, f"File upload failed: {upload_resp.status_code}"
    upload_resp_json = upload_resp.json()
    assert upload_resp_json.get("success") is True, "File upload success false"
    uploaded_file_data = upload_resp_json.get("data")
    assert uploaded_file_data and "fileDocumentPk" in uploaded_file_data, "No fileDocumentPk in upload response"
    file_document_pk = uploaded_file_data["fileDocumentPk"]

    # 3. Issue download access token
    download_token_url = f"{BASE_URL}{ACCESS_TOKEN_ENDPOINT_TEMPLATE.format(fileDocumentPk=file_document_pk)}?action=download"
    download_token_resp = session.post(download_token_url, headers=headers, timeout=timeout)
    assert download_token_resp.status_code == 200, f"Download token request failed: {download_token_resp.status_code}"
    download_token_json = download_token_resp.json()
    assert download_token_json.get("success") is True, "Download token response success false"
    encrypted_download_token = download_token_json["data"]["encryptedToken"]
    assert encrypted_download_token, "No encryptedToken in download token response"

    # 4. Issue delete access token
    delete_token_url = f"{BASE_URL}{ACCESS_TOKEN_ENDPOINT_TEMPLATE.format(fileDocumentPk=file_document_pk)}?action=delete"
    delete_token_resp = session.post(delete_token_url, headers=headers, timeout=timeout)
    assert delete_token_resp.status_code == 200, f"Delete token request failed: {delete_token_resp.status_code}"
    delete_token_json = delete_token_resp.json()
    assert delete_token_json.get("success") is True, "Delete token response success false"
    encrypted_delete_token = delete_token_json["data"]["encryptedToken"]
    assert encrypted_delete_token, "No encryptedToken in delete token response"

    # Helper function to test token behavior on upload, download, delete endpoints
    def test_token_validation(token, endpoint_path_template, method, expected_status=400):
        url = f"{BASE_URL}{endpoint_path_template.format(encryptedToken=token)}"
        try:
            if method == 'POST':
                # For upload endpoint: send empty file to provoke error if token invalid
                files = {"file": (FILE_NAME, FILE_CONTENT, "text/plain")} if "upload" in endpoint_path_template else None
                resp = requests.post(url, files=files, timeout=timeout)
            elif method == 'GET':
                resp = requests.get(url, timeout=timeout)
            elif method == 'DELETE':
                resp = requests.delete(url, timeout=timeout)
            else:
                raise ValueError("Unsupported HTTP method")
        except requests.exceptions.RequestException as e:
            return False, str(e)

        # Check that the response indicates token rejection; typically 4xx status and success false in JSON
        if resp.status_code == 200 and resp.headers.get('Content-Type', '').startswith('application/json'):
            try:
                resp_json = resp.json()
                # Token validation failures should have success==false according to PRD error response envelope
                if resp_json.get("success") is False:
                    return True, resp_json.get("message", "Token validation rejected")
                else:
                    return False, "Token unexpectedly accepted"
            except Exception as e:
                return False, f"JSON decode error: {str(e)}"
        elif resp.status_code in {401, 403, 400, 422}:
            return True, f"Rejected with status {resp.status_code}"
        else:
            # For binary download endpoint, no JSON expected on success; error returns JSON with success false
            if method == 'GET' and resp.status_code != 200:
                return True, f"Rejected with status {resp.status_code}"
            return False, f"Unexpected response: {resp.status_code}"

    # 5. Successful download test before expiry
    download_url = f"{BASE_URL}{DOWNLOAD_ENDPOINT_TEMPLATE.format(encryptedToken=encrypted_download_token)}"
    download_resp = requests.get(download_url, timeout=timeout)
    assert download_resp.status_code == 200, f"File download failed: {download_resp.status_code}"
    assert download_resp.content == FILE_CONTENT, "Downloaded file content mismatch"

    # 6. Test expired token:
    # To test expired tokens, we simulate by creating a token with immediate expiry if possible - here we only can wait.
    # But since we don't have control over token expiry, attempt to sleep beyond expiry time from upload token response.
    # Use expiresAt from upload token response to calculate wait time.
    import datetime
    from dateutil import parser as dateparser

    upload_token_expiry_str = upload_token_json["data"].get("expiresAt")
    if upload_token_expiry_str:
        expiry_dt = dateparser.parse(upload_token_expiry_str)
        now = datetime.datetime.utcnow().replace(tzinfo=datetime.timezone.utc)
        wait_seconds = (expiry_dt - now).total_seconds()
        if wait_seconds > 0:
            # Sleep for wait_seconds + 1 to ensure expiry
            time.sleep(wait_seconds + 1)

            # Test upload with expired token
            ok, msg = test_token_validation(encrypted_upload_token, UPLOAD_ENDPOINT_TEMPLATE, 'POST')
            assert ok, f"Expired upload token not rejected: {msg}"

            # Test download with expired token
            ok, msg = test_token_validation(encrypted_download_token, DOWNLOAD_ENDPOINT_TEMPLATE, 'GET')
            assert ok, f"Expired download token not rejected: {msg}"

            # Test delete with expired token
            ok, msg = test_token_validation(encrypted_delete_token, DELETE_ENDPOINT_TEMPLATE, 'DELETE')
            assert ok, f"Expired delete token not rejected: {msg}"
        else:
            # Expiry already passed or invalid, cannot test expiry
            pass

    # 7. Test tampered token: alter last char(s)
    def tamper_token(token):
        if len(token) < 2:
            return token + "X"
        return token[:-1] + ("X" if token[-1] != "X" else "Y")

    tampered_upload_token = tamper_token(encrypted_upload_token)
    tampered_download_token = tamper_token(encrypted_download_token)
    tampered_delete_token = tamper_token(encrypted_delete_token)

    ok, msg = test_token_validation(tampered_upload_token, UPLOAD_ENDPOINT_TEMPLATE, 'POST')
    assert ok, f"Tampered upload token not rejected: {msg}"

    ok, msg = test_token_validation(tampered_download_token, DOWNLOAD_ENDPOINT_TEMPLATE, 'GET')
    assert ok, f"Tampered download token not rejected: {msg}"

    ok, msg = test_token_validation(tampered_delete_token, DELETE_ENDPOINT_TEMPLATE, 'DELETE')
    assert ok, f"Tampered delete token not rejected: {msg}"

    # 8. Test reused token on delete endpoint: using same delete token twice should reject the second call

    delete_url = f"{BASE_URL}{DELETE_ENDPOINT_TEMPLATE.format(encryptedToken=encrypted_delete_token)}"
    # First delete attempt - should succeed with 200
    first_del_resp = requests.delete(delete_url, timeout=timeout)
    assert first_del_resp.status_code == 200, f"First delete request failed: {first_del_resp.status_code}"
    first_del_json = first_del_resp.json()
    assert first_del_json.get("success") is True, "First delete response success false"

    # Second delete attempt with same token - should be rejected
    second_del_resp = requests.delete(delete_url, timeout=timeout)
    # Expected rejection due to reused token; success:false error response or 4xx
    if second_del_resp.status_code == 200:
        try:
            resp_json = second_del_resp.json()
            assert resp_json.get("success") is False, "Reused delete token unexpectedly accepted"
        except Exception:
            assert False, "Reused delete token returned invalid JSON"
    else:
        # Accepted rejection by status code different than 200
        assert second_del_resp.status_code in {401, 403, 400, 422}, f"Unexpected status for reused delete token: {second_del_resp.status_code}"

    # 9. Test wrong-action token: e.g. use upload token on download endpoint, download token on delete endpoint, delete token on upload endpoint

    # Upload token used for download endpoint
    ok, msg = test_token_validation(encrypted_upload_token, DOWNLOAD_ENDPOINT_TEMPLATE, 'GET')
    assert ok, f"Wrong-action (upload token on download) not rejected: {msg}"

    # Download token used for delete endpoint
    ok, msg = test_token_validation(encrypted_download_token, DELETE_ENDPOINT_TEMPLATE, 'DELETE')
    assert ok, f"Wrong-action (download token on delete) not rejected: {msg}"

    # Delete token used for upload endpoint
    ok, msg = test_token_validation(encrypted_delete_token, UPLOAD_ENDPOINT_TEMPLATE, 'POST')
    assert ok, f"Wrong-action (delete token on upload) not rejected: {msg}"


verify_file_upload_download_delete_token_validation()