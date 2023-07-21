insert into field_type (created_at, reference, version, base_field_type_id)
values (now(), 'CaseMessage', 1,
(select id from field_type where reference = 'Complex'
and jurisdiction_id is null
and version = (select max(version)
from field_type where reference = 'Complex'
and jurisdiction_id is null
and base_field_type_id is null))
);

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('idamId', 'IdamId', 'PUBLIC',
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'JudicialUser' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('subject', 'Subject', 'PUBLIC',
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'JudicialUser' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('body', 'Body', 'PUBLIC',
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'JudicialUser' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('attachments', 'PersonalCode', 'PUBLIC',
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'JudicialUser' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('isHearingRelated', 'isHearingRelated', 'PUBLIC',
(select id from field_type where reference = 'YesOrNo' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'JudicialUser' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('hearingDate', 'hearingDate', 'PUBLIC',
(select id from field_type where reference = 'Date' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'JudicialUser' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('createdOn', 'createdOn', 'PUBLIC',
(select id from field_type where reference = 'Date' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'JudicialUser' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('createdBy', 'createdBy', 'PUBLIC',
(select id from field_type where reference = "DateTime" and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'JudicialUser' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('parentId', 'parentId', 'PUBLIC',
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'JudicialUser' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('name', 'name', 'PUBLIC',
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'JudicialUser' and version = 1 and jurisdiction_id is null));