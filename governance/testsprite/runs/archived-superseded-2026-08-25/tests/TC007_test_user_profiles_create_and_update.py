import requests

BASE_URL = "http://localhost:7272"
USERNAME = "admin"
PASSWORD = "admin"
TIMEOUT = 30


def test_user_profiles_create_and_update():
    session = requests.Session()

    # Login to get JWT token
    login_payload = {
        "username": USERNAME,
        "password": PASSWORD
    }
    login_resp = session.post(f"{BASE_URL}/api/auth/login", json=login_payload, timeout=TIMEOUT)
    login_resp.raise_for_status()
    login_body = login_resp.json()
    assert login_body["success"] is True
    access_token = login_body["data"]["accessToken"]
    assert isinstance(access_token, str) and access_token

    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {access_token}"
    }

    # Helper to create a user for profile linkage
    def create_user():
        user_payload = {
            "username": "testuser_prof",
            "password": "TestPass123!",
            "roleNames": ["User"]  # Provide at least one valid role name
        }
        resp = session.post(f"{BASE_URL}/api/users", json=user_payload, headers=headers, timeout=TIMEOUT)
        assert resp.status_code == 201
        body = resp.json()
        assert body["success"] is True
        user_data = body["data"]
        # user id field is 'id' as per spec
        return user_data["id"]

    # Helper to delete user
    def delete_user(user_id):
        resp = session.delete(f"{BASE_URL}/api/users/{user_id}", headers=headers, timeout=TIMEOUT)
        # Allow 204 or 404 if already deleted; ignore 409 error (in use) for cleanup
        if resp.status_code not in (204, 404):
            resp.raise_for_status()

    # Helper to create a branch for profile linkage
    def create_branch(legal_entity_id):
        branch_payload = {
            "legalEntityFk": legal_entity_id,
            "nameAr": "فرع للاختبار",
            "nameEn": "Test Branch",
            "branchTypeId": 1,
            "notes": "Branch for user profile test"
        }
        resp = session.post(f"{BASE_URL}/api/v1/org/branches", json=branch_payload, headers=headers, timeout=TIMEOUT)
        assert resp.status_code == 201
        body = resp.json()
        assert body["success"] is True
        branch_data = body["data"]
        return branch_data["id"]

    # Helper to delete branch
    def delete_branch(branch_id):
        resp = session.delete(f"{BASE_URL}/api/v1/org/branches/{branch_id}", headers=headers, timeout=TIMEOUT)
        # Allow 204 or 404 if already deleted; ignore 409 error (in use) for cleanup
        if resp.status_code not in (204, 404):
            resp.raise_for_status()

    # Helper to get or create a default legal entity
    def get_or_create_legal_entity():
        # Search legal entities first
        resp = session.post(f"{BASE_URL}/api/v1/org/legal-entities/search", json={}, headers=headers, timeout=TIMEOUT)
        resp.raise_for_status()
        body = resp.json()
        if body["success"] and body.get("data") and body["data"].get("content"):
            legal_entities = body["data"]["content"]
            if legal_entities:
                ent = legal_entities[0]
                return ent["id"]
        # No legal entity found, create one
        legal_entity_payload = {
            "nameAr": "جهة قانونية للاختبار",
            "nameEn": "Test Legal Entity",
            "entityTypeId": 1,
            "notes": "Legal entity for user profile test"
        }
        resp = session.post(f"{BASE_URL}/api/v1/org/legal-entities", json=legal_entity_payload, headers=headers, timeout=TIMEOUT)
        assert resp.status_code == 201
        body = resp.json()
        assert body["success"] is True
        return body["data"]["id"]

    user_id = None
    branch_id = None
    legal_entity_id = None

    try:
        # Create a user to link profile
        user_id = create_user()
        assert user_id is not None

        # Get or create a legal entity for branch linkage
        legal_entity_id = get_or_create_legal_entity()
        assert legal_entity_id is not None

        # Create a branch to link profile
        branch_id = create_branch(legal_entity_id)
        assert branch_id is not None

        # Create user profile (POST)
        profile_payload = {
            "userIdFk": user_id,
            "branchIdFk": branch_id,
            "fullNameAr": "الاسم الكامل للاختبار",
            "fullNameEn": "Test Full Name",
            "preferredLang": "en",
            "employeeIdFk": None  # Can be None or omitted if optional
        }
        resp_create = session.post(f"{BASE_URL}/api/v1/security/user-profiles", json=profile_payload, headers=headers, timeout=TIMEOUT)
        assert resp_create.status_code == 201
        body_create = resp_create.json()
        assert body_create["success"] is True
        profile = body_create["data"]
        assert profile is not None
        assert profile.get("userIdFk") == user_id
        assert profile.get("branchIdFk") == branch_id
        assert profile.get("fullNameEn") == "Test Full Name"
        profile_user_id = profile.get("userIdFk")

        # Update user profile (PUT)
        update_payload = {
            "fullNameAr": "الاسم الكامل المحدّث",
            "fullNameEn": "Updated Full Name",
            "preferredLang": "ar",
            "employeeIdFk": None
        }
        resp_update = session.put(f"{BASE_URL}/api/v1/security/user-profiles/{profile_user_id}", json=update_payload, headers=headers, timeout=TIMEOUT)
        assert resp_update.status_code == 200
        body_update = resp_update.json()
        assert body_update["success"] is True
        updated_profile = body_update["data"]
        assert updated_profile is not None
        assert updated_profile.get("fullNameEn") == "Updated Full Name"
        assert updated_profile.get("fullNameAr") == "الاسم الكامل المحدّث"
        assert updated_profile.get("preferredLang") == "ar"
        assert updated_profile.get("userIdFk") == user_id
        assert updated_profile.get("branchIdFk") == branch_id

    finally:
        # Cleanup: delete profile by deleting user (profile linked to user)
        if user_id:
            try:
                delete_user(user_id)
            except Exception:
                pass
        if branch_id:
            try:
                delete_branch(branch_id)
            except Exception:
                pass


test_user_profiles_create_and_update()
