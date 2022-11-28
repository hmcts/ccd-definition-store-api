UPDATE complex_field SET retain_hidden_value = true
where complex_field_type_id in (
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'Flags' and version = 1 and jurisdiction_id is null));