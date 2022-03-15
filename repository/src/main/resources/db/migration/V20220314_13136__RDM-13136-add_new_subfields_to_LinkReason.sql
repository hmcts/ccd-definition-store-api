INSERT INTO public.field_type (created_at, reference, base_field_type_id, collection_field_type_id, version)
VALUES (now(), 'ReasonForLinkList',
        (select id from field_type where reference = 'Collection' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'LinkReason' and version = 1 and jurisdiction_id is null), '1');

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('ReasonForLink', 'ReasonForLink', 'PUBLIC',
        (select id from field_type where reference = 'ReasonForLinkList' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'CaseLink' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('CreatedDateTime', 'Created Date Time', 'PUBLIC',
        (select id from field_type where reference = 'DateTime' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'CaseLink' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('CaseType', 'Case Type', 'PUBLIC',
        (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'CaseLink' and version = 1 and jurisdiction_id is null));
