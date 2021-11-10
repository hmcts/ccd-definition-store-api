-- Delete case type DO $$
DECLARE
  caseTypeId constant varchar := '???';
BEGIN

  DROP VIEW IF EXISTS view__case_type_to_remove;

  CREATE or REPLACE VIEW view__case_type_to_remove AS
  (
    SELECT ct.id, ct.created_at, ct.reference, ct.version FROM case_type ct INNER JOIN
        (SELECT reference, MAX("version") AS MaxVersion
        FROM case_type
        GROUP BY reference) grouped_ct
    ON ct.reference = grouped_ct.reference
    AND (ct.version != grouped_ct.MaxVersion AND ct.created_at <= 'now'::timestamp - '3 MONTH'::interval)
  );

  delete from event_case_field_complex_type where event_case_field_id in (select id from event_case_field where event_id in
        (select id from event where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId)));

  delete from event_case_field where event_id in (select id from event where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId));

  delete from challenge_question where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId);

  delete from display_group_case_field where display_group_id in (select id from display_group where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId));

  delete from case_field_acl where case_field_id in (select id from case_field where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId));

  delete from workbasket_case_field where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId);

  delete from workbasket_input_case_field where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId);

  delete from search_alias_field where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId);

  delete from search_result_case_field where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId);

  delete from search_input_case_field where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId);

  delete from search_cases_result_fields where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId);

  delete from complex_field_acl where case_field_id in (select id from case_field where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId));

  delete from case_field where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId);

  delete from display_group where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId);

  delete from event_webhook where event_id in (select id from event where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId));

  delete from event_pre_state where event_id in (select id from event where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId));

  delete from event_acl where event_id in (select id from event where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId));

  delete from event_post_state where case_event_id in (select id from event where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId));

  delete from event where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId);

  delete from state_acl where state_id in (select id from state where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId));

  delete from state where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId);

  delete from case_type_acl where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId);

  delete from role where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId);

  delete from role_to_access_profiles where case_type_id in (select id from view__case_type_to_remove where reference = caseTypeId);

  delete from case_type where id IN (select id from view__case_type_to_remove where reference = caseTypeId);

  DROP VIEW IF EXISTS view__case_type_to_remove;

END $$;
