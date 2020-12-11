UPDATE complex_field SET field_type_id = (SELECT id from field_type WHERE field_type.reference = 'DynamicList' and version = 1 and jurisdiction_id is null)
    WHERE reference = 'CaseRoleId'
    AND complex_field_type_id = (select id from field_type where reference = 'ChangeOrganisationRequest' and version = 1 and jurisdiction_id is null);
