insert into field_type (created_at, reference, version, base_field_type_id)
values (now(), 'SearchParty', 1,
        (select id
         from field_type
         where reference = 'Complex'
           and jurisdiction_id is null
           and version = (select max(version)
                          from field_type
                          where reference = 'Complex'
                            and jurisdiction_id is null
                            and base_field_type_id is null)));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('Name', 'Name', 'PUBLIC',
        (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'SearchParty' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('EmailAddress', 'EmailAddress', 'PUBLIC',
        (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'SearchParty' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('AddressLine1', 'AddressLine1', 'PUBLIC',
        (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'SearchParty' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('PostCode', 'PostCode', 'PUBLIC',
        (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'SearchParty' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('DateOfBirth', 'DateOfBirth', 'PUBLIC',
        (select id from field_type where reference = 'DateTime' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'SearchParty' and version = 1 and jurisdiction_id is null));