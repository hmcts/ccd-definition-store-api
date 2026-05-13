CREATE TABLE public.shell_mapping (
                                        id integer NOT NULL,
                                        live_from date,
                                        live_to date,
                                        shell_case_type_id integer NOT NULL,
                                        shell_case_field_name integer NOT NULL,
                                        originating_case_type_id integer NOT NULL,
                                        originating_case_field_name integer NOT NULL
);

CREATE SEQUENCE public.shell_mapping_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE public.shell_mapping_seq OWNED BY public.shell_mapping.id;

ALTER TABLE ONLY public.shell_mapping
    ADD CONSTRAINT pk_shell_mapping PRIMARY KEY (id);

ALTER TABLE ONLY public.shell_mapping
    ADD CONSTRAINT fk_shell_mapping_case_type_id FOREIGN KEY (shell_case_type_id) REFERENCES public.case_type(id);

ALTER TABLE ONLY public.shell_mapping
    ADD CONSTRAINT fk_shell_mapping_shell_case_field_name FOREIGN KEY (shell_case_field_name) REFERENCES public.case_field(id);

ALTER TABLE ONLY public.shell_mapping
    ADD CONSTRAINT fk_shell_mapping_originating_case_type_id FOREIGN KEY (originating_case_type_id) REFERENCES public.case_type(id);

ALTER TABLE ONLY public.shell_mapping
    ADD CONSTRAINT fk_shell_mapping_originating_case_field_name FOREIGN KEY (originating_case_field_name) REFERENCES public.case_field(id);

ALTER TABLE ONLY public.shell_mapping ALTER COLUMN id SET DEFAULT nextval('public.shell_mapping_seq'::regclass);
