import requests
import time
import copy

BASE_URL = "http://localhost:7272"
AUTH_ENDPOINT = "/api/auth/login"
UPLOAD_TOKEN_ENDPOINT = "/api/v1/files/upload-token"
ACCESS_TOKEN_ENDPOINT_TEMPLATE = "/api/v1/files/{file_id}/access-token"
UPLOAD_FILE_ENDPOINT_TEMPLATE = "/upload/{encrypted_token}"
DOWNLOAD_FILE_ENDPOINT_TEMPLATE = "/download/{encrypted_token}"
DELETE_FILE_ENDPOINT_TEMPLATE = "/{encrypted_token}"

USERNAME = "admin"
PASSWORD = "admin"

TIMEOUT = 30


def test_file_management_upload_download_delete_with_token_validation():
    session = requests.Session()
    # Authenticate and get JWT token
    auth_resp = session.post(
        BASE_URL + AUTH_ENDPOINT,
        json={"username": USERNAME, "password": PASSWORD},
        timeout=TIMEOUT
    )
    assert auth_resp.status_code == 200, f"Auth failed: {auth_resp.text}"
    auth_json = auth_resp.json()
    assert auth_json.get("success") is True
    access_token = auth_json["data"]["accessToken"]
    assert access_token

    headers = {"Authorization": f"Bearer {access_token}"}

    # Prepare dummy file and owner info for upload-token request
    # Since ownerId & fileCategoryFk required, but no direct data provided, use placeholder values
    # We'll assume ownerId=1, ownerType='USER', moduleCode='TEST_MODULE', fileCategoryFk=1 for test
    upload_token_payload = {
        "ownerId": 1,
        "ownerType": "USER",
        "moduleCode": "TEST_MODULE",
        "fileCategoryFk": 1
    }

    file_document_pk = None
    encrypted_upload_token = None

    try:
        # Step 1: Issue upload token
        resp = session.post(
            BASE_URL + UPLOAD_TOKEN_ENDPOINT,
            json=upload_token_payload,
            headers=headers,
            timeout=TIMEOUT
        )
        assert resp.status_code == 201, f"Upload token issuance failed: {resp.text}"
        resp_json = resp.json()
        assert resp_json.get("success") is True
        data = resp_json.get("data")
        assert data and "encryptedToken" in data
        encrypted_upload_token = data["encryptedToken"]

        # Step 2: Upload file with token
        file_content = b"Sample file content for testing token validation"
        files = {"file": ("testfile.txt", file_content, "text/plain")}

        upload_resp = requests.post(
            BASE_URL + UPLOAD_FILE_ENDPOINT_TEMPLATE.format(encrypted_token=encrypted_upload_token),
            files=files,
            timeout=TIMEOUT
        )
        assert upload_resp.status_code == 201, f"File upload failed: {upload_resp.text}"
        upload_resp_json = upload_resp.json()
        assert upload_resp_json.get("success") is True
        upload_data = upload_resp_json.get("data")
        assert upload_data and "fileDocumentPk" in upload_data
        file_document_pk = upload_data["fileDocumentPk"]

        # Step 3: Issue download access token
        download_token_resp = session.post(
            BASE_URL + ACCESS_TOKEN_ENDPOINT_TEMPLATE.format(file_id=file_document_pk),
            json={"action": "download"},
            headers=headers,
            timeout=TIMEOUT
        )
        assert download_token_resp.status_code == 201, f"Download token issuance failed: {download_token_resp.text}"
        download_token_json = download_token_resp.json()
        assert download_token_json.get("success") is True
        download_token_data = download_token_json.get("data")
        assert download_token_data and "encryptedToken" in download_token_data
        encrypted_download_token = download_token_data["encryptedToken"]

        # Step 4: Download file with valid download token
        download_resp = requests.get(
            BASE_URL + DOWNLOAD_FILE_ENDPOINT_TEMPLATE.format(encrypted_token=encrypted_download_token),
            timeout=TIMEOUT
        )
        assert download_resp.status_code == 200
        assert download_resp.content == file_content

        # Step 5: Issue delete access token
        delete_token_resp = session.post(
            BASE_URL + ACCESS_TOKEN_ENDPOINT_TEMPLATE.format(file_id=file_document_pk),
            json={"action": "delete"},
            headers=headers,
            timeout=TIMEOUT
        )
        assert delete_token_resp.status_code == 201, f"Delete token issuance failed: {delete_token_resp.text}"
        delete_token_json = delete_token_resp.json()
        assert delete_token_json.get("success") is True
        delete_token_data = delete_token_json.get("data")
        assert delete_token_data and "encryptedToken" in delete_token_data
        encrypted_delete_token = delete_token_data["encryptedToken"]

        # Helper function to tamper token by changing last char
        def tamper_token(token: str) -> str:
            if not token:
                return token
            last_char = token[-1]
            # Change last char to next ascii char cyclically
            tampered_char = chr(((ord(last_char) + 1 - 32) % 95) + 32)
            return token[:-1] + tampered_char

        # Step 6: Validate token rejection for tampered tokens on download and delete

        # Tampered download token
        tampered_download_token = tamper_token(encrypted_download_token)
        tampered_download_resp = requests.get(
            BASE_URL + DOWNLOAD_FILE_ENDPOINT_TEMPLATE.format(encrypted_token=tampered_download_token),
            timeout=TIMEOUT
        )
        assert tampered_download_resp.status_code >= 400, "Tampered download token unexpectedly succeeded"

        # Tampered delete token
        tampered_delete_token = tamper_token(encrypted_delete_token)
        tampered_delete_resp = requests.delete(
            BASE_URL + DELETE_FILE_ENDPOINT_TEMPLATE.format(encrypted_token=tampered_delete_token),
            timeout=TIMEOUT
        )
        assert tampered_delete_resp.status_code >= 400, "Tampered delete token unexpectedly succeeded"

        # Step 7: Validate token action type enforcement
        # Try to use a download token for delete - should fail
        mismatched_action_resp = requests.delete(
            BASE_URL + DELETE_FILE_ENDPOINT_TEMPLATE.format(encrypted_token=encrypted_download_token),
            timeout=TIMEOUT
        )
        assert mismatched_action_resp.status_code >= 400, "Using download token for delete succeeded unexpectedly"

        # Try to use a delete token for download - should fail
        mismatched_action_resp2 = requests.get(
            BASE_URL + DOWNLOAD_FILE_ENDPOINT_TEMPLATE.format(encrypted_token=encrypted_delete_token),
            timeout=TIMEOUT
        )
        assert mismatched_action_resp2.status_code >= 400, "Using delete token for download succeeded unexpectedly"

        # Step 8: Validate token reuse: reuse delete token after deletion - should fail
        # First delete file with valid delete token
        del_resp = requests.delete(
            BASE_URL + DELETE_FILE_ENDPOINT_TEMPLATE.format(encrypted_token=encrypted_delete_token),
            timeout=TIMEOUT
        )
        assert del_resp.status_code == 200, f"File deletion failed: {del_resp.text}"

        # Reuse same delete token again - expect failure
        reuse_delete_resp = requests.delete(
            BASE_URL + DELETE_FILE_ENDPOINT_TEMPLATE.format(encrypted_token=encrypted_delete_token),
            timeout=TIMEOUT
        )
        assert reuse_delete_resp.status_code >= 400, "Reused delete token unexpectedly succeeded"

        # Step 9: Validate expired token rejection
        # Issue a token that expires very soon, then wait for expiration and test

        # Issue upload token with short expiration (simulate by reuse existing token, assuming expiresAt field)
        short_expiry_payload = copy.deepcopy(upload_token_payload)
        resp_exp = session.post(
            BASE_URL + UPLOAD_TOKEN_ENDPOINT,
            json=short_expiry_payload,
            headers=headers,
            timeout=TIMEOUT
        )
        assert resp_exp.status_code == 201, f"Upload token issuance for expiry test failed: {resp_exp.text}"
        res_exp_json = resp_exp.json()
        exp_data = res_exp_json.get("data")
        token_exp = exp_data.get("encryptedToken")
        expires_at = exp_data.get("expiresAt")
        assert token_exp and expires_at

        # Wait until token expiry plus 1 sec (convert expiresAt to time, if ISO8601, parse)
        from datetime import datetime, timezone
        import dateutil.parser

        expires_datetime = dateutil.parser.isoparse(expires_at)
        now_datetime = datetime.now(timezone.utc)
        sleep_seconds = (expires_datetime - now_datetime).total_seconds() + 1
        if sleep_seconds > 0:
            time.sleep(sleep_seconds)

        # Try upload with expired token, expect error
        expired_upload_resp = requests.post(
            BASE_URL + UPLOAD_FILE_ENDPOINT_TEMPLATE.format(encrypted_token=token_exp),
            files={"file": ("expired_test.txt", b"data")},
            timeout=TIMEOUT
        )
        assert expired_upload_resp.status_code >= 400, "Expired upload token unexpectedly succeeded"

    finally:
        # Cleanup: If file wasn't deleted, try to delete with valid delete token or fallback

        if file_document_pk:
            try:
                # Try to get delete token if not already deleted
                if 'encrypted_delete_token' not in locals():
                    resp_dt = session.post(
                        BASE_URL + ACCESS_TOKEN_ENDPOINT_TEMPLATE.format(file_id=file_document_pk),
                        json={"action": "delete"},
                        headers=headers,
                        timeout=TIMEOUT
                    )
                    if resp_dt.status_code == 201:
                        encrypted_delete_token = resp_dt.json().get("data", {}).get("encryptedToken")
                if 'encrypted_delete_token' in locals() and encrypted_delete_token:
                    del_resp = requests.delete(
                        BASE_URL + DELETE_FILE_ENDPOINT_TEMPLATE.format(encrypted_token=encrypted_delete_token),
                        timeout=TIMEOUT
                    )
                    # Accept 200 or 404 or 400 as means file is already gone or deleted
                    if del_resp.status_code not in (200, 404, 400):
                        raise Exception(f"File cleanup delete failed: {del_resp.text}")
            except Exception:
                pass


test_file_management_upload_download_delete_with_token_validation()
