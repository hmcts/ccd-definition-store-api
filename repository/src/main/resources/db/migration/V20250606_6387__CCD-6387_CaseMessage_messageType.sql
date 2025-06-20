-- Migration to add 'messageType' field to CaseMessage complex type
insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('messageType', 'messageType', 'PUBLIC',
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'CaseMessage' and version = 1 and jurisdiction_id is null));

