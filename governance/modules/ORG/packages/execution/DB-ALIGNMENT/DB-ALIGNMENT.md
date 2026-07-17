<!-- Source: PHASE:DB-ALIGNMENT -->

# SECTION 2 — DB ALIGNMENT MANIFEST

Canonical interface per CONTRACT-1 (shared-artifact-contracts.md): FIELD-ID, DBF-ID, Plan Type, FK/XM-ID, Match Status only. Column name, DB type, table name, and SRS Source are NOT reproduced here — they are derived from dbs-org-001.md DBF Traceability Matrix by DBF-ID lookup (see DRV-ORG-018). DRV-ORG-018: 4A-005-006 remediation — Manifest rebuilt from the prior 5-column ("Field Name"/"Type"/"Read-Only") format, which duplicated DB Field Traceability Matrix content (column names, raw DB types) in violation of CONTRACT-1. Read-Only semantics (Business Code immutability, audit-field system-only status) now live exclusively in PHASE DATA+DOM per-entity narrative and SECTION A Error Catalog (RULE-ORG-011/014/016), not in this Manifest. FK/XM-ID column is "—" for all 94 rows: confirmed zero outbound XM-IDs (ROOT module, DBS XM Register), all FK columns are intra-module bindings (DRV-ORG-007). Match Status "✓" for all rows: every DBF-ID resolves to exactly one FIELD-ID with no type mismatch.

### ORG_LEGAL_ENTITY (ENTITY-ORG-001)
| FIELD-ID | DBF-ID | Plan Type | FK/XM-ID | Match Status |
|---|---|---|---|---|
| FIELD-0001 | DBF-0001 | Long | — | ✓ |
| FIELD-0002 | DBF-0002 | String(20) | — | ✓ |
| FIELD-0003 | DBF-0003 | String(200) | — | ✓ |
| FIELD-0004 | DBF-0004 | String(100) | — | ✓ |
| FIELD-0005 | DBF-0005 | String(50) | — | ✓ |
| FIELD-0006 | DBF-0006 | Boolean | — | ✓ |
| FIELD-0007 | DBF-0007 | String(2000) | — | ✓ |
| FIELD-0008 | DBF-0008 | String(255) | — | ✓ |
| FIELD-0009 | DBF-0009 | LocalDateTime | — | ✓ |
| FIELD-0010 | DBF-0010 | String(255) | — | ✓ |
| FIELD-0011 | DBF-0011 | LocalDateTime | — | ✓ |

### ORG_BRANCH (ENTITY-ORG-002)
| FIELD-ID | DBF-ID | Plan Type | FK/XM-ID | Match Status |
|---|---|---|---|---|
| FIELD-0012 | DBF-0012 | Long | — | ✓ |
| FIELD-0013 | DBF-0013 | String(20) | — | ✓ |
| FIELD-0014 | DBF-0014 | String(200) | — | ✓ |
| FIELD-0015 | DBF-0015 | String(100) | — | ✓ |
| FIELD-0016 | DBF-0016 | Long | — | ✓ |
| FIELD-0017 | DBF-0017 | String(50) | — | ✓ |
| FIELD-0018 | DBF-0018 | Boolean | — | ✓ |
| FIELD-0019 | DBF-0019 | String(2000) | — | ✓ |
| FIELD-0020 | DBF-0020 | String(255) | — | ✓ |
| FIELD-0021 | DBF-0021 | LocalDateTime | — | ✓ |
| FIELD-0022 | DBF-0022 | String(255) | — | ✓ |
| FIELD-0023 | DBF-0023 | LocalDateTime | — | ✓ |

### ORG_REGION_TYPE (ENTITY-ORG-008)
| FIELD-ID | DBF-ID | Plan Type | FK/XM-ID | Match Status |
|---|---|---|---|---|
| FIELD-0024 | DBF-0024 | Long | — | ✓ |
| FIELD-0025 | DBF-0025 | String(30) | — | ✓ |
| FIELD-0026 | DBF-0026 | String(200) | — | ✓ |
| FIELD-0027 | DBF-0027 | String(100) | — | ✓ |
| FIELD-0028 | DBF-0028 | Boolean | — | ✓ |
| FIELD-0029 | DBF-0029 | String(255) | — | ✓ |
| FIELD-0030 | DBF-0030 | LocalDateTime | — | ✓ |
| FIELD-0031 | DBF-0031 | String(255) | — | ✓ |
| FIELD-0032 | DBF-0032 | LocalDateTime | — | ✓ |

### ORG_REGION (ENTITY-ORG-003)
| FIELD-ID | DBF-ID | Plan Type | FK/XM-ID | Match Status |
|---|---|---|---|---|
| FIELD-0033 | DBF-0033 | Long | — | ✓ |
| FIELD-0034 | DBF-0034 | String(20) | — | ✓ |
| FIELD-0035 | DBF-0035 | String(200) | — | ✓ |
| FIELD-0036 | DBF-0036 | String(100) | — | ✓ |
| FIELD-0037 | DBF-0037 | Long | — | ✓ |
| FIELD-0038 | DBF-0038 | Long | — | ✓ |
| FIELD-0039 | DBF-0039 | Boolean | — | ✓ |
| FIELD-0040 | DBF-0040 | String(2000) | — | ✓ |
| FIELD-0041 | DBF-0041 | String(255) | — | ✓ |
| FIELD-0042 | DBF-0042 | LocalDateTime | — | ✓ |
| FIELD-0043 | DBF-0043 | String(255) | — | ✓ |
| FIELD-0044 | DBF-0044 | LocalDateTime | — | ✓ |

### ORG_DEPARTMENT (ENTITY-ORG-004)
| FIELD-ID | DBF-ID | Plan Type | FK/XM-ID | Match Status |
|---|---|---|---|---|
| FIELD-0045 | DBF-0045 | Long | — | ✓ |
| FIELD-0046 | DBF-0046 | String(20) | — | ✓ |
| FIELD-0047 | DBF-0047 | String(200) | — | ✓ |
| FIELD-0048 | DBF-0048 | String(100) | — | ✓ |
| FIELD-0049 | DBF-0049 | Long | — | ✓ |
| FIELD-0050 | DBF-0050 | Long | — | ✓ |
| FIELD-0051 | DBF-0051 | String(50) | — | ✓ |
| FIELD-0052 | DBF-0052 | Boolean | — | ✓ |
| FIELD-0053 | DBF-0053 | String(2000) | — | ✓ |
| FIELD-0054 | DBF-0054 | String(255) | — | ✓ |
| FIELD-0055 | DBF-0055 | LocalDateTime | — | ✓ |
| FIELD-0056 | DBF-0056 | String(255) | — | ✓ |
| FIELD-0057 | DBF-0057 | LocalDateTime | — | ✓ |

### ORG_COST_CENTER (ENTITY-ORG-005)
| FIELD-ID | DBF-ID | Plan Type | FK/XM-ID | Match Status |
|---|---|---|---|---|
| FIELD-0058 | DBF-0058 | Long | — | ✓ |
| FIELD-0059 | DBF-0059 | String(20) | — | ✓ |
| FIELD-0060 | DBF-0060 | String(200) | — | ✓ |
| FIELD-0061 | DBF-0061 | String(100) | — | ✓ |
| FIELD-0062 | DBF-0062 | Long | — | ✓ |
| FIELD-0063 | DBF-0063 | Long | — | ✓ |
| FIELD-0064 | DBF-0064 | String(50) | — | ✓ |
| FIELD-0065 | DBF-0065 | String(50) | — | ✓ |
| FIELD-0066 | DBF-0066 | Boolean | — | ✓ |
| FIELD-0067 | DBF-0067 | String(2000) | — | ✓ |
| FIELD-0068 | DBF-0068 | String(255) | — | ✓ |
| FIELD-0069 | DBF-0069 | LocalDateTime | — | ✓ |
| FIELD-0070 | DBF-0070 | String(255) | — | ✓ |
| FIELD-0071 | DBF-0071 | LocalDateTime | — | ✓ |

### ORG_PROFIT_CENTER (ENTITY-ORG-006)
| FIELD-ID | DBF-ID | Plan Type | FK/XM-ID | Match Status |
|---|---|---|---|---|
| FIELD-0072 | DBF-0072 | Long | — | ✓ |
| FIELD-0073 | DBF-0073 | String(20) | — | ✓ |
| FIELD-0074 | DBF-0074 | String(200) | — | ✓ |
| FIELD-0075 | DBF-0075 | String(100) | — | ✓ |
| FIELD-0076 | DBF-0076 | Long | — | ✓ |
| FIELD-0077 | DBF-0077 | Boolean | — | ✓ |
| FIELD-0078 | DBF-0078 | String(2000) | — | ✓ |
| FIELD-0079 | DBF-0079 | String(255) | — | ✓ |
| FIELD-0080 | DBF-0080 | LocalDateTime | — | ✓ |
| FIELD-0081 | DBF-0081 | String(255) | — | ✓ |
| FIELD-0082 | DBF-0082 | LocalDateTime | — | ✓ |

### ORG_LOCATION_SITE (ENTITY-ORG-007)
| FIELD-ID | DBF-ID | Plan Type | FK/XM-ID | Match Status |
|---|---|---|---|---|
| FIELD-0083 | DBF-0083 | Long | — | ✓ |
| FIELD-0084 | DBF-0084 | String(20) | — | ✓ |
| FIELD-0085 | DBF-0085 | String(200) | — | ✓ |
| FIELD-0086 | DBF-0086 | String(100) | — | ✓ |
| FIELD-0087 | DBF-0087 | Long | — | ✓ |
| FIELD-0088 | DBF-0088 | String(50) | — | ✓ |
| FIELD-0089 | DBF-0089 | Boolean | — | ✓ |
| FIELD-0090 | DBF-0090 | String(2000) | — | ✓ |
| FIELD-0091 | DBF-0091 | String(255) | — | ✓ |
| FIELD-0092 | DBF-0092 | LocalDateTime | — | ✓ |
| FIELD-0093 | DBF-0093 | String(255) | — | ✓ |
| FIELD-0094 | DBF-0094 | LocalDateTime | — | ✓ |
