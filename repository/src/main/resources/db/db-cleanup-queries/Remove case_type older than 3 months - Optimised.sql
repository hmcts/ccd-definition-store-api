/*

--Execute the following DROP statements before starting the cleanup

--May require these missing index's
--CREATE INDEX IF NOT EXISTS idx_role_case_type_id ON "role" (case_type_id);
--CREATE INDEX IF NOT EXISTS idx_case_type_id ON case_type (id);
--CREATE INDEX IF NOT EXISTS idx_role_id ON "role" (id);
--CREATE INDEX IF NOT EXISTS idx_user_role_id ON "role" (user_role_id);

ALTER TABLE public."role" DROP CONSTRAINT case_type_id_check;
ALTER TABLE public."role"
DROP CONSTRAINT unique_role_case_type_id_role_reference;
ALTER TABLE public."role" DROP CONSTRAINT "fk_role_case_type_id_case_type_id";
ALTER TABLE public."case_field_acl" DROP CONSTRAINT "fk_case_field_acl_role_id_role_id";
ALTER TABLE public."case_type_acl" DROP CONSTRAINT "fk_case_type_acl_role_id_role_id";
ALTER TABLE public."complex_field_acl" DROP CONSTRAINT "fk_complex_field_acl_role_id_role_id";
ALTER TABLE public."display_group" DROP CONSTRAINT "fk_display_group_role_id";
ALTER TABLE public."search_input_case_field" DROP CONSTRAINT "fk_display_group_role_id";
ALTER TABLE public."search_result_case_field" DROP CONSTRAINT "fk_display_group_role_id";
ALTER TABLE public."workbasket_case_field" DROP CONSTRAINT "fk_display_group_role_id";
ALTER TABLE public."workbasket_input_case_field" DROP CONSTRAINT "fk_display_group_role_id";
ALTER TABLE public."search_cases_result_fields" DROP CONSTRAINT "fk_search_cases_result_fields_role_id_role_id";
ALTER TABLE public."state_acl" DROP CONSTRAINT "fk_state_acl_role_id_role_id";
ALTER TABLE public."event_acl" DROP CONSTRAINT "fk_event_acl_role_id_role_id";

--DO THE DELETIONS

--Execute the following ADD statements after the cleanup
ALTER TABLE public."role" ADD CONSTRAINT "fk_role_case_type_id_case_type_id" FOREIGN KEY (case_type_id) REFERENCES case_type(id);
ALTER TABLE public."role"
ADD CONSTRAINT unique_role_case_type_id_role_reference
UNIQUE (case_type_id, reference);
ALTER TABLE public."role"
ADD CONSTRAINT case_type_id_check CHECK (
  (
    CASE
      WHEN dtype = 'CASEROLE' THEN
        CASE
          WHEN case_type_id IS NOT NULL THEN 1
          ELSE 0
        END
      ELSE 1
    END = 1
  )
);
ALTER TABLE public."case_field_acl" ADD CONSTRAINT "fk_case_field_acl_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."case_type_acl" ADD CONSTRAINT "fk_case_type_acl_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."complex_field_acl" ADD CONSTRAINT "fk_complex_field_acl_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."workbasket_input_case_field" ADD CONSTRAINT "fk_display_group_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."workbasket_case_field" ADD CONSTRAINT "fk_display_group_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."search_result_case_field" ADD CONSTRAINT "fk_display_group_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."search_input_case_field" ADD CONSTRAINT "fk_display_group_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."display_group" ADD CONSTRAINT "fk_display_group_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."event_acl" ADD CONSTRAINT "fk_event_acl_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."search_cases_result_fields" ADD CONSTRAINT "fk_search_cases_result_fields_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id);
ALTER TABLE public."state_acl" ADD CONSTRAINT "fk_state_acl_role_id_role_id" FOREIGN KEY (role_id) REFERENCES role(id);

*/

-- Clean up temp tables after transaction completes
DO $$
BEGIN
  BEGIN
    DROP TABLE IF EXISTS case_type_ids_to_remove;
  EXCEPTION WHEN OTHERS THEN
    -- ignore error
  END;

  BEGIN
    DROP TABLE IF EXISTS valid_field_type_ids;
  EXCEPTION WHEN OTHERS THEN
    -- ignore error
  END;

  BEGIN
    DROP TABLE IF EXISTS removable_case_fields;
  EXCEPTION WHEN OTHERS THEN
    -- ignore error
  END;

  BEGIN
    DROP TABLE IF EXISTS removable_events;
  EXCEPTION WHEN OTHERS THEN
    -- ignore error
  END;

  BEGIN
    DROP TABLE IF EXISTS removable_states;
  EXCEPTION WHEN OTHERS THEN
    -- ignore error
  END;
END $$;

--BEGIN;

-- =========================================
-- ⚠️ Tables containing static (base types)
-- - Care must be taken to not delete any base types
-- - public.field_type
-- - public.complex_field
-- - public.case_field
-- =========================================

-- ==========================================
-- - The following tables also need cleaning up
-- - to be done later
-- -      search_alias_field
-- -      field_type_list_item
-- -      complex_field
-- -      event
-- -      role_to_access_profiles
-- -      search_criteria
-- -      search_party
-- -      category
-- ==========================================

-- These queries are designed to be run in a controlled environment where the data integrity and relationships are well understood.
-- Always ensure to backup your data before running such delete operations.

CREATE TEMP TABLE case_type_ids_to_remove AS
SELECT id
FROM case_type ct
INNER JOIN (
    SELECT reference, MAX(version) AS max_version
    FROM case_type
    GROUP BY reference
) grouped_ct
ON ct.reference = grouped_ct.reference
WHERE ct.version != grouped_ct.max_version
  AND ct.created_at <= NOW() - INTERVAL '3 months' ORDER BY id ASC LIMIT 6000;

-- Create a temporary table (valid_field_type_ids) to hold the IDs of field types that are static (base types) and should not be deleted.
CREATE TEMP TABLE valid_field_type_ids AS
SELECT id
FROM field_type
WHERE jurisdiction_id IS NULL;

-- Create a temporary table (removable_case_fields) to hold the IDs of case fields that are not static (base types)
CREATE TEMP TABLE removable_case_fields AS
SELECT id, case_type_id
FROM case_field
WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
  AND field_type_id NOT IN (SELECT id FROM valid_field_type_ids);

-- Create a temporary table (removable_events) to hold the IDs of events that are associated with the case types to be removed
CREATE TEMP TABLE removable_events AS
SELECT id
FROM event
	WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove);

-- Create a temporary table (removable_states) to hold the IDs of states that are associated with the case types to be removed
CREATE TEMP TABLE removable_states AS
SELECT id
FROM state
	WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove);

-- Case field related deletes
DELETE FROM case_field_acl WHERE case_field_id IN (SELECT id FROM removable_case_fields);
DELETE FROM display_group_case_field WHERE case_field_id IN (SELECT id FROM removable_case_fields);
DELETE FROM event_case_field_complex_type
WHERE event_case_field_id IN (
    SELECT ecf.id
    FROM event_case_field ecf
    JOIN removable_case_fields rcf ON ecf.case_field_id = rcf.id
    WHERE ecf.event_id IN (SELECT id FROM removable_events)
);

DELETE FROM event_case_field WHERE case_field_id IN (SELECT id FROM removable_case_fields);
DELETE FROM complex_field_acl WHERE case_field_id IN (SELECT id FROM removable_case_fields);
DELETE FROM search_result_case_field WHERE case_field_id IN (SELECT id FROM removable_case_fields);
DELETE FROM workbasket_case_field WHERE case_field_id IN (SELECT id FROM removable_case_fields);
DELETE FROM search_input_case_field WHERE case_field_id IN (SELECT id FROM removable_case_fields);
DELETE FROM workbasket_input_case_field WHERE case_field_id IN (SELECT id FROM removable_case_fields);
DELETE FROM search_cases_result_fields scrf
USING removable_case_fields rcf
WHERE scrf.case_field_id = rcf.id
  AND scrf.case_type_id IN (SELECT id FROM case_type_ids_to_remove);

-- Delete the case fields themselves
DELETE FROM case_field WHERE id IN (SELECT id FROM removable_case_fields);

-- Other related deletions
DELETE FROM case_type_acl WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove);

DELETE FROM display_group_case_field WHERE case_field_id IN (SELECT id FROM removable_case_fields);

DELETE FROM event_post_state WHERE case_event_id IN (SELECT id FROM removable_events);
DELETE FROM event_acl WHERE event_id IN (SELECT id FROM removable_events);
DELETE FROM event_pre_state WHERE event_id IN (SELECT id FROM removable_events);
DELETE FROM event_webhook WHERE event_id IN (SELECT id FROM removable_events);
DELETE FROM state_acl WHERE state_id IN (SELECT id FROM removable_states);
DELETE FROM state WHERE id IN (SELECT id FROM removable_states);

-- Redundant search/workbasket deletes
DELETE FROM search_cases_result_fields
WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
  AND case_field_id IN (SELECT id FROM removable_case_fields);

DELETE FROM search_input_case_field
WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
  AND case_field_id IN (SELECT id FROM removable_case_fields);

DELETE FROM search_result_case_field
WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
  AND case_field_id IN (SELECT id FROM removable_case_fields);

DELETE FROM workbasket_case_field
WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
  AND case_field_id IN (SELECT id FROM removable_case_fields);

-- remove orphan field_types
DELETE FROM field_type
WHERE id IN (
    SELECT DISTINCT field_type_id
    FROM case_field
    WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
)
AND id NOT IN (SELECT id FROM valid_field_type_ids)
AND jurisdiction_id IS NOT NULL;

DELETE FROM case_field_acl WHERE case_field_id IN
    (SELECT id FROM case_field WHERE case_type_id IN
        (SELECT id FROM case_type_ids_to_remove)
    );

DELETE FROM display_group_case_field WHERE display_group_id IN
    (SELECT id FROM display_group WHERE case_type_id IN
        (SELECT id FROM case_type_ids_to_remove)
    );

DELETE FROM event_case_field_complex_type WHERE event_case_field_id IN
    (SELECT id FROM event_case_field WHERE event_id IN
        (SELECT id FROM event WHERE case_type_id IN
            (SELECT id FROM case_type_ids_to_remove)
        )
    );

DELETE FROM complex_field_acl WHERE case_field_id IN
    (SELECT id FROM case_field WHERE case_type_id IN
        (SELECT id FROM case_type_ids_to_remove)
    );

DELETE FROM search_cases_result_fields WHERE case_type_id IN
    (SELECT id FROM case_type_ids_to_remove);

DELETE FROM search_input_case_field WHERE case_type_id IN
    (SELECT id FROM case_type_ids_to_remove);

DELETE FROM search_result_case_field WHERE case_type_id IN
    (SELECT id FROM case_type_ids_to_remove);

DELETE FROM workbasket_case_field  WHERE case_type_id IN
    (SELECT id FROM case_type_ids_to_remove);

DELETE FROM workbasket_input_case_field WHERE case_type_id IN
    (SELECT id FROM case_type_ids_to_remove);

DELETE FROM event_case_field WHERE event_id IN
    (SELECT id FROM event WHERE case_type_id IN
        (SELECT id FROM case_type_ids_to_remove)
    );

DELETE FROM case_field cf WHERE cf.case_type_id IN (SELECT id FROM case_type_ids_to_remove);

DELETE FROM role_to_access_profiles WHERE case_type_id IN
    (SELECT id FROM case_type_ids_to_remove);

DELETE FROM display_group_case_field WHERE display_group_id IN
    (SELECT id FROM display_group WHERE case_type_id IN
        (SELECT id FROM case_type_ids_to_remove)
    );

DELETE FROM display_group WHERE case_type_id IN
    (SELECT id FROM case_type_ids_to_remove);

DELETE FROM event WHERE case_type_id IN
    (SELECT id FROM case_type_ids_to_remove);

DELETE FROM search_criteria WHERE case_type_id IN
    (SELECT id FROM case_type_ids_to_remove);

DELETE FROM search_party WHERE case_type_id IN
    (SELECT id FROM case_type_ids_to_remove);

DELETE FROM search_alias_field WHERE case_type_id IN
    (SELECT id FROM case_type_ids_to_remove);

DELETE FROM category WHERE case_type_id IN
    (SELECT id FROM case_type_ids_to_remove);

DELETE FROM challenge_question WHERE case_type_id IN
    (SELECT id FROM case_type_ids_to_remove);

DELETE FROM "role" WHERE case_type_id IN
			(SELECT id FROM case_type_ids_to_remove);

DELETE FROM access_type_role WHERE case_type_id IN
    (SELECT id FROM case_type_ids_to_remove);

DELETE FROM access_type WHERE case_type_id IN
    (SELECT id FROM case_type_ids_to_remove);

-- Final cleanup: remove the case_type entries
DELETE FROM case_type
	WHERE id IN (SELECT id FROM case_type_ids_to_remove)
	  AND jurisdiction_id IS NOT NULL;

-- Note: The jurisdiction_id check is to ensure we only delete case types that are not system-defined.
-- This is important to prevent accidental deletion of system case types.

--COMMIT;

-- Clean up temp tables after transaction completes
DO $$
BEGIN
  BEGIN
    DROP TABLE IF EXISTS case_type_ids_to_remove;
  EXCEPTION WHEN OTHERS THEN
    -- ignore error
  END;

  BEGIN
    DROP TABLE IF EXISTS valid_field_type_ids;
  EXCEPTION WHEN OTHERS THEN
    -- ignore error
  END;

  BEGIN
    DROP TABLE IF EXISTS removable_case_fields;
  EXCEPTION WHEN OTHERS THEN
    -- ignore error
  END;

  BEGIN
    DROP TABLE IF EXISTS removable_events;
  EXCEPTION WHEN OTHERS THEN
    -- ignore error
  END;

  BEGIN
    DROP TABLE IF EXISTS removable_states;
  EXCEPTION WHEN OTHERS THEN
    -- ignore error
  END;
END $$;
