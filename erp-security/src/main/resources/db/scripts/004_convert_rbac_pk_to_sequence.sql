-- ============================================================================
-- erp-security/src/main/resources/db/scripts/004_convert_rbac_pk_to_sequence.sql
-- ============================================================================
-- Security Module — convert ROLES/USERS/PERMISSIONS primary-key generation
-- from IDENTITY to SEQUENCE (GenerationType.SEQUENCE + @SequenceGenerator),
-- per create-entity/enforce-backend-contract governance rule A.1.3/A.1.4
-- ("GenerationType.IDENTITY or AUTO" is an automatic rejection trigger).
--
-- Run manually by DBA (psql / pgAdmin), AFTER 001_security_schema_migration_
-- and_seed.sql and 002_datascope_selfservice_auth_schema.sql have been
-- applied (this script assumes ROLES_PK/USERS_PK/PERMISSIONS_PK already
-- exist under those names — true in the current live DB, confirmed via \d).
-- Safe to re-run (idempotent — sequence creation is guarded by existence
-- check; DROP IDENTITY IF EXISTS / SET DEFAULT / OWNED BY are all no-ops or
-- safely repeatable on a second run).
--
-- Zero-downtime: DROP IDENTITY / SET DEFAULT are metadata-only (no table
-- rewrite). Each table takes a brief ACCESS EXCLUSIVE lock (milliseconds)
-- held across the MAX(pk) read + cutover, closing the race window against
-- concurrent inserts (relevant for USERS — self-service signup writes).
-- ============================================================================

BEGIN;

DO $$
DECLARE
    rec RECORD;
    actual_table regclass;
    v_col text;
    v_seq text;
    v_max_id BIGINT;
    v_start_with BIGINT;
    v_buffer CONSTANT BIGINT := 1000;  -- safety headroom above current MAX(pk)
BEGIN
    FOR rec IN
        SELECT * FROM (VALUES
            ('ROLES',       'ROLES_PK',       'ROLES_SEQ'),
            ('USERS',       'USERS_PK',       'USERS_SEQ'),
            ('PERMISSIONS', 'PERMISSIONS_PK', 'PERMISSIONS_SEQ')
        ) AS t(table_name, pk_column, seq_name)
    LOOP
        -- Casting to regclass folds case correctly regardless of how the
        -- literal above is styled; %I on the raw literal would NOT (see
        -- 001_security_schema_migration_and_seed.sql's own comment on this
        -- exact gotcha) — verified live during drafting of this script.
        actual_table := rec.table_name::regclass;
        v_col := lower(rec.pk_column);
        v_seq := lower(rec.seq_name);

        EXECUTE format('LOCK TABLE %s IN ACCESS EXCLUSIVE MODE', actual_table);

        EXECUTE format('SELECT max(%I) FROM %s', v_col, actual_table) INTO v_max_id;
        v_start_with := COALESCE(v_max_id, 0) + v_buffer;

        IF NOT EXISTS (
            SELECT 1 FROM pg_sequences
            WHERE schemaname = 'public' AND sequencename = v_seq
        ) THEN
            EXECUTE format('CREATE SEQUENCE %I START WITH %s INCREMENT BY 1', v_seq, v_start_with);
        END IF;

        -- Drops the old internal identity sequence automatically (verified:
        -- zero leftover pg_class rows after DROP IDENTITY on a test table).
        EXECUTE format('ALTER TABLE %s ALTER COLUMN %I DROP IDENTITY IF EXISTS', actual_table, v_col);
        EXECUTE format('ALTER TABLE %s ALTER COLUMN %I SET DEFAULT nextval(%L)', actual_table, v_col, v_seq);

        -- Ties the new sequence's lifecycle to the column (auto-dropped if
        -- the column/table is ever dropped) without reintroducing IDENTITY.
        EXECUTE format('ALTER SEQUENCE %I OWNED BY %s.%I', v_seq, actual_table, v_col);

        RAISE NOTICE 'Converted %.% -> sequence % starting at % (observed MAX=%)',
            actual_table, v_col, v_seq, v_start_with, v_max_id;
    END LOOP;
END $$;

COMMIT;
