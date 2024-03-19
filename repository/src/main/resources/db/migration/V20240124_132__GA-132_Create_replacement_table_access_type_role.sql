DROP TABLE access_type_roles;

--
-- Name: access_type_role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.access_type_role (
                                        id integer NOT NULL,
                                        live_from date,
                                        live_to date,
                                        case_type_id integer NOT NULL,
                                        access_type_id character varying(200) NOT NULL,
                                        organisation_profile_id character varying(200) NOT NULL,
                                        organisational_role_name character varying(70),
                                        group_role_name character varying(70),
                                        case_assigned_role_field character varying(70),
                                        group_access_enabled boolean,
                                        case_access_group_id_template character varying(200)
);

--
-- Name: access_type_role_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.access_type_role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: access_type_role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.access_type_role_id_seq OWNED BY public.access_type_role.id;

--
-- Name: display_group fk_access_type_role_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.access_type_role
    ADD CONSTRAINT fk_access_type_role_case_type_id FOREIGN KEY (case_type_id) REFERENCES public.case_type(id);

--
-- Name: access_type_role pk_access_type_role; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.access_type_role
    ADD CONSTRAINT pk_access_type_role PRIMARY KEY (id);

--
-- Name: access_type_role id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.access_type_role ALTER COLUMN id SET DEFAULT nextval('public.access_type_role_id_seq'::regclass);

