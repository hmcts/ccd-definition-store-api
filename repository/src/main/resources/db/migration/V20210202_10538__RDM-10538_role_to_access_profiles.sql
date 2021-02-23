--
-- Name: role_to_access_profiles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.role_to_access_profiles_id_seq
START WITH 1
INCREMENT BY 1
NO MINVALUE
NO MAXVALUE
CACHE 1;



CREATE TABLE public.role_to_access_profiles (
  id integer NOT NULL DEFAULT nextval('public.role_to_access_profiles_id_seq'::regclass),
  live_from date,
  live_to date,
  role_name character varying(70) NOT NULL,
  case_type_id integer NOT NULL,
  access_profiles character varying(2000) NOT NULL,
  authorisation character varying(2000),
  read_only boolean DEFAULT FALSE,
  disabled boolean DEFAULT FALSE
);


--
-- Name: role_to_access_profiles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.role_to_access_profiles_id_seq OWNED BY public.role_to_access_profiles.id;


ALTER TABLE ONLY public.role_to_access_profiles
ADD CONSTRAINT fk_case_field_role_to_access_profiles FOREIGN KEY (case_type_id) REFERENCES public.case_type(id);

ALTER TABLE ONLY public.role_to_access_profiles
ADD CONSTRAINT unique_role_name_case_type_id_role_to_access_profiles UNIQUE (role_name, case_type_id);
