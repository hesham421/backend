import requests

BASE_URL = "http://localhost:7272"
TIMEOUT = 30


def authenticate():
    url = f"{BASE_URL}/api/auth/login"
    payload = {"username": "admin", "password": "admin123"}
    resp = requests.post(url, json=payload, timeout=TIMEOUT)
    resp.raise_for_status()
    data = resp.json()
    assert data["success"] is True
    return data["data"]["accessToken"]


def test_get_region_includes_regiontype():
    token = authenticate()
    headers = {"Authorization": f"Bearer {token}"}

    # Helper functions to create and delete a legal entity and region with region type.
    def create_legal_entity():
        url = f"{BASE_URL}/api/v1/org/legal-entities"
        payload = {
            "nameAr": "Test Legal Entity AR",
            "nameEn": "Test Legal Entity EN",
            "entityTypeId": 1,
            "notes": "Created for region test"
        }
        resp = requests.post(url, json=payload, headers=headers, timeout=TIMEOUT)
        resp.raise_for_status()
        data = resp.json()
        assert data["success"] is True
        return data["data"]["id"]

    def delete_legal_entity(entity_id):
        url = f"{BASE_URL}/api/v1/org/legal-entities/{entity_id}/deactivate"
        requests.put(url, headers=headers, timeout=TIMEOUT)
        requests.delete(f"{BASE_URL}/api/v1/org/legal-entities/{entity_id}", headers=headers, timeout=TIMEOUT)

    def get_region_types():
        url = f"{BASE_URL}/api/lookups/REGION_TYPE"
        resp = requests.get(url, headers=headers, timeout=TIMEOUT)
        resp.raise_for_status()
        data = resp.json()
        assert data["success"] is True
        return data["data"]

    def create_region(legal_entity_fk, region_type_id):
        url = f"{BASE_URL}/api/v1/org/regions"
        payload = {
            "legalEntityFk": legal_entity_fk,
            "regionTypeIdFk": region_type_id,
            "nameAr": "Test Region AR",
            "nameEn": "Test Region EN",
            "notes": "Created for get region test"
        }
        resp = requests.post(url, json=payload, headers=headers, timeout=TIMEOUT)
        resp.raise_for_status()
        data = resp.json()
        assert data["success"] is True
        return data["data"]["id"]

    def delete_region(region_id):
        url = f"{BASE_URL}/api/v1/org/regions/{region_id}/deactivate"
        requests.put(url, headers=headers, timeout=TIMEOUT)
        url_del = f"{BASE_URL}/api/v1/org/regions/{region_id}"
        requests.delete(url_del, headers=headers, timeout=TIMEOUT)

    legal_entity_id = None
    region_id = None

    try:
        # Create a new Legal Entity first
        legal_entity_id = create_legal_entity()

        # Fetch region types to get a valid regionTypeIdFk
        region_types = get_region_types()
        # Pick first active region type; fallback to None if none found
        if not region_types or len(region_types) == 0:
            raise Exception("No REGION_TYPE lookup values available")
        region_type_id = None
        for rt in region_types:
            if rt.get("isActive", True):
                # regionType code is string, but the API expects regionTypeIdFk as number (id)
                # so prefer 'id' field
                region_type_id = rt.get("id", None)
                if region_type_id is not None:
                    break
        if region_type_id is None:
            raise Exception("No active REGION_TYPE found with valid id")

        # Create the Region with the region type
        region_id = create_region(legal_entity_id, region_type_id)

        # Get region by ID and verify it includes region type info
        url = f"{BASE_URL}/api/v1/org/regions/{region_id}"
        resp = requests.get(url, headers=headers, timeout=TIMEOUT)
        resp.raise_for_status()
        data = resp.json()
        assert data["success"] is True
        region_data = data["data"]

        # Basic checks
        assert region_data["id"] == region_id
        assert region_data["legalEntityFk"] == legal_entity_id
        assert "regionTypeIdFk" in region_data
        assert region_data["regionTypeIdFk"] == region_type_id
        assert region_data["nameAr"] == "Test Region AR"
        assert region_data["nameEn"] == "Test Region EN"

    finally:
        if region_id is not None:
            try:
                delete_region(region_id)
            except Exception:
                pass
        if legal_entity_id is not None:
            try:
                delete_legal_entity(legal_entity_id)
            except Exception:
                pass


test_get_region_includes_regiontype()