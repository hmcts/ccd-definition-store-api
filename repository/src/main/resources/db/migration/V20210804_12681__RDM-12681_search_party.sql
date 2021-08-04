--
-- Name: search_party_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.search_party_id_seq
START WITH 1
INCREMENT BY 1
NO MINVALUE
NO MAXVALUE
CACHE 1;



CREATE TABLE public.search_party (
  id integer NOT NULL DEFAULT nextval('public.search_party_id_seq'::regclass),
  live_from date,
  live_to date,
  search_party_name character varying(2000) NOT NULL,
  case_type_id integer NOT NULL,
  search_party_email_address character varying(2000),
  search_party_address_line_1 character varying(2000),
  search_party_post_code character varying(2000),
  search_party_dob character varying(2000)
);


--
-- Name: search_party_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.search_party_id_seq OWNED BY public.search_party.id;


ALTER TABLE ONLY public.search_party
ADD CONSTRAINT fk_case_field_search_party FOREIGN KEY (case_type_id) REFERENCES public.case_type(id);

ALTER TABLE ONLY public.search_party
ADD CONSTRAINT unique_search_party_name_case_type_id_search_party UNIQUE (search_party_name, case_type_id);
