insert into field_type (created_at, reference, version, base_field_type_id)
values (now(), 'PreviousOrganisation', 1,
    (select id from field_type where reference = 'Complex'
        and jurisdiction_id is null
        and version = (select max(version)
        from field_type where reference = 'Complex'
        and jurisdiction_id is null
        and base_field_type_id is null))
);

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('FromTimestamp', 'FromTimestamp', 'PUBLIC',
    (select id from field_type where reference = 'DateTime' and version = 1 and jurisdiction_id is null),
    (select id from field_type where reference = 'PreviousOrganisation' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('ToTimestamp', 'ToTimestamp', 'PUBLIC',
    (select id from field_type where reference = 'DateTime' and version = 1 and jurisdiction_id is null),
    (select id from field_type where reference = 'PreviousOrganisation' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('OrganisationName', 'OrganisationName', 'PUBLIC',
    (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
    (select id from field_type where reference = 'PreviousOrganisation' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('OrganisationAddress', 'OrganisationAddress', 'PUBLIC',
    (select id from field_type where reference = 'AddressUK' and version = 1 and jurisdiction_id is null),
    (select id from field_type where reference = 'PreviousOrganisation' and version = 1 and jurisdiction_id is null));

insert into field_type (created_at, reference, version, base_field_type_id, collection_field_type_id)
values (now(), 'PreviousOrganisationCollection', 1,
    (select id from field_type where reference = 'Collection'
        and jurisdiction_id is null
        and version = (select max(version)
        from field_type where reference = 'Collection'
        and jurisdiction_id is null
        and base_field_type_id is null)),
    (select id from field_type where reference = 'PreviousOrganisation'
        and jurisdiction_id is null
        and version = 1)
);

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('PreviousOrganisations', 'PreviousOrganisations', 'PUBLIC',
    (select id from field_type where reference = 'PreviousOrganisationCollection' and version = 1 and jurisdiction_id is null),
    (select id from field_type where reference = 'OrganisationPolicy' and version = 1 and jurisdiction_id is null));
