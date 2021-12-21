insert into field_type (created_at, reference, version, base_field_type_id)
values (now(), 'FlagDetails', 1,
(select id from field_type where reference = 'Complex'
and jurisdiction_id is null
and version = (select max(version)
from field_type where reference = 'Complex'
and jurisdiction_id is null
and base_field_type_id is null))
);

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('name', 'Name', 'PUBLIC',
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('subTypeValue', 'Value', 'PUBLIC',
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('subTypeKey', 'Key', 'PUBLIC',
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('otherDescription', 'Other Description', 'PUBLIC',
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('flagComment', 'Comments', 'PUBLIC',
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('dateTimeModified', 'Modified Date', 'PUBLIC',
(select id from field_type where reference = 'DateTime' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('dateTimeCreated', 'Created Date', 'PUBLIC',
(select id from field_type where reference = 'DateTime' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));


--- Path and Path Collection ---
INSERT INTO public.field_type (created_at, reference, base_field_type_id, version)
VALUES (now(), 'Path', (select id
from field_type
where reference = 'Complex'
and jurisdiction_id is null
and version = (select max(version)
from field_type
where reference = 'Complex'
and jurisdiction_id is null
and base_field_type_id is null)), '1');

INSERT INTO public.field_type (created_at, reference, base_field_type_id, collection_field_type_id, version)
VALUES (now(), 'PathCollection',
(select id from field_type where reference = 'Collection' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'Path' and version = 1 and jurisdiction_id is null), '1');

--- Add Path Collection to FlgDetails ---
insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('path', 'Path', 'PUBLIC',
(select id from field_type where reference = 'PathCollection' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));


insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('hearingRelevant', 'Requires Hearing', 'PUBLIC',
(select id from field_type where reference = 'YesOrNo' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('flagCode', 'Reference Code', 'PUBLIC',
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('status', 'Status', 'PUBLIC',
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'FlagDetails' and version = 1 and jurisdiction_id is null));

insert into field_type (created_at, reference, version, base_field_type_id, collection_field_type_id)
values (now(), 'FlagDetailsCollection', 1,
(select id from field_type where reference = 'Collection'
and jurisdiction_id is null
and version = (select max(version)
from field_type where reference = 'Collection'
and jurisdiction_id is null
and base_field_type_id is null)),
(select id from field_type where reference = 'FlagDetails'
and jurisdiction_id is null
and version = 1)
);

insert into field_type (created_at, reference, version, base_field_type_id)
values (now(), 'Flags', 1,
(select id from field_type where reference = 'Complex'
and jurisdiction_id is null
and version = (select max(version)
from field_type where reference = 'Complex'
and jurisdiction_id is null
and base_field_type_id is null))
);

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('FlagType', 'Flag Type', 'PUBLIC',
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'Flags' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('PartyName', 'Party Name', 'PUBLIC',
(select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'Flags' and version = 1 and jurisdiction_id is null));

insert into complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
values ('FlagDetails', 'Flag Details', 'PUBLIC',
(select id from field_type where reference = 'FlagDetailsCollection' and version = 1 and jurisdiction_id is null),
(select id from field_type where reference = 'Flags' and version = 1 and jurisdiction_id is null));

