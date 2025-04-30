BEGIN;

-- =========================================
-- ⚠️ Tables containing static (base types)
-- - Care must be taken to not delete any base types
-- - public.field_type
-- - public.complex_field
-- - public.case_field
-- =========================================

-- These queries are designed to be run in a controlled environment where the data integrity and relationships are well understood.
-- Always ensure to backup your data before running such delete operations.

CREATE TEMP TABLE case_type_ids_to_remove AS
SELECT ct.id
FROM case_type ct
INNER JOIN (
    SELECT reference, MAX(version) AS max_version
    FROM case_type
    GROUP BY reference
) grouped_ct
ON ct.reference = grouped_ct.reference
WHERE ct.version != grouped_ct.max_version
  AND ct.created_at <= NOW() - INTERVAL '3 months';

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

--Not Sure if this is required yet
DELETE FROM event_case_field_complex_type
USING event_case_field, case_field
WHERE event_case_field_complex_type.event_case_field_id  = event_case_field.id
  AND event_case_field.case_field_id  = case_field.id
  AND event_case_field.event_id IN (SELECT id FROM removable_events)
  AND case_field.case_type_id IN (SELECT id FROM case_type_ids_to_remove)
  AND field_type_id NOT IN (SELECT id FROM valid_field_type_ids);

--Not Sure if this is required yet
DELETE FROM challenge_question
USING case_type
WHERE challenge_question.case_type_id IN (SELECT id FROM case_type_ids_to_remove)
AND case_type.id = challenge_question.case_type_id
  AND case_type.jurisdiction_id IS NOT NULL;

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
DELETE FROM display_group WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove);
DELETE FROM event_post_state WHERE case_event_id IN (SELECT id FROM removable_events);
DELETE FROM event_acl WHERE event_id IN (SELECT id FROM removable_events);
DELETE FROM event_pre_state WHERE event_id IN (SELECT id FROM removable_events);
DELETE FROM event_webhook WHERE event_id IN (SELECT id FROM removable_events);
DELETE FROM "event" WHERE id IN (SELECT id FROM removable_events);
DELETE FROM state_acl WHERE state_id IN (SELECT id FROM removable_states);
DELETE FROM state WHERE id IN (SELECT id FROM removable_states);
DELETE FROM role WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove);

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

-- Optional: remove orphan field_types
DELETE FROM field_type
WHERE id IN (
    SELECT DISTINCT field_type_id
    FROM case_field
    WHERE case_type_id IN (SELECT id FROM case_type_ids_to_remove)
)
AND id NOT IN (SELECT id FROM valid_field_type_ids)
AND jurisdiction_id IS NOT NULL;

-- Final cleanup: remove the case_type entries
DELETE FROM case_type
WHERE id IN (SELECT id FROM case_type_ids_to_remove)
  AND jurisdiction_id IS NOT NULL;

-- Note: The jurisdiction_id check is to ensure we only delete case types that are not system-defined.
-- This is important to prevent accidental deletion of system case types.

COMMIT;



