--
-- Name: reindex; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reindex (
                                        id integer NOT NULL,
                                        start_time timestamp NOT NULL,
                                        end_time timestamp NOT NULL,
                                        status character varying(50) NOT NULL,
                                        case_type_id integer NOT NULL,
                                        jurisdiction character varying(70) NOT NULL,
                                        message character varying(200)
);
--
-- Name: reindex_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.reindex_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: reindex_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.reindex_id_seq OWNED BY public.reindex.id;

--
-- Name: reindex fk_reindex_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reindex
    ADD CONSTRAINT fk_reindex_case_type_id FOREIGN KEY (case_type_id) REFERENCES public.case_type(id);

--
-- Name: reindex pk_reindex; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reindex
    ADD CONSTRAINT pk_reindex PRIMARY KEY (id);

--
-- Name: reindex id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reindex ALTER COLUMN id SET DEFAULT nextval('public.reindex_id_seq'::regclass);

