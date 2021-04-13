UPDATE complex_field SET label = 'From Timestamp'
    WHERE reference = 'FromTimestamp'
    AND field_type_id = (select id from field_type where reference = 'DateTime' and version = 1 and jurisdiction_id is null)
    AND complex_field_type_id = (select id from field_type where reference = 'PreviousOrganisation' and version = 1 and jurisdiction_id is null);

UPDATE complex_field SET label = 'To Timestamp'
    WHERE reference = 'ToTimestamp'
    AND field_type_id = (select id from field_type where reference = 'DateTime' and version = 1 and jurisdiction_id is null)
    AND complex_field_type_id = (select id from field_type where reference = 'PreviousOrganisation' and version = 1 and jurisdiction_id is null);

UPDATE complex_field SET label = 'Organisation Name'
    WHERE reference = 'OrganisationName'
    AND field_type_id = (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null)
    AND complex_field_type_id = (select id from field_type where reference = 'PreviousOrganisation' and version = 1 and jurisdiction_id is null);

UPDATE complex_field SET label = 'Organisation Address'
    WHERE reference = 'OrganisationAddress'
    AND field_type_id = (select id from field_type where reference = 'AddressUK' and version = 1 and jurisdiction_id is null)
    AND complex_field_type_id = (select id from field_type where reference = 'PreviousOrganisation' and version = 1 and jurisdiction_id is null);

UPDATE complex_field SET label = 'Previous Organisations'
    WHERE reference = 'PreviousOrganisations'
    AND field_type_id = (select id from field_type where reference = 'PreviousOrganisationCollection' and version = 1 and jurisdiction_id is null)
    AND complex_field_type_id = (select id from field_type where reference = 'OrganisationPolicy' and version = 1 and jurisdiction_id is null);
