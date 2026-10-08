insert into complex_field (reference, label, security_classification, retain_hidden_value, display_order,
                           field_type_id, complex_field_type_id)
values ('flagUpdateComment_cy', 'Update Comments in Welsh', 'PUBLIC', true, 11,
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));

update complex_field
set display_order = display_order + 1
where complex_field_type_id = (select id from field_type
                               where reference = 'FlagDetails'
                                 and version = 1
                                 and jurisdiction_id is null)
  and display_order >= 11
  and reference <> 'flagUpdateComment_cy';
