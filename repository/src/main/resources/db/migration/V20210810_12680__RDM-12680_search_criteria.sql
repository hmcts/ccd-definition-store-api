--
-- Name: search_criteria_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.search_criteria_id_seq
START WITH 1
INCREMENT BY 1
NO MINVALUE
NO MAXVALUE
CACHE 1;



CREATE TABLE public.search_criteria (
  id integer NOT NULL DEFAULT nextval('public.search_criteria_id_seq'::regclass),
  live_from date,
  live_to date,
  other_case_reference character varying(200),
  case_type_id integer NOT NULL,
);


--
-- Name: search_criteria_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.search_criteria_id_seq OWNED BY public.search_criteria.id;


ALTER TABLE ONLY public.search_criteria
ADD CONSTRAINT fk_case_field_search_criteria FOREIGN KEY (case_type_id) REFERENCES public.case_type(id);
