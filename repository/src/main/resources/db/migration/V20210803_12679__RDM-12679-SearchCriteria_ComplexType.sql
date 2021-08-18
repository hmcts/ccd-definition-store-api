insert into field_type (created_at, reference, version, base_field_type_id)
values (now(), 'SearchCriteria', 1,
        (select id
         from field_type
         where reference = 'Complex'
           and jurisdiction_id is null
           and version = (select max(version)
                          from field_type
                          where reference = 'Complex'
                            and jurisdiction_id is null
                            and base_field_type_id is null)));

INSERT INTO public.field_type (created_at, reference, base_field_type_id, collection_field_type_id, version)
VALUES (now(), 'OtherCaseReferencesList',
        (select id from field_type where reference = 'Collection' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null), '1');

INSERT INTO public.field_type (created_at, reference, base_field_type_id, collection_field_type_id, version)
VALUES (now(), 'SearchCriteriaList',
        (select id from field_type where reference = 'Collection' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'SearchParty' and version = 1 and jurisdiction_id is null), '1');

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('OtherCaseReferences', 'OtherCaseReferences', 'PUBLIC',
        (select id from field_type where reference = 'OtherCaseReferencesList' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'SearchCriteria' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('SearchParties', 'SearchParties', 'PUBLIC',
        (select id from field_type where reference = 'SearchCriteriaList' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'SearchCriteria' and version = 1 and jurisdiction_id is null));
