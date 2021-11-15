-- Delete jurisdiction
DO $$
DECLARE
  jurisdictionId constant varchar := '???';
BEGIN

  DELETE FROM event_case_field_complex_type WHERE event_case_field_id IN
    (SELECT id FROM event_case_field WHERE case_field_id IN
        (SELECT id FROM case_field WHERE case_type_id IN
            (SELECT id FROM case_type WHERE jurisdiction_id =
                (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
            )
        )
    );

  DELETE FROM event_case_field WHERE case_field_id IN
    (SELECT id FROM case_field WHERE case_type_id IN
        (SELECT id FROM case_type WHERE jurisdiction_id =
            (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
        )
    );

  DELETE FROM display_group_case_field WHERE case_field_id IN
    (SELECT id FROM case_field WHERE case_type_id IN
        (SELECT id FROM case_type WHERE jurisdiction_id =
            (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
         )
    );

  DELETE FROM case_field_acl WHERE case_field_id IN
    (SELECT id FROM case_field WHERE case_type_id IN
        (SELECT id FROM case_type WHERE jurisdiction_id =
            (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
        )
    );

  DELETE FROM workbasket_case_field WHERE case_field_id IN
    (SELECT id FROM case_field WHERE case_type_id IN
        (SELECT id FROM case_type WHERE jurisdiction_id =
            (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
        )
    );

  DELETE FROM workbasket_input_case_field WHERE case_field_id IN
    (SELECT id FROM case_field WHERE case_type_id IN
        (SELECT id FROM case_type WHERE jurisdiction_id =
            (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
        )
    );

  DELETE FROM search_result_case_field WHERE case_field_id IN
    (SELECT id FROM case_field WHERE case_type_id IN
        (SELECT id FROM case_type WHERE jurisdiction_id =
            (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
        )
    );

  DELETE FROM search_input_case_field WHERE case_field_id IN
    (SELECT id FROM case_field WHERE case_type_id IN
        (SELECT id FROM case_type WHERE jurisdiction_id =
            (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
        )
    );

  DELETE FROM search_alias_field WHERE case_type_id IN
    (SELECT id FROM case_type WHERE jurisdiction_id =
        (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
    );

  DELETE FROM complex_field_acl WHERE case_field_id IN
    (SELECT id FROM case_field WHERE case_type_id IN
        (SELECT id FROM case_type WHERE jurisdiction_id =
            (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
         )
    );

  DELETE FROM case_field WHERE case_type_id IN
    (SELECT id FROM case_type WHERE jurisdiction_id =
        (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
    );

  DELETE FROM display_group WHERE case_type_id IN
    (SELECT id FROM case_type WHERE jurisdiction_id =
        (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
    );

  DELETE FROM event_webhook WHERE event_id IN
    (SELECT id FROM event WHERE case_type_id IN
        (SELECT id FROM case_type WHERE jurisdiction_id =
            (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
        )
    );

  DELETE FROM event_pre_state WHERE event_id IN
    (SELECT id FROM event WHERE case_type_id IN
        (SELECT id FROM case_type WHERE jurisdiction_id =
            (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
        )
    );

  DELETE FROM event_acl WHERE event_id IN
    (SELECT id FROM event WHERE case_type_id IN
        (SELECT id FROM case_type WHERE jurisdiction_id =
            (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
        )
    );

  DELETE FROM event WHERE case_type_id IN
    (SELECT id FROM case_type WHERE jurisdiction_id =
        (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
    );

  DELETE FROM state_acl WHERE state_id IN
    (SELECT id FROM state WHERE case_type_id IN
        (SELECT id FROM case_type WHERE jurisdiction_id =
            (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
        )
    );

  DELETE FROM state WHERE case_type_id IN
    (SELECT id FROM case_type WHERE jurisdiction_id =
        (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
    );

  DELETE FROM case_type_acl WHERE case_type_id IN
    (SELECT id FROM case_type WHERE jurisdiction_id =
        (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
    );

  DELETE FROM role WHERE case_type_id IN
    (SELECT id FROM case_type WHERE jurisdiction_id =
        (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
    );

  DELETE FROM case_type WHERE jurisdiction_id =
    (SELECT id FROM jurisdiction WHERE reference = jurisdictionId);

  DELETE FROM field_type_list_item WHERE field_type_id IN
    (SELECT id FROM field_type WHERE jurisdiction_id =
        (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
    );

  DELETE FROM complex_field WHERE complex_field_type_id IN
    (SELECT id FROM field_type WHERE jurisdiction_id =
        (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
    );

  DELETE FROM field_type WHERE jurisdiction_id =
    (SELECT id FROM jurisdiction WHERE reference = jurisdictionId);

  DELETE FROM jurisdiction_ui_config WHERE jurisdiction_id =
    (SELECT id FROM jurisdiction WHERE reference = jurisdictionId);

  DELETE FROM challenge_question WHERE case_type_id IN
    (SELECT id FROM case_type WHERE jurisdiction_id =
        (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
    );

  DELETE FROM definition_designer WHERE jurisdiction_id =
    (SELECT id FROM jurisdiction WHERE reference = jurisdictionId);

  DELETE FROM banner WHERE jurisdiction_id =
    (SELECT id FROM jurisdiction WHERE reference = jurisdictionId);

  DELETE FROM state_test WHERE case_type_test_id IN
    (SELECT id FROM case_type_test WHERE jurisdiction_id =
        (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
    );

  DELETE FROM webhook_test WHERE id IN
    (SELECT print_webhook_test_id FROM case_type_test WHERE jurisdiction_id =
        (SELECT id FROM jurisdiction WHERE reference = jurisdictionId)
    );

  DELETE FROM case_type_test WHERE jurisdiction_id =
    (SELECT id FROM jurisdiction WHERE reference = jurisdictionId);

  DELETE FROM jurisdiction WHERE reference = jurisdictionId;
END $$;
