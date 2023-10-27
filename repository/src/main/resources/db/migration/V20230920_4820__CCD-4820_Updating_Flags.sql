INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('groupId', 'A GUID. To be set by the service when creating the 2 collections', 'PUBLIC',
        (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'Flags' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('visibility', '	Visibility should be one of 2 values: Internal or External', 'PUBLIC',
        (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'Flags' and version = 1 and jurisdiction_id is null));
