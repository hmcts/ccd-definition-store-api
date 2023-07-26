insert into field_type (created_at, reference, version, base_field_type_id)
values (now(), 'CaseQueriesCollection', 1,
(select id from field_type where reference = 'Complex'
and jurisdiction_id is null
and version = (select max(version)
from field_type where reference = 'Complex'
and jurisdiction_id is null
and base_field_type_id is null))
);

insert into field_type (created_at, reference, version, base_field_type_id, collection_field_type_id)
values (now(), 'caseMessageCollection', 1,
    (select id from field_type where reference = 'Collection'
        and jurisdiction_id is null
        and version = (select max(version)
        from field_type where reference = 'Collection'
        and jurisdiction_id is null
        and base_field_type_id is null)),
    (select id from field_type where reference = 'CaseMessage'
        and jurisdiction_id is null
        and version = 1)
);

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('CaseMessages', 'Case messages', 'PUBLIC',
    (select id from field_type where reference = 'caseMessageCollection' and version = 1 and jurisdiction_id is null),
    (select id from field_type where reference = 'CaseQueriesCollection' and version = 1 and jurisdiction_id is null));
