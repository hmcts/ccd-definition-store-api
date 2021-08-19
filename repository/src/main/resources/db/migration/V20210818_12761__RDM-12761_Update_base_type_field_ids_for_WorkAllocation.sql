UPDATE complex_field SET reference = 'region' WHERE reference='RegionId' and complex_field_type_id = (select id from field_type where reference = 'CaseLocation' and jurisdiction_id is null);

UPDATE complex_field SET reference = 'baseLocation' WHERE reference='BaseLocationId' and complex_field_type_id = (select id from field_type where reference = 'CaseLocation' and jurisdiction_id is null);
