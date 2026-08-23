import requests
from requests.auth import HTTPBasicAuth
import time

BASE_URL = "http://localhost:7272"
API_PREFIX = "/api/v1/files"
# AUTH_CREDENTIALS = HTTPBasicAuth("admin", "admin")  # Not used
TIMEOUT = 30

def test_verify_file_upload_download_delete_token_validation():
    session = requests.Session()
    # session.auth = AUTH_CREDENTIALS  # Not needed
    # session.timeout = TIMEOUT  # Not valid attribute
    headers = {}

    # Step 1: Get JWT access token
    auth_url = f"{BASE_URL}/api/auth/login"
    auth_payload = {
        "username": "admin",
        "password": "admin"
    }
    try:
        auth_response = requests.post(auth_url, json=auth_payload, timeout=TIMEOUT)
        assert auth_response.status_code == 200
        auth_json = auth_response.json()
        assert auth_json.get("success") is True
        access_token = auth_json["data"]["accessToken"]
    except Exception as e:
        raise AssertionError(f"Failed to get access token: {e}")

    auth_header = {"Authorization": f"Bearer {access_token}"}

    # Helper function to get upload token
    def issue_upload_token():
        url = f"{BASE_URL}{API_PREFIX}/upload-token"
        body = {
            "ownerId": 1,
            "ownerType": "USER",
            "moduleCode": "TEST_MODULE",
            "fileCategoryFk": 1
        }
        resp = requests.post(url, json=body, headers=auth_header, timeout=TIMEOUT)
        assert resp.status_code == 200
        resp_json = resp.json()
        assert resp_json.get("success") is True
        token_data = resp_json.get("data")
        assert token_data and "encryptedToken" in token_data
        return token_data["encryptedToken"]

    # Helper to upload file with encryptedToken
    def upload_file(encryptedToken):
        url = f"{BASE_URL}/upload/{encryptedToken}"
        files = {"file": ("testfile.txt", b"Hello World")}
        resp = requests.post(url, files=files, timeout=TIMEOUT)
        return resp

    # Helper to get access token for download or delete action per fileDocumentPk
    def issue_access_token(fileDocumentPk, action):
        url = f"{BASE_URL}{API_PREFIX}/{fileDocumentPk}/access-token"
        params = {"action": action}
        resp = requests.post(url, headers=auth_header, params=params, timeout=TIMEOUT)
        assert resp.status_code == 200
        resp_json = resp.json()
        assert resp_json.get("success") is True
        token_data = resp_json.get("data")
        assert token_data and "encryptedToken" in token_data
        return token_data["encryptedToken"]

    # Helper to download file bytes with encryptedToken
    def download_file(encryptedToken):
        url = f"{BASE_URL}/download/{encryptedToken}"
        resp = requests.get(url, timeout=TIMEOUT)
        return resp

    # Helper to delete file with encryptedToken
    def delete_file(encryptedToken):
        url = f"{BASE_URL}/{encryptedToken}"
        resp = requests.delete(url, timeout=TIMEOUT)
        return resp

    # Issue upload token
    encrypted_upload_token = issue_upload_token()

    fileDocumentPk = None
    try:
        # Upload file with valid token
        upload_resp = upload_file(encrypted_upload_token)
        assert upload_resp.status_code == 200
        upload_json = upload_resp.json()
        assert upload_json.get("success") is True
        upload_data = upload_json.get("data")
        assert upload_data and "fileDocumentPk" in upload_data
        fileDocumentPk = upload_data["fileDocumentPk"]

        # Issue download token for the uploaded file
        encrypted_download_token = issue_access_token(fileDocumentPk, "download")

        # Issue delete token for the uploaded file
        encrypted_delete_token = issue_access_token(fileDocumentPk, "delete")

        # Tampered token: modify token string and expect rejection
        tampered_upload_token = encrypted_upload_token[:-1] + ('A' if encrypted_upload_token[-1] != 'A' else 'B')
        tampered_upload_resp = upload_file(tampered_upload_token)
        assert tampered_upload_resp.status_code >= 400
        tampered_upload_json = tampered_upload_resp.json()
        assert tampered_upload_json.get("success") is False

        tampered_download_token = encrypted_download_token[:-1] + ('A' if encrypted_download_token[-1] != 'A' else 'B')
        tampered_download_resp = download_file(tampered_download_token)
        assert tampered_download_resp.status_code >= 400

        tampered_delete_token = encrypted_delete_token[:-1] + ('A' if encrypted_delete_token[-1] != 'A' else 'B')
        tampered_delete_resp = delete_file(tampered_delete_token)
        assert tampered_delete_resp.status_code >= 400
        try:
            tampered_delete_json = tampered_delete_resp.json()
            assert tampered_delete_json.get("success") is False
        except:
            pass

        # Wrong-action token usage: use upload token on download or delete endpoint and vice versa
        wrong_action_download_resp = download_file(encrypted_upload_token)
        assert wrong_action_download_resp.status_code >= 400

        wrong_action_delete_resp = delete_file(encrypted_upload_token)
        assert wrong_action_delete_resp.status_code >= 400

        wrong_action_upload_resp = upload_file(encrypted_download_token)
        assert wrong_action_upload_resp.status_code >= 400

        wrong_action_delete_resp_2 = delete_file(encrypted_download_token)
        assert wrong_action_delete_resp_2.status_code >= 400

        wrong_action_upload_resp_2 = upload_file(encrypted_delete_token)
        assert wrong_action_upload_resp_2.status_code >= 400

        wrong_action_download_resp_2 = download_file(encrypted_delete_token)
        assert wrong_action_download_resp_2.status_code >= 400

        # Reused token on delete endpoint (expect rejection after first use)
        delete_resp = delete_file(encrypted_delete_token)
        assert delete_resp.status_code == 200
        delete_json = delete_resp.json()
        assert delete_json.get("success") is True

        reuse_delete_resp = delete_file(encrypted_delete_token)
        assert reuse_delete_resp.status_code >= 400
        try:
            reuse_delete_json = reuse_delete_resp.json()
            assert reuse_delete_json.get("success") is False
        except:
            pass

        post_delete_download_resp = download_file(encrypted_download_token)
        assert post_delete_download_resp.status_code >= 400

    finally:
        if fileDocumentPk is not None:
            try:
                new_delete_token = issue_access_token(fileDocumentPk, "delete")
                delete_file(new_delete_token)
            except:
                pass

test_verify_file_upload_download_delete_token_validation()
