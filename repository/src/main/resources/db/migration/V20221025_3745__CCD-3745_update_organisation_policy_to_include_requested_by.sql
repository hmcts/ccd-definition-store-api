INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('LastNoCRequestedBy', 'Last NoC Requested By', 'PUBLIC',
        (select id from field_type where reference = 'Email' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'OrganisationPolicy' and version = 1 and jurisdiction_id is null));