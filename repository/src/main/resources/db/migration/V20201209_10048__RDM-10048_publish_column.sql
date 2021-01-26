ALTER TABLE event_case_field_complex_type ADD COLUMN publish BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE event_case_field_complex_type ADD COLUMN publish_as VARCHAR(70);

ALTER TABLE event_case_field ADD COLUMN publish BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE event_case_field ADD COLUMN publish_as VARCHAR(70);
