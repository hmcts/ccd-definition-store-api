--
-- Name: access_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.access_type (
                                        id integer NOT NULL,
                                        live_from date,
                                        live_to date,
                                        case_type_id integer NOT NULL,
                                        access_type_id character varying(200) NOT NULL,
                                        organisation_profile_id character varying(200) NOT NULL,
                                        access_mandatory boolean,
                                        access_default boolean,
                                        display boolean,
                                        description character varying(200),
                                        hint character varying(300),
                                        display_order integer
);

--
-- Name: access_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.access_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: access_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.access_type_id_seq OWNED BY public.access_type.id;

--
-- Name: display_group fk_access_type_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.access_type
    ADD CONSTRAINT fk_access_type_case_type_id FOREIGN KEY (case_type_id) REFERENCES public.case_type(id);

--
-- Name: access_type pk_access_type; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.access_type
    ADD CONSTRAINT pk_access_type PRIMARY KEY (id);

--
-- Name: access_type id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.access_type ALTER COLUMN id SET DEFAULT nextval('public.access_type_id_seq'::regclass);

