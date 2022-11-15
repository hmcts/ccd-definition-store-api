insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('UPRN', 'UPRN', 'PUBLIC',
        (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressUK' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('UPRN', 'UPRN', 'PUBLIC',
        (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressGlobalUK' and version = 1 and jurisdiction_id is null));
