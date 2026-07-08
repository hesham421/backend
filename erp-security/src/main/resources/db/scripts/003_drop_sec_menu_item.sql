-- ============================================================================
-- Security Module — Drop unused legacy SEC_MENU_ITEM table
-- GAP: registry-security.md §8.3 — table exists but is never read/written by
-- MenuService (menu is built dynamically from SEC_PAGES + permissions).
-- Run manually by DBA (psql / pgAdmin).
-- ============================================================================

DROP TABLE IF EXISTS SEC_MENU_ITEM;
