-- ============================================================
-- V15 — Drop legacy MDM (Master Data) module schema
-- The MDM module's Java code (controllers, services, entities) was removed. V4 (schema+seed) and V5
-- (SEC-registry seed) are never edited per this repo's Flyway policy — this migration forward-drops
-- the tables/sequences V4 created instead. A future MDM rebuild owns its own fresh migration.
-- ============================================================

DROP TABLE IF EXISTS MDM_LOOKUP_VALUE CASCADE;
DROP TABLE IF EXISTS MDM_LOOKUP_TYPE CASCADE;

DROP SEQUENCE IF EXISTS SEQ_MDM_LOOKUP_VALUE;
DROP SEQUENCE IF EXISTS SEQ_MDM_LOOKUP_TYPE;
