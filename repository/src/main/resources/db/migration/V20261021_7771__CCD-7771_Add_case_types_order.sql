ALTER TABLE case_type
    ADD COLUMN case_types_order INTEGER,
    ADD CONSTRAINT chk_case_type_case_types_order
        CHECK (case_types_order BETWEEN 0 AND 99);
