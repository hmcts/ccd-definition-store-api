DROP TABLE IF EXISTS public.reindex CASCADE;
DROP SEQUENCE IF EXISTS public.reindex_id_seq;
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
-- Name: reindex; Type: TABLE; Schema: public; Owner: -
--
CREATE TABLE public.reindex (
    id             integer NOT NULL DEFAULT nextval('public.reindex_id_seq'::regclass),
    start_time     timestamp NOT NULL,
    end_time       timestamp NOT NULL,
    status         varchar(50) NOT NULL,
    case_type_id   integer NOT NULL,
    jurisdiction   varchar(70) NOT NULL,
    message        varchar(200),
    CONSTRAINT pk_reindex PRIMARY KEY (id),
    CONSTRAINT fk_reindex_case_type_id FOREIGN KEY (case_type_id) REFERENCES public.case_type(id)
);

--
-- Name: reindex_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.reindex_id_seq OWNED BY public.reindex.id;

