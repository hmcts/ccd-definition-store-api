insert into complex_field (reference, label, security_classification, retain_hidden_value, field_type_id, complex_field_type_id)
values ('name_cy', 'Name of Flag In Welsh', 'PUBLIC', true,
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, retain_hidden_value, field_type_id, complex_field_type_id)
values ('subTypeValue_cy', 'Flag subType Value In Welsh', 'PUBLIC', true,
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, retain_hidden_value, field_type_id, complex_field_type_id)
values ('otherDescription_cy', 'Flag Name In Welsh', 'PUBLIC', true
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, retain_hidden_value, field_type_id, complex_field_type_id)
values ('flagComment_cy', 'Additional Comments In Welsh', 'PUBLIC', true
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, retain_hidden_value, field_type_id, complex_field_type_id)
values ('flagUpdateComment', 'Flag Update Comment', 'PUBLIC', true,
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, retain_hidden_value, field_type_id, complex_field_type_id)
values ('availableExternally', 'Availability to Non Staff', 'PUBLIC', true,
(select id from field_type where reference = 'YesOrNo' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));
