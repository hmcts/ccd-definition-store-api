
insert into field_type (created_at, reference, version, base_field_type_id, collection_field_type_id)
values (now(), 'CaseAccessGroups', 1,
    (select id from field_type where reference = 'Collection'
        and jurisdiction_id is null
        and version = (select max(version)
        from field_type where reference = 'Collection'
        and jurisdiction_id is null
        and base_field_type_id is null)),
    (select id from field_type where reference = 'CaseAccessGroup'
        and jurisdiction_id is null
        and version = 1)
);

