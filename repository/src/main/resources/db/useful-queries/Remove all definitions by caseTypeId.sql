-- Delete case type DO $$
DECLARE
  caseTypeId constant varchar := '???';
BEGIN

  delete from event_case_field_complex_type where event_case_field_id in (
  select id from event_case_field where event_id in
    (
        select id from event where case_type_id in (select id from case_type where reference = caseTypeId)
    )
  );
  delete from event_case_field where event_id in (select id from event where case_type_id in (select id from case_type where reference = caseTypeId));
  delete from challenge_question where case_type_id in (select id from case_type where reference = caseTypeId);
  delete from display_group_case_field where display_group_id in (select id from display_group where case_type_id in (select id from case_type where reference = caseTypeId));
  delete from case_field_acl where case_field_id in (select id from case_field where case_type_id in (select id from case_type where reference = caseTypeId));
  delete from workbasket_case_field where case_type_id in (select id from case_type where reference = caseTypeId);
  delete from workbasket_input_case_field where case_type_id in (select id from case_type where reference = caseTypeId);
  delete from search_alias_field where case_type_id in (select id from case_type where reference = caseTypeId);
  delete from search_result_case_field where case_type_id in (select id from case_type where reference = caseTypeId);
  delete from search_input_case_field where case_type_id in (select id from case_type where reference = caseTypeId);
  delete from search_cases_result_fields where case_type_id in (select id from case_type where reference = caseTypeId);
  delete from complex_field_acl where case_field_id in (select id from case_field where case_type_id in (select id from case_type where reference = caseTypeId));
  delete from case_field where case_type_id in (select id from case_type where reference = caseTypeId);

  delete from display_group where case_type_id in (select id from case_type where reference = caseTypeId);
  delete from event_webhook where event_id in (select id from event where case_type_id in (select id from case_type where reference = caseTypeId));

  delete from webhook where id in (
    select webhook_id from event_webhook where event_id in (
        select id from event where case_type_id in (
            select id from case_type where reference = caseTypeId
        )
    )
  );

  delete from event_pre_state where event_id in (select id from event where case_type_id in (select id from case_type where reference = caseTypeId));
  delete from event_acl where event_id in (select id from event where case_type_id in (select id from case_type where reference = caseTypeId));
  delete from event_post_state where case_event_id in (select id from event where case_type_id in (select id from case_type where reference = caseTypeId));
  delete from event where case_type_id in (select id from case_type where reference = caseTypeId);

  delete from state_acl where state_id in (select id from state where case_type_id in (select id from case_type where reference = caseTypeId));
  delete from state where case_type_id in (select id from case_type where reference = caseTypeId);

  delete from case_type_acl where case_type_id in (select id from case_type where reference = caseTypeId);
  delete from role where case_type_id in (select id from case_type where reference = caseTypeId);
  delete from case_type where reference = caseTypeId;
END $$;
