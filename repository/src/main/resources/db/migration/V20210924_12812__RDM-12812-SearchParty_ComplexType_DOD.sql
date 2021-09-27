insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('DateOfDeath', 'DateOfDeath', 'PUBLIC',
        (select id from field_type where reference = 'DateTime' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'SearchParty' and version = 1 and jurisdiction_id is null));
