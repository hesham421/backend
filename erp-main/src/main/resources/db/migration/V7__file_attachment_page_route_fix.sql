-- ============================================================================
-- File Service — FILE_ATTACHMENT SEC_PAGES route fix (SEC.md screen guard)
--
-- V3__file_service_schema_and_seed.sql seeded route = '/shared/file-attachment'
-- for the FILE_ATTACHMENT page, but SCR-FILE-001 is explicitly a Shared/Embedded
-- Component with "no independent navigation menu" (SEC.md: "لا قائمة تنقّل
-- مستقلة") — no Angular route was ever created for it (F4.md DRV-FILE-011:
-- embedded via <app-file-attachment>, no route of its own).
--
-- MenuService.buildUserMenuFromPermissions() builds the sidebar purely from
-- SEC_PAGES rows the user holds PERM_*_VIEW for, with no exclusion for
-- "shared component" pages — so any user holding PERM_FILE_ATTACHMENT_VIEW
-- (e.g. SUPER_ADMIN, granted since V3) gets a real, clickable root-level
-- sidebar entry pointing at a route Angular has never registered. Clicking it
-- does nothing (NavItemComponent's template only renders an <li> at all when
-- item.url is truthy — see nav-item.component.html line 2).
--
-- sec_pages.route is NOT NULL (schema-enforced), so it can't simply be
-- cleared to NULL — empty string satisfies the constraint AND is falsy in
-- the frontend's `@if (item().url && !item().external)` check, so the item
-- still renders nothing, matching the documented "no independent navigation"
-- intent exactly, same as other embedded-component pages.
-- ============================================================================

BEGIN;

UPDATE sec_pages
SET route = '', updated_by = 'SYSTEM', updated_at = now()
WHERE page_code = 'FILE_ATTACHMENT';

COMMIT;
