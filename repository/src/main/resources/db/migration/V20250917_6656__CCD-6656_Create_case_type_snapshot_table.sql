CREATE TABLE case_type_snapshot (
                                    id integer NOT NULL,
                                    case_type_reference VARCHAR(70) NOT NULL UNIQUE,
                                    version_id INT NOT NULL,
                                    precomputed_response JSONB NOT NULL,
                                    created_at TIMESTAMP DEFAULT NOW(),
                                    last_modified TIMESTAMP DEFAULT NOW()
);


CREATE SEQUENCE case_type_snapshot_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE case_type_snapshot
    ALTER COLUMN id SET DEFAULT nextval('case_type_snapshot_id_seq');

ALTER TABLE case_type_snapshot
    ADD PRIMARY KEY (id);
