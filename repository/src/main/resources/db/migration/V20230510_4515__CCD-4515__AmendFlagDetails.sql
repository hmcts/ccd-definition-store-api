UPDATE complex_field SET display_order=1
where complex_field_type_id in (
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null))
and complex_field.id = (select id from complex_field where reference = 'name');

insert into complex_field (reference, label, security_classification, retain_hidden_value, display_order, field_type_id, complex_field_type_id)
values ('name_cy', 'Name in Welsh', 'PUBLIC', true, 2,
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));

UPDATE complex_field SET display_order=3
where complex_field_type_id in (
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null))
and complex_field.id = (select id from complex_field where reference = 'subTypeValue');

insert into complex_field (reference, label, security_classification, retain_hidden_value, display_order, field_type_id, complex_field_type_id)
values ('subTypeValue_cy', 'Value in Welsh', 'PUBLIC', true, 4,
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));

UPDATE complex_field SET display_order=5
where complex_field_type_id in (
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null))
and complex_field.id = (select id from complex_field where reference = 'subTypeKey');

UPDATE complex_field SET display_order=6
where complex_field_type_id in (
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null))
and complex_field.id = (select id from complex_field where reference = 'otherDescription');

insert into complex_field (reference, label, security_classification, retain_hidden_value, display_order, field_type_id, complex_field_type_id)
values ('otherDescription_cy', 'Other Description in Welsh', 'PUBLIC', true, 7,
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));

UPDATE complex_field SET display_order=8
where complex_field_type_id in (
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null))
and complex_field.id = (select id from complex_field where reference = 'flagComment');

insert into complex_field (reference, label, security_classification, retain_hidden_value, display_order, field_type_id, complex_field_type_id)
values ('flagComment_cy', 'Comments in Welsh', 'PUBLIC', true, 9,
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, retain_hidden_value, display_order, field_type_id, complex_field_type_id)
values ('flagUpdateComment', 'Update Comments', 'PUBLIC', true, 10,
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));

UPDATE complex_field SET display_order=11
where complex_field_type_id in (
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null))
and complex_field.id = (select id from complex_field where reference = 'dateTimeModified');

UPDATE complex_field SET display_order=12
where complex_field_type_id in (
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null))
and complex_field.id = (select id from complex_field where reference = 'dateTimeCreated');

UPDATE complex_field SET display_order=13
where complex_field_type_id in (
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null))
and complex_field.id = (select id from complex_field where reference = 'path');

UPDATE complex_field SET display_order=14
where complex_field_type_id in (
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null))
and complex_field.id = (select id from complex_field where reference = 'hearingRelevant');

UPDATE complex_field SET display_order=15
where complex_field_type_id in (
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null))
and complex_field.id = (select id from complex_field where reference = 'flagCode');

UPDATE complex_field SET display_order=16
where complex_field_type_id in (
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null))
and complex_field.id = (select id from complex_field where reference = 'status');
