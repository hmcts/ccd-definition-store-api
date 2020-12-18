insert into field_type (created_at, reference, version, base_field_type_id)
values (now(), 'CaseLocation', 1,
    (select id from field_type where reference = 'Complex'
        and jurisdiction_id is null
        and version = (select max(version)
        from field_type where reference = 'Complex'
        and jurisdiction_id is null
        and base_field_type_id is null))
);

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('Region ID', 'Region ID', 'PUBLIC',
    (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
    (select id from field_type where reference = 'CaseLocation' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('Base Location ID', 'Base Location ID', 'PUBLIC',
    (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
    (select id from field_type where reference = 'CaseLocation' and version = 1 and jurisdiction_id is null));
