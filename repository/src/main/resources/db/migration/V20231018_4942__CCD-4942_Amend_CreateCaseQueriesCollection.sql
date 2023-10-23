insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('partyName', 'party name', 'PUBLIC',
    (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
    (select id from field_type where reference = 'CaseQueriesCollection' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('roleOnCase', 'role on case', 'PUBLIC',
    (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
    (select id from field_type where reference = 'CaseQueriesCollection' and version = 1 and jurisdiction_id is null));
