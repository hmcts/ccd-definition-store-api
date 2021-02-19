--
-- PostgreSQL database schema only dump
--

--
-- Name: datafieldtype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.datafieldtype AS ENUM (
    'CASE_DATA',
    'METADATA'
    );


--
-- Name: definitionstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.definitionstatus AS ENUM (
    'DRAFT',
    'PUBLISHED'
    );


--
-- Name: displaycontext; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.displaycontext AS ENUM (
    'OPTIONAL',
    'MANDATORY',
    'READONLY',
    'COMPLEX'
    );


--
-- Name: security_classification; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.security_classification AS ENUM (
    'PUBLIC',
    'PRIVATE',
    'RESTRICTED'
    );


--
-- Name: webhook_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.webhook_type AS ENUM (
    'START',
    'PRE_SUBMIT',
    'POST_SUBMIT'
    );


SET default_tablespace = '';

--
-- Name: banner; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.banner (
                               id integer NOT NULL,
                               banner_enabled boolean NOT NULL,
                               banner_description character varying(300) NOT NULL,
                               banner_url_text character varying(50),
                               banner_url character varying(300),
                               created_at timestamp without time zone DEFAULT now() NOT NULL,
                               jurisdiction_id integer NOT NULL
);


--
-- Name: banner_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.banner_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: banner_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.banner_id_seq OWNED BY public.banner.id;


--
-- Name: case_field; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.case_field (
                                   id integer NOT NULL,
                                   reference character varying(70) NOT NULL,
                                   live_from date,
                                   live_to date,
                                   label text NOT NULL,
                                   hint text,
                                   hidden boolean,
                                   security_classification public.security_classification NOT NULL,
                                   field_type_id integer NOT NULL,
                                   case_type_id integer,
                                   data_field_type public.datafieldtype DEFAULT 'CASE_DATA'::public.datafieldtype NOT NULL,
                                   searchable boolean DEFAULT true NOT NULL
);


--
-- Name: case_field_acl; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.case_field_acl (
                                       id integer NOT NULL,
                                       case_field_id integer NOT NULL,
                                       "create" boolean NOT NULL,
                                       read boolean NOT NULL,
                                       update boolean NOT NULL,
                                       delete boolean NOT NULL,
                                       live_from date,
                                       live_to date,
                                       created_at timestamp without time zone DEFAULT now() NOT NULL,
                                       role_id integer NOT NULL
);


--
-- Name: case_field_acl_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.case_field_acl_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: case_field_acl_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.case_field_acl_id_seq OWNED BY public.case_field_acl.id;


--
-- Name: case_field_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.case_field_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: case_field_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.case_field_id_seq OWNED BY public.case_field.id;


--
-- Name: case_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.case_type (
                                  id integer NOT NULL,
                                  created_at timestamp without time zone NOT NULL,
                                  reference character varying(70) NOT NULL,
                                  version integer NOT NULL,
                                  live_from date,
                                  live_to date,
                                  name character varying(30) NOT NULL,
                                  description character varying(100),
                                  print_webhook_id integer,
                                  jurisdiction_id integer NOT NULL,
                                  security_classification public.security_classification NOT NULL
);


--
-- Name: case_type_acl; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.case_type_acl (
                                      id integer NOT NULL,
                                      case_type_id integer NOT NULL,
                                      "create" boolean NOT NULL,
                                      read boolean NOT NULL,
                                      update boolean NOT NULL,
                                      delete boolean NOT NULL,
                                      live_from date,
                                      live_to date,
                                      created_at timestamp without time zone DEFAULT now() NOT NULL,
                                      role_id integer NOT NULL
);


--
-- Name: case_type_acl_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.case_type_acl_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: case_type_acl_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.case_type_acl_id_seq OWNED BY public.case_type_acl.id;


--
-- Name: case_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.case_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: case_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.case_type_id_seq OWNED BY public.case_type.id;


--
-- Name: challenge_question; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.challenge_question (
                                           id integer NOT NULL,
                                           case_type_id integer NOT NULL,
                                           display_order integer NOT NULL,
                                           question_text character varying(1000) NOT NULL,
                                           answer_field_type integer NOT NULL,
                                           display_context_parameter character varying(1000),
                                           challenge_question_id character varying(70) NOT NULL,
                                           answer_field character varying(1000) NOT NULL,
                                           question_id character varying(70) NOT NULL
);


--
-- Name: challenge_question_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.challenge_question_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: challenge_question_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.challenge_question_id_seq OWNED BY public.challenge_question.id;


--
-- Name: complex_field; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.complex_field (
                                      id integer NOT NULL,
                                      reference character varying(70) NOT NULL,
                                      label character varying(200) NOT NULL,
                                      hint text,
                                      hidden boolean,
                                      security_classification public.security_classification NOT NULL,
                                      field_type_id integer NOT NULL,
                                      complex_field_type_id integer NOT NULL,
                                      show_condition character varying(1000),
                                      display_order integer,
                                      display_context_parameter character varying(1000),
                                      searchable boolean DEFAULT true NOT NULL,
                                      retain_hidden_value boolean
);


--
-- Name: complex_field_acl; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.complex_field_acl (
                                          id integer NOT NULL,
                                          case_field_id integer NOT NULL,
                                          list_element_code character varying(1000) NOT NULL,
                                          role_id integer NOT NULL,
                                          "create" boolean NOT NULL,
                                          read boolean NOT NULL,
                                          update boolean NOT NULL,
                                          delete boolean NOT NULL,
                                          live_from date,
                                          live_to date,
                                          created_at timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: complex_field_acl_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.complex_field_acl_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: complex_field_acl_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.complex_field_acl_id_seq OWNED BY public.complex_field_acl.id;


--
-- Name: complex_field_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.complex_field_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: complex_field_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.complex_field_id_seq OWNED BY public.complex_field.id;

--
-- Name: definition_designer; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.definition_designer (
                                            id integer NOT NULL,
                                            jurisdiction_id integer NOT NULL,
                                            case_types character varying(100),
                                            description character varying(100) NOT NULL,
                                            version integer NOT NULL,
                                            status public.definitionstatus NOT NULL,
                                            data jsonb NOT NULL,
                                            author character varying(70) NOT NULL,
                                            created_at timestamp without time zone DEFAULT now() NOT NULL,
                                            last_modified timestamp without time zone DEFAULT now() NOT NULL,
                                            deleted boolean DEFAULT false NOT NULL,
                                            optimistic_lock integer NOT NULL
);


--
-- Name: definition_designer_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.definition_designer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: definition_designer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.definition_designer_id_seq OWNED BY public.definition_designer.id;


--
-- Name: display_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.display_group (
                                      id integer NOT NULL,
                                      reference character varying(70) NOT NULL,
                                      label character varying(200),
                                      channel character varying(64),
                                      display_order integer,
                                      type character varying(16) NOT NULL,
                                      purpose character varying(16) NOT NULL,
                                      case_type_id integer NOT NULL,
                                      event_id integer,
                                      show_condition character varying(1000),
                                      webhook_mid_event_id integer,
                                      role_id integer,
                                      CONSTRAINT enum_display_group_purpose CHECK (((purpose)::text = ANY ((ARRAY['VIEW'::character varying, 'EDIT'::character varying])::text[]))),
                                      CONSTRAINT enum_display_group_type CHECK (((type)::text = ANY ((ARRAY['TAB'::character varying, 'PAGE'::character varying, 'EXPAND'::character varying])::text[])))
);


--
-- Name: display_group_case_field; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.display_group_case_field (
                                                 id integer NOT NULL,
                                                 live_from date,
                                                 live_to date,
                                                 display_order integer,
                                                 display_group_id integer NOT NULL,
                                                 case_field_id integer NOT NULL,
                                                 page_column_no integer,
                                                 show_condition character varying(1000),
                                                 display_context_parameter character varying(1000)
);


--
-- Name: display_group_case_field_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.display_group_case_field_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: display_group_case_field_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.display_group_case_field_id_seq OWNED BY public.display_group_case_field.id;


--
-- Name: display_group_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.display_group_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: display_group_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.display_group_id_seq OWNED BY public.display_group.id;


--
-- Name: event; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.event (
                              id integer NOT NULL,
                              reference character varying(70) NOT NULL,
                              live_from date,
                              live_to date,
                              name character varying(30) NOT NULL,
                              description character varying(100),
                              can_create boolean NOT NULL,
                              display_order integer,
                              case_type_id integer NOT NULL,
                              security_classification public.security_classification NOT NULL,
                              show_summary boolean,
                              end_button_label character varying(200),
                              show_event_notes boolean,
                              can_save_draft boolean
);


--
-- Name: event_acl; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.event_acl (
                                  id integer NOT NULL,
                                  event_id integer NOT NULL,
                                  "create" boolean NOT NULL,
                                  read boolean NOT NULL,
                                  update boolean NOT NULL,
                                  delete boolean NOT NULL,
                                  live_from date,
                                  live_to date,
                                  created_at timestamp without time zone DEFAULT now() NOT NULL,
                                  role_id integer NOT NULL
);


--
-- Name: event_acl_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.event_acl_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: event_acl_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.event_acl_id_seq OWNED BY public.event_acl.id;


--
-- Name: event_case_field; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.event_case_field (
                                         id integer NOT NULL,
                                         event_id integer NOT NULL,
                                         case_field_id integer NOT NULL,
                                         show_condition character varying(1000),
                                         show_summary_change_option boolean,
                                         display_context public.displaycontext NOT NULL,
                                         show_summary_content_option integer,
                                         label text,
                                         hint_text text,
                                         display_context_parameter character varying(1000),
                                         retain_hidden_value boolean
);


--
-- Name: event_case_field_complex_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.event_case_field_complex_type (
                                                      id integer NOT NULL,
                                                      reference character varying(70) NOT NULL,
                                                      live_from date,
                                                      live_to date,
                                                      label character varying(200),
                                                      hint text,
                                                      display_order integer,
                                                      display_context public.displaycontext NOT NULL,
                                                      show_condition character varying(1000),
                                                      event_case_field_id integer NOT NULL,
                                                      default_value character varying(200)
);


--
-- Name: event_case_field_complex_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.event_case_field_complex_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: event_case_field_complex_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.event_case_field_complex_type_id_seq OWNED BY public.event_case_field_complex_type.id;


--
-- Name: event_case_field_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.event_case_field_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: event_case_field_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.event_case_field_id_seq OWNED BY public.event_case_field.id;


--
-- Name: event_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.event_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: event_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.event_id_seq OWNED BY public.event.id;


--
-- Name: event_post_state; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.event_post_state (
                                         id integer NOT NULL,
                                         enabling_condition character varying(2000),
                                         priority integer,
                                         case_event_id integer NOT NULL,
                                         post_state_reference character varying(70)
);


--
-- Name: event_post_state_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.event_post_state_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: event_post_state_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.event_post_state_id_seq OWNED BY public.event_post_state.id;


--
-- Name: event_pre_state; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.event_pre_state (
                                        event_id integer NOT NULL,
                                        state_id integer NOT NULL
);


--
-- Name: event_webhook; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.event_webhook (
                                      id integer NOT NULL,
                                      webhook_id integer NOT NULL,
                                      event_id integer NOT NULL,
                                      webhook_type public.webhook_type NOT NULL
);


--
-- Name: event_webhook_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.event_webhook_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: event_webhook_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.event_webhook_id_seq OWNED BY public.event_webhook.id;


--
-- Name: field_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.field_type (
                                   id integer NOT NULL,
                                   created_at timestamp without time zone NOT NULL,
                                   reference character varying(70) NOT NULL,
                                   version integer NOT NULL,
                                   minimum text,
                                   maximum text,
                                   regular_expression text,
                                   jurisdiction_id integer,
                                   base_field_type_id integer,
                                   collection_field_type_id integer
);


--
-- Name: field_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.field_type_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: field_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.field_type_id_seq OWNED BY public.field_type.id;


--
-- Name: field_type_list_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.field_type_list_item (
                                             id integer NOT NULL,
                                             value character varying(150) NOT NULL,
                                             label character varying NOT NULL,
                                             field_type_id integer NOT NULL,
                                             display_order integer
);


--
-- Name: field_type_list_item_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.field_type_list_item_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: field_type_list_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.field_type_list_item_id_seq OWNED BY public.field_type_list_item.id;


--
-- Name: jurisdiction; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.jurisdiction (
                                     id integer NOT NULL,
                                     created_at timestamp without time zone NOT NULL,
                                     reference character varying(70) NOT NULL,
                                     version integer NOT NULL,
                                     live_from timestamp without time zone,
                                     live_to timestamp without time zone,
                                     name character varying(30) NOT NULL,
                                     description character varying(100)
);


--
-- Name: jurisdiction_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.jurisdiction_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: jurisdiction_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.jurisdiction_id_seq OWNED BY public.jurisdiction.id;


--
-- Name: jurisdiction_ui_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.jurisdiction_ui_config (
                                               id integer NOT NULL,
                                               shuttered boolean NOT NULL,
                                               jurisdiction_id integer NOT NULL
);


--
-- Name: jurisdiction_ui_config_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.jurisdiction_ui_config_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: jurisdiction_ui_config_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.jurisdiction_ui_config_id_seq OWNED BY public.jurisdiction_ui_config.id;



--
-- Name: role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.role (
                             id integer NOT NULL,
                             reference character varying(255) NOT NULL,
                             name character varying(255) NOT NULL,
                             description character varying(255),
                             case_type_id integer,
                             created_at timestamp without time zone DEFAULT now() NOT NULL,
                             user_role_id integer,
                             security_classification public.security_classification DEFAULT 'PUBLIC'::public.security_classification NOT NULL,
                             dtype character varying(10) NOT NULL,
                             CONSTRAINT case_type_id_check CHECK ((
                                     CASE
                                         WHEN ((dtype)::text = 'CASEROLE'::text) THEN
                                             CASE
                                                 WHEN (case_type_id IS NOT NULL) THEN 1
                                                 ELSE 0
                                                 END
                                         ELSE 1
                                         END = 1))
);


--
-- Name: role_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.role_id_seq OWNED BY public.role.id;


--
-- Name: search_alias_field; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.search_alias_field (
                                           id integer NOT NULL,
                                           reference character varying(40) NOT NULL,
                                           case_type_id integer NOT NULL,
                                           case_field_path character varying(500) NOT NULL,
                                           field_type_id integer NOT NULL
);


--
-- Name: search_alias_field_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.search_alias_field_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: search_alias_field_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.search_alias_field_id_seq OWNED BY public.search_alias_field.id;


--
-- Name: search_cases_result_fields; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.search_cases_result_fields (
                                                   id integer NOT NULL,
                                                   live_from date,
                                                   live_to date,
                                                   case_type_id integer,
                                                   case_field_element_path character varying(300),
                                                   role_id integer,
                                                   case_field_id integer NOT NULL,
                                                   label character varying(200) NOT NULL,
                                                   hint text,
                                                   use_case text,
                                                   display_order integer,
                                                   sort_order_direction character varying(10),
                                                   sort_order_priority integer,
                                                   display_context_parameter character varying(1000)
);


--
-- Name: search_cases_result_fields_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.search_cases_result_fields_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: search_cases_result_fields_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.search_cases_result_fields_id_seq OWNED BY public.search_cases_result_fields.id;


--
-- Name: search_input_case_field; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.search_input_case_field (
                                                id integer NOT NULL,
                                                live_from date,
                                                live_to date,
                                                case_type_id integer NOT NULL,
                                                case_field_id integer NOT NULL,
                                                label character varying(200),
                                                display_order integer,
                                                case_field_element_path character varying(300),
                                                role_id integer,
                                                show_condition character varying(1000),
                                                display_context_parameter character varying(1000)
);


--
-- Name: search_input_case_field_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.search_input_case_field_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: search_input_case_field_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.search_input_case_field_id_seq OWNED BY public.search_input_case_field.id;


--
-- Name: search_result_case_field; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.search_result_case_field (
                                                 id integer NOT NULL,
                                                 live_from date,
                                                 live_to date,
                                                 case_type_id integer NOT NULL,
                                                 case_field_id integer NOT NULL,
                                                 label character varying(200),
                                                 display_order integer,
                                                 case_field_element_path character varying(300),
                                                 role_id integer,
                                                 sort_order_direction character varying(10),
                                                 sort_order_priority integer,
                                                 display_context_parameter character varying(1000)
);


--
-- Name: search_result_case_field_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.search_result_case_field_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: search_result_case_field_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.search_result_case_field_id_seq OWNED BY public.search_result_case_field.id;


--
-- Name: state; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.state (
                              id integer NOT NULL,
                              reference character varying(70) NOT NULL,
                              live_from date,
                              live_to date,
                              name character varying(100) NOT NULL,
                              description character varying(100),
                              display_order integer,
                              case_type_id integer NOT NULL,
                              title_display character varying(100)
);


--
-- Name: state_acl; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.state_acl (
                                  id integer NOT NULL,
                                  state_id integer NOT NULL,
                                  "create" boolean NOT NULL,
                                  read boolean NOT NULL,
                                  update boolean NOT NULL,
                                  delete boolean NOT NULL,
                                  live_from date,
                                  live_to date,
                                  created_at timestamp without time zone DEFAULT now() NOT NULL,
                                  role_id integer NOT NULL
);


--
-- Name: state_acl_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.state_acl_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: state_acl_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.state_acl_id_seq OWNED BY public.state_acl.id;


--
-- Name: state_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.state_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: state_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.state_id_seq OWNED BY public.state.id;


--
-- Name: webhook; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.webhook (
                                id integer NOT NULL,
                                url text NOT NULL,
                                timeouts integer[] NOT NULL,
                                CONSTRAINT webhook_timeouts_check CHECK ((array_position(timeouts, NULL::integer) IS NULL))
);


--
-- Name: webhook_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.webhook_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: webhook_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.webhook_id_seq OWNED BY public.webhook.id;


--
-- Name: workbasket_case_field; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.workbasket_case_field (
                                              id integer NOT NULL,
                                              live_from date,
                                              live_to date,
                                              case_type_id integer NOT NULL,
                                              case_field_id integer NOT NULL,
                                              label character varying(200),
                                              display_order integer,
                                              case_field_element_path character varying(300),
                                              role_id integer,
                                              sort_order_direction character varying(10),
                                              sort_order_priority integer,
                                              display_context_parameter character varying(1000)
);


--
-- Name: workbasket_case_field_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.workbasket_case_field_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: workbasket_case_field_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.workbasket_case_field_id_seq OWNED BY public.workbasket_case_field.id;


--
-- Name: workbasket_input_case_field; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.workbasket_input_case_field (
                                                    id integer NOT NULL,
                                                    live_from date,
                                                    live_to date,
                                                    case_type_id integer NOT NULL,
                                                    case_field_id integer NOT NULL,
                                                    label character varying(200),
                                                    display_order integer,
                                                    case_field_element_path character varying(300),
                                                    role_id integer,
                                                    show_condition character varying(1000),
                                                    display_context_parameter character varying(1000)
);


--
-- Name: workbasket_input_case_field_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.workbasket_input_case_field_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: workbasket_input_case_field_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.workbasket_input_case_field_id_seq OWNED BY public.workbasket_input_case_field.id;


--
-- Name: banner id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.banner ALTER COLUMN id SET DEFAULT nextval('public.banner_id_seq'::regclass);


--
-- Name: case_field id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.case_field ALTER COLUMN id SET DEFAULT nextval('public.case_field_id_seq'::regclass);


--
-- Name: case_field_acl id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.case_field_acl ALTER COLUMN id SET DEFAULT nextval('public.case_field_acl_id_seq'::regclass);


--
-- Name: case_type id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.case_type ALTER COLUMN id SET DEFAULT nextval('public.case_type_id_seq'::regclass);


--
-- Name: case_type_acl id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.case_type_acl ALTER COLUMN id SET DEFAULT nextval('public.case_type_acl_id_seq'::regclass);


--
-- Name: challenge_question id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.challenge_question ALTER COLUMN id SET DEFAULT nextval('public.challenge_question_id_seq'::regclass);


--
-- Name: complex_field id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.complex_field ALTER COLUMN id SET DEFAULT nextval('public.complex_field_id_seq'::regclass);


--
-- Name: complex_field_acl id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.complex_field_acl ALTER COLUMN id SET DEFAULT nextval('public.complex_field_acl_id_seq'::regclass);


--
-- Name: definition_designer id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.definition_designer ALTER COLUMN id SET DEFAULT nextval('public.definition_designer_id_seq'::regclass);


--
-- Name: display_group id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.display_group ALTER COLUMN id SET DEFAULT nextval('public.display_group_id_seq'::regclass);


--
-- Name: display_group_case_field id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.display_group_case_field ALTER COLUMN id SET DEFAULT nextval('public.display_group_case_field_id_seq'::regclass);


--
-- Name: event id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event ALTER COLUMN id SET DEFAULT nextval('public.event_id_seq'::regclass);


--
-- Name: event_acl id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_acl ALTER COLUMN id SET DEFAULT nextval('public.event_acl_id_seq'::regclass);


--
-- Name: event_case_field id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_case_field ALTER COLUMN id SET DEFAULT nextval('public.event_case_field_id_seq'::regclass);


--
-- Name: event_case_field_complex_type id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_case_field_complex_type ALTER COLUMN id SET DEFAULT nextval('public.event_case_field_complex_type_id_seq'::regclass);


--
-- Name: event_post_state id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_post_state ALTER COLUMN id SET DEFAULT nextval('public.event_post_state_id_seq'::regclass);


--
-- Name: event_webhook id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_webhook ALTER COLUMN id SET DEFAULT nextval('public.event_webhook_id_seq'::regclass);


--
-- Name: field_type id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.field_type ALTER COLUMN id SET DEFAULT nextval('public.field_type_id_seq'::regclass);


--
-- Name: field_type_list_item id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.field_type_list_item ALTER COLUMN id SET DEFAULT nextval('public.field_type_list_item_id_seq'::regclass);


--
-- Name: jurisdiction id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.jurisdiction ALTER COLUMN id SET DEFAULT nextval('public.jurisdiction_id_seq'::regclass);


--
-- Name: jurisdiction_ui_config id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.jurisdiction_ui_config ALTER COLUMN id SET DEFAULT nextval('public.jurisdiction_ui_config_id_seq'::regclass);



--
-- Name: role id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role ALTER COLUMN id SET DEFAULT nextval('public.role_id_seq'::regclass);


--
-- Name: search_alias_field id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_alias_field ALTER COLUMN id SET DEFAULT nextval('public.search_alias_field_id_seq'::regclass);


--
-- Name: search_cases_result_fields id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_cases_result_fields ALTER COLUMN id SET DEFAULT nextval('public.search_cases_result_fields_id_seq'::regclass);


--
-- Name: search_input_case_field id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_input_case_field ALTER COLUMN id SET DEFAULT nextval('public.search_input_case_field_id_seq'::regclass);


--
-- Name: search_result_case_field id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_result_case_field ALTER COLUMN id SET DEFAULT nextval('public.search_result_case_field_id_seq'::regclass);


--
-- Name: state id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.state ALTER COLUMN id SET DEFAULT nextval('public.state_id_seq'::regclass);


--
-- Name: state_acl id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.state_acl ALTER COLUMN id SET DEFAULT nextval('public.state_acl_id_seq'::regclass);


--
-- Name: webhook id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.webhook ALTER COLUMN id SET DEFAULT nextval('public.webhook_id_seq'::regclass);


--
-- Name: workbasket_case_field id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.workbasket_case_field ALTER COLUMN id SET DEFAULT nextval('public.workbasket_case_field_id_seq'::regclass);


--
-- Name: workbasket_input_case_field id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.workbasket_input_case_field ALTER COLUMN id SET DEFAULT nextval('public.workbasket_input_case_field_id_seq'::regclass);

--
-- Name: event_webhook event_webhook_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_webhook
    ADD CONSTRAINT event_webhook_id_key UNIQUE (id);


--
-- Name: event_webhook event_webhook_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_webhook
    ADD CONSTRAINT event_webhook_pkey PRIMARY KEY (event_id, webhook_type);


--
-- Name: case_field pk_case_field; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.case_field
    ADD CONSTRAINT pk_case_field PRIMARY KEY (id);


--
-- Name: case_field_acl pk_case_field_acl; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.case_field_acl
    ADD CONSTRAINT pk_case_field_acl PRIMARY KEY (id);


--
-- Name: case_type pk_case_type; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.case_type
    ADD CONSTRAINT pk_case_type PRIMARY KEY (id);


--
-- Name: case_type_acl pk_case_type_acl; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.case_type_acl
    ADD CONSTRAINT pk_case_type_acl PRIMARY KEY (id);


--
-- Name: challenge_question pk_challenge_question; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.challenge_question
    ADD CONSTRAINT pk_challenge_question PRIMARY KEY (id);


--
-- Name: complex_field pk_complex_field; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.complex_field
    ADD CONSTRAINT pk_complex_field PRIMARY KEY (id);


--
-- Name: complex_field_acl pk_complex_field_acl; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.complex_field_acl
    ADD CONSTRAINT pk_complex_field_acl PRIMARY KEY (id);


--
-- Name: definition_designer pk_definition_designer; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.definition_designer
    ADD CONSTRAINT pk_definition_designer PRIMARY KEY (id);


--
-- Name: display_group pk_display_group; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.display_group
    ADD CONSTRAINT pk_display_group PRIMARY KEY (id);


--
-- Name: display_group_case_field pk_display_group_case_field; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.display_group_case_field
    ADD CONSTRAINT pk_display_group_case_field PRIMARY KEY (id);


--
-- Name: event pk_event; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT pk_event PRIMARY KEY (id);


--
-- Name: event_acl pk_event_acl; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_acl
    ADD CONSTRAINT pk_event_acl PRIMARY KEY (id);


--
-- Name: event_case_field pk_event_case_field; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_case_field
    ADD CONSTRAINT pk_event_case_field PRIMARY KEY (id);


--
-- Name: event_case_field_complex_type pk_event_case_field_complex_type; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_case_field_complex_type
    ADD CONSTRAINT pk_event_case_field_complex_type PRIMARY KEY (id);


--
-- Name: event_pre_state pk_event_pre_state; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_pre_state
    ADD CONSTRAINT pk_event_pre_state PRIMARY KEY (event_id, state_id);


--
-- Name: field_type pk_field_type; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.field_type
    ADD CONSTRAINT pk_field_type PRIMARY KEY (id);


--
-- Name: field_type_list_item pk_field_type_list_item; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.field_type_list_item
    ADD CONSTRAINT pk_field_type_list_item PRIMARY KEY (id);


--
-- Name: jurisdiction pk_jurisdiction; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.jurisdiction
    ADD CONSTRAINT pk_jurisdiction PRIMARY KEY (id);


--
-- Name: role pk_role; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT pk_role PRIMARY KEY (id);


--
-- Name: search_alias_field pk_search_alias_field_reference; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_alias_field
    ADD CONSTRAINT pk_search_alias_field_reference PRIMARY KEY (id);


--
-- Name: search_cases_result_fields pk_search_cases_result_fields; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_cases_result_fields
    ADD CONSTRAINT pk_search_cases_result_fields PRIMARY KEY (id);


--
-- Name: search_input_case_field pk_search_input_case_field; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_input_case_field
    ADD CONSTRAINT pk_search_input_case_field PRIMARY KEY (id);


--
-- Name: search_result_case_field pk_search_result_case_field; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_result_case_field
    ADD CONSTRAINT pk_search_result_case_field PRIMARY KEY (id);


--
-- Name: state pk_state; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.state
    ADD CONSTRAINT pk_state PRIMARY KEY (id);


--
-- Name: state_acl pk_state_acl; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.state_acl
    ADD CONSTRAINT pk_state_acl PRIMARY KEY (id);


--
-- Name: webhook pk_webhook; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.webhook
    ADD CONSTRAINT pk_webhook PRIMARY KEY (id);


--
-- Name: workbasket_case_field pk_workbasket_case_field; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.workbasket_case_field
    ADD CONSTRAINT pk_workbasket_case_field PRIMARY KEY (id);


--
-- Name: workbasket_input_case_field pk_workbasket_input_case_field; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.workbasket_input_case_field
    ADD CONSTRAINT pk_workbasket_input_case_field PRIMARY KEY (id);


--
-- Name: event_post_state unique_case_event_id_post_state_reference; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_post_state
    ADD CONSTRAINT unique_case_event_id_post_state_reference UNIQUE (case_event_id, post_state_reference);


--
-- Name: case_field_acl unique_case_field_acl_case_field_id_role_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.case_field_acl
    ADD CONSTRAINT unique_case_field_acl_case_field_id_role_id UNIQUE (case_field_id, role_id);


--
-- Name: case_field unique_case_field_reference_case_type_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.case_field
    ADD CONSTRAINT unique_case_field_reference_case_type_id UNIQUE (reference, case_type_id);


--
-- Name: case_type_acl unique_case_type_acl_case_type_id_role_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.case_type_acl
    ADD CONSTRAINT unique_case_type_acl_case_type_id_role_id UNIQUE (case_type_id, role_id);


--
-- Name: case_type unique_case_type_reference_version; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.case_type
    ADD CONSTRAINT unique_case_type_reference_version UNIQUE (reference, version);


--
-- Name: complex_field_acl unique_complex_field_acl_case_field_id_list_elemnt_code_role_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.complex_field_acl
    ADD CONSTRAINT unique_complex_field_acl_case_field_id_list_elemnt_code_role_id UNIQUE (case_field_id, list_element_code, role_id);


--
-- Name: complex_field unique_complex_field_reference_complex_field_type_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.complex_field
    ADD CONSTRAINT unique_complex_field_reference_complex_field_type_id UNIQUE (reference, complex_field_type_id);


--
-- Name: definition_designer unique_definition_designer_jurisdiction_id_version; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.definition_designer
    ADD CONSTRAINT unique_definition_designer_jurisdiction_id_version UNIQUE (jurisdiction_id, version);


--
-- Name: display_group_case_field unique_display_group_case_field_display_group_id_case_field_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.display_group_case_field
    ADD CONSTRAINT unique_display_group_case_field_display_group_id_case_field_id UNIQUE (display_group_id, case_field_id);


--
-- Name: display_group unique_display_group_reference_case_type_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.display_group
    ADD CONSTRAINT unique_display_group_reference_case_type_id UNIQUE (reference, case_type_id);


--
-- Name: display_group unique_display_group_reference_purpose_case_type_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.display_group
    ADD CONSTRAINT unique_display_group_reference_purpose_case_type_id UNIQUE (reference, purpose, case_type_id);


--
-- Name: event_acl unique_event_acl_event_id_role_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_acl
    ADD CONSTRAINT unique_event_acl_event_id_role_id UNIQUE (event_id, role_id);


--
-- Name: event_case_field unique_event_case_field_event_id_case_field_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_case_field
    ADD CONSTRAINT unique_event_case_field_event_id_case_field_id UNIQUE (event_id, case_field_id);


--
-- Name: event unique_event_reference_case_type_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT unique_event_reference_case_type_id UNIQUE (reference, case_type_id);


--
-- Name: field_type unique_field_type_reference_version_jurisdiction; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.field_type
    ADD CONSTRAINT unique_field_type_reference_version_jurisdiction UNIQUE (reference, version, jurisdiction_id);


--
-- Name: jurisdiction unique_jurisdiction_reference_version; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.jurisdiction
    ADD CONSTRAINT unique_jurisdiction_reference_version UNIQUE (reference, version);


--
-- Name: role unique_role_case_type_id_role_reference; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT unique_role_case_type_id_role_reference UNIQUE (case_type_id, reference);


--
-- Name: search_alias_field unique_search_alias_field_reference_case_type; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_alias_field
    ADD CONSTRAINT unique_search_alias_field_reference_case_type UNIQUE (reference, case_type_id);


--
-- Name: state_acl unique_state_acl_state_id_role_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.state_acl
    ADD CONSTRAINT unique_state_acl_state_id_role_id UNIQUE (state_id, role_id);


--
-- Name: state unique_state_reference_case_type_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.state
    ADD CONSTRAINT unique_state_reference_case_type_id UNIQUE (reference, case_type_id);


--
-- Name: case_field_case_type_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX case_field_case_type_id_idx ON public.case_field USING btree (case_type_id);


--
-- Name: case_field_field_type_id_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX case_field_field_type_id_id_idx ON public.case_field USING btree (field_type_id, id);


--
-- Name: case_field_field_type_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX case_field_field_type_id_idx ON public.case_field USING btree (field_type_id);


--
-- Name: case_type_jurisdiction_id_version_reference_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX case_type_jurisdiction_id_version_reference_idx ON public.case_type USING btree (jurisdiction_id, version, reference);


--
-- Name: case_type_reference_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX case_type_reference_idx ON public.case_type USING btree (reference);


--
-- Name: complex_field_complex_field_type_id_field_type_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX complex_field_complex_field_type_id_field_type_id_idx ON public.complex_field USING btree (complex_field_type_id, field_type_id);


--
-- Name: display_group_case_type_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX display_group_case_type_id_idx ON public.display_group USING btree (case_type_id);


--
-- Name: event_case_field_case_field_id_event_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX event_case_field_case_field_id_event_id_idx ON public.event_case_field USING btree (case_field_id, event_id);


--
-- Name: event_case_field_case_field_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX event_case_field_case_field_id_idx ON public.event_case_field USING btree (case_field_id);


--
-- Name: event_case_field_event_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX event_case_field_event_id_idx ON public.event_case_field USING btree (event_id);


--
-- Name: event_case_type_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX event_case_type_id_idx ON public.event USING btree (case_type_id);


--
-- Name: event_pre_state_state_id_event_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX event_pre_state_state_id_event_id_idx ON public.event_pre_state USING btree (event_id, state_id);


--
-- Name: field_type_id_base_field_type_id_collection_field_type_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX field_type_id_base_field_type_id_collection_field_type_id_idx ON public.field_type USING btree (id, base_field_type_id, collection_field_type_id);


--
-- Name: field_type_list_item_field_type_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX field_type_list_item_field_type_id_idx ON public.field_type_list_item USING btree (field_type_id);


--
-- Name: idx_case_field__id__field_type_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_case_field__id__field_type_id ON public.case_field USING btree (id, field_type_id);


--
-- Name: idx_case_type__id__jurisdiction_id__version__reference; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_case_type__id__jurisdiction_id__version__reference ON public.case_type USING btree (id, jurisdiction_id, version, reference);


--
-- Name: idx_display_group_case_field__case_field_id__display_group_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_display_group_case_field__case_field_id__display_group_id ON public.display_group_case_field USING btree (case_field_id, display_group_id);


--
-- Name: idx_event__case_type_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_event__case_type_id ON public.event USING btree (case_type_id);


--
-- Name: idx_event_pre_state__state_id__event_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_event_pre_state__state_id__event_id ON public.event_pre_state USING btree (state_id, event_id);


--
-- Name: idx_fied_type__base_filed_type_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_fied_type__base_filed_type_id ON public.field_type USING btree (base_field_type_id);


--
-- Name: idx_field_type__complex_field_type_id__field_type_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_field_type__complex_field_type_id__field_type_id ON public.complex_field USING btree (complex_field_type_id, field_type_id);


--
-- Name: idx_field_type__field_type_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_field_type__field_type_id ON public.complex_field USING btree (field_type_id);


--
-- Name: idx_field_type__id__base_field_type_id__collection_field_type_i; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_field_type__id__base_field_type_id__collection_field_type_i ON public.field_type USING btree (id, base_field_type_id, collection_field_type_id);


--
-- Name: idx_field_type_list_item__field_type_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_field_type_list_item__field_type_id ON public.field_type_list_item USING btree (field_type_id);


--
-- Name: idx_jurisdiction__id__reference; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_jurisdiction__id__reference ON public.jurisdiction USING btree (id, reference);


--
-- Name: idx_reference; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reference ON public.case_type USING btree (reference);


--
-- Name: idx_state__id__case_type_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_state__id__case_type_id ON public.state USING btree (id, case_type_id);


--
-- Name: idx_state_acl__role_id__state_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_state_acl__role_id__state_id ON public.state_acl USING btree (role_id, state_id);


--
-- Name: idx_version; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_version ON public.case_type USING btree (version);


--
-- Name: jurisdiction_id_reference_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX jurisdiction_id_reference_idx ON public.jurisdiction USING btree (id, reference);


--
-- Name: event_webhook event_webhook_event_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_webhook
    ADD CONSTRAINT event_webhook_event_id_fkey FOREIGN KEY (event_id) REFERENCES public.event(id);


--
-- Name: event_webhook event_webhook_webhook_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_webhook
    ADD CONSTRAINT event_webhook_webhook_id_fkey FOREIGN KEY (webhook_id) REFERENCES public.webhook(id);


--
-- Name: banner fk_banner_jurisdiction_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.banner
    ADD CONSTRAINT fk_banner_jurisdiction_id FOREIGN KEY (jurisdiction_id) REFERENCES public.jurisdiction(id);


--
-- Name: case_field_acl fk_case_field_acl_case_field_id_case_field_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.case_field_acl
    ADD CONSTRAINT fk_case_field_acl_case_field_id_case_field_id FOREIGN KEY (case_field_id) REFERENCES public.case_field(id);


--
-- Name: case_field_acl fk_case_field_acl_role_id_role_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.case_field_acl
    ADD CONSTRAINT fk_case_field_acl_role_id_role_id FOREIGN KEY (role_id) REFERENCES public.role(id);


--
-- Name: case_field fk_case_field_case_type_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.case_field
    ADD CONSTRAINT fk_case_field_case_type_id FOREIGN KEY (case_type_id) REFERENCES public.case_type(id);


--
-- Name: case_field fk_case_field_field_type_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.case_field
    ADD CONSTRAINT fk_case_field_field_type_id FOREIGN KEY (field_type_id) REFERENCES public.field_type(id);


--
-- Name: case_type_acl fk_case_type_acl_case_type_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.case_type_acl
    ADD CONSTRAINT fk_case_type_acl_case_type_id FOREIGN KEY (case_type_id) REFERENCES public.case_type(id);


--
-- Name: case_type_acl fk_case_type_acl_role_id_role_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.case_type_acl
    ADD CONSTRAINT fk_case_type_acl_role_id_role_id FOREIGN KEY (role_id) REFERENCES public.role(id);


--
-- Name: case_type fk_case_type_jurisdiction_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.case_type
    ADD CONSTRAINT fk_case_type_jurisdiction_id FOREIGN KEY (jurisdiction_id) REFERENCES public.jurisdiction(id);


--
-- Name: case_type fk_case_type_print_webhook_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.case_type
    ADD CONSTRAINT fk_case_type_print_webhook_id FOREIGN KEY (print_webhook_id) REFERENCES public.webhook(id);


--
-- Name: challenge_question fk_challenge_question_case_type_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.challenge_question
    ADD CONSTRAINT fk_challenge_question_case_type_id FOREIGN KEY (case_type_id) REFERENCES public.case_type(id);


--
-- Name: challenge_question fk_challenge_question_field_type_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.challenge_question
    ADD CONSTRAINT fk_challenge_question_field_type_id FOREIGN KEY (answer_field_type) REFERENCES public.field_type(id);


--
-- Name: complex_field_acl fk_complex_field_acl_case_field_id_case_field_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.complex_field_acl
    ADD CONSTRAINT fk_complex_field_acl_case_field_id_case_field_id FOREIGN KEY (case_field_id) REFERENCES public.case_field(id);


--
-- Name: complex_field_acl fk_complex_field_acl_role_id_role_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.complex_field_acl
    ADD CONSTRAINT fk_complex_field_acl_role_id_role_id FOREIGN KEY (role_id) REFERENCES public.role(id);


--
-- Name: complex_field fk_complex_field_complex_field_type_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.complex_field
    ADD CONSTRAINT fk_complex_field_complex_field_type_id FOREIGN KEY (complex_field_type_id) REFERENCES public.field_type(id);


--
-- Name: complex_field fk_complex_field_field_type_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.complex_field
    ADD CONSTRAINT fk_complex_field_field_type_id FOREIGN KEY (field_type_id) REFERENCES public.field_type(id);


--
-- Name: definition_designer fk_definition_designer_jurisdiction_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.definition_designer
    ADD CONSTRAINT fk_definition_designer_jurisdiction_id FOREIGN KEY (jurisdiction_id) REFERENCES public.jurisdiction(id);


--
-- Name: display_group_case_field fk_display_group_case_field_case_field_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.display_group_case_field
    ADD CONSTRAINT fk_display_group_case_field_case_field_id FOREIGN KEY (case_field_id) REFERENCES public.case_field(id);


--
-- Name: display_group_case_field fk_display_group_case_field_display_group_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.display_group_case_field
    ADD CONSTRAINT fk_display_group_case_field_display_group_id FOREIGN KEY (display_group_id) REFERENCES public.display_group(id);


--
-- Name: display_group fk_display_group_case_type_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.display_group
    ADD CONSTRAINT fk_display_group_case_type_id FOREIGN KEY (case_type_id) REFERENCES public.case_type(id);


--
-- Name: display_group fk_display_group_event_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.display_group
    ADD CONSTRAINT fk_display_group_event_id FOREIGN KEY (event_id) REFERENCES public.event(id);


--
-- Name: display_group fk_display_group_role_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.display_group
    ADD CONSTRAINT fk_display_group_role_id FOREIGN KEY (role_id) REFERENCES public.role(id);


--
-- Name: search_input_case_field fk_display_group_role_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_input_case_field
    ADD CONSTRAINT fk_display_group_role_id FOREIGN KEY (role_id) REFERENCES public.role(id);


--
-- Name: search_result_case_field fk_display_group_role_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_result_case_field
    ADD CONSTRAINT fk_display_group_role_id FOREIGN KEY (role_id) REFERENCES public.role(id);


--
-- Name: workbasket_input_case_field fk_display_group_role_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.workbasket_input_case_field
    ADD CONSTRAINT fk_display_group_role_id FOREIGN KEY (role_id) REFERENCES public.role(id);


--
-- Name: workbasket_case_field fk_display_group_role_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.workbasket_case_field
    ADD CONSTRAINT fk_display_group_role_id FOREIGN KEY (role_id) REFERENCES public.role(id);


--
-- Name: display_group fk_display_group_webhook_mid_event_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.display_group
    ADD CONSTRAINT fk_display_group_webhook_mid_event_id FOREIGN KEY (webhook_mid_event_id) REFERENCES public.webhook(id);


--
-- Name: event_acl fk_event_acl_event_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_acl
    ADD CONSTRAINT fk_event_acl_event_id FOREIGN KEY (event_id) REFERENCES public.event(id);


--
-- Name: event_acl fk_event_acl_role_id_role_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_acl
    ADD CONSTRAINT fk_event_acl_role_id_role_id FOREIGN KEY (role_id) REFERENCES public.role(id);


--
-- Name: event_case_field fk_event_case_field_case_field_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_case_field
    ADD CONSTRAINT fk_event_case_field_case_field_id FOREIGN KEY (case_field_id) REFERENCES public.case_field(id);


--
-- Name: event_case_field_complex_type fk_event_case_field_complex_type_event_case_field_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_case_field_complex_type
    ADD CONSTRAINT fk_event_case_field_complex_type_event_case_field_id FOREIGN KEY (event_case_field_id) REFERENCES public.event_case_field(id);


--
-- Name: event_case_field fk_event_case_field_event_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_case_field
    ADD CONSTRAINT fk_event_case_field_event_id FOREIGN KEY (event_id) REFERENCES public.event(id);


--
-- Name: event fk_event_case_type_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT fk_event_case_type_id FOREIGN KEY (case_type_id) REFERENCES public.case_type(id);


--
-- Name: event_post_state fk_event_post_state_case_event_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_post_state
    ADD CONSTRAINT fk_event_post_state_case_event_id FOREIGN KEY (case_event_id) REFERENCES public.event(id);


--
-- Name: event_pre_state fk_event_pre_state_event_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_pre_state
    ADD CONSTRAINT fk_event_pre_state_event_id FOREIGN KEY (event_id) REFERENCES public.event(id);


--
-- Name: event_pre_state fk_event_pre_state_state_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event_pre_state
    ADD CONSTRAINT fk_event_pre_state_state_id FOREIGN KEY (state_id) REFERENCES public.state(id);


--
-- Name: field_type fk_field_type_base_field_type_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.field_type
    ADD CONSTRAINT fk_field_type_base_field_type_id FOREIGN KEY (base_field_type_id) REFERENCES public.field_type(id);


--
-- Name: field_type fk_field_type_collection_field_type_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.field_type
    ADD CONSTRAINT fk_field_type_collection_field_type_id FOREIGN KEY (collection_field_type_id) REFERENCES public.field_type(id);


--
-- Name: field_type fk_field_type_jurisdiction_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.field_type
    ADD CONSTRAINT fk_field_type_jurisdiction_id FOREIGN KEY (jurisdiction_id) REFERENCES public.jurisdiction(id);


--
-- Name: field_type_list_item fk_field_type_list_item_field_type_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.field_type_list_item
    ADD CONSTRAINT fk_field_type_list_item_field_type_id FOREIGN KEY (field_type_id) REFERENCES public.field_type(id);


--
-- Name: jurisdiction_ui_config fk_jurisdiction_ui_config_jurisdiction_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.jurisdiction_ui_config
    ADD CONSTRAINT fk_jurisdiction_ui_config_jurisdiction_id FOREIGN KEY (jurisdiction_id) REFERENCES public.jurisdiction(id);


--
-- Name: role fk_role_case_type_id_case_type_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT fk_role_case_type_id_case_type_id FOREIGN KEY (case_type_id) REFERENCES public.case_type(id);


--
-- Name: search_alias_field fk_search_alias_field_case_type_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_alias_field
    ADD CONSTRAINT fk_search_alias_field_case_type_id FOREIGN KEY (case_type_id) REFERENCES public.case_type(id);


--
-- Name: search_alias_field fk_search_alias_field_field_type_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_alias_field
    ADD CONSTRAINT fk_search_alias_field_field_type_id FOREIGN KEY (field_type_id) REFERENCES public.field_type(id);


--
-- Name: search_cases_result_fields fk_search_cases_result_fields_case_field_id_case_field_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_cases_result_fields
    ADD CONSTRAINT fk_search_cases_result_fields_case_field_id_case_field_id FOREIGN KEY (case_field_id) REFERENCES public.case_field(id);


--
-- Name: search_cases_result_fields fk_search_cases_result_fields_case_type_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_cases_result_fields
    ADD CONSTRAINT fk_search_cases_result_fields_case_type_id FOREIGN KEY (case_type_id) REFERENCES public.case_type(id);


--
-- Name: search_cases_result_fields fk_search_cases_result_fields_role_id_role_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_cases_result_fields
    ADD CONSTRAINT fk_search_cases_result_fields_role_id_role_id FOREIGN KEY (role_id) REFERENCES public.role(id);


--
-- Name: search_input_case_field fk_search_input_case_field_case_field_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_input_case_field
    ADD CONSTRAINT fk_search_input_case_field_case_field_id FOREIGN KEY (case_field_id) REFERENCES public.case_field(id);


--
-- Name: search_input_case_field fk_search_input_case_field_case_type_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_input_case_field
    ADD CONSTRAINT fk_search_input_case_field_case_type_id FOREIGN KEY (case_type_id) REFERENCES public.case_type(id);


--
-- Name: search_result_case_field fk_search_result_case_field_case_field_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_result_case_field
    ADD CONSTRAINT fk_search_result_case_field_case_field_id FOREIGN KEY (case_field_id) REFERENCES public.case_field(id);


--
-- Name: search_result_case_field fk_search_result_case_field_case_type_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_result_case_field
    ADD CONSTRAINT fk_search_result_case_field_case_type_id FOREIGN KEY (case_type_id) REFERENCES public.case_type(id);


--
-- Name: state_acl fk_state_acl_role_id_role_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.state_acl
    ADD CONSTRAINT fk_state_acl_role_id_role_id FOREIGN KEY (role_id) REFERENCES public.role(id);


--
-- Name: state_acl fk_state_acl_state_id_state_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.state_acl
    ADD CONSTRAINT fk_state_acl_state_id_state_id FOREIGN KEY (state_id) REFERENCES public.state(id);


--
-- Name: state fk_state_case_type_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.state
    ADD CONSTRAINT fk_state_case_type_id FOREIGN KEY (case_type_id) REFERENCES public.case_type(id);


--
-- Name: workbasket_case_field fk_workbasket_case_field_case_field_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.workbasket_case_field
    ADD CONSTRAINT fk_workbasket_case_field_case_field_id FOREIGN KEY (case_field_id) REFERENCES public.case_field(id);


--
-- Name: workbasket_case_field fk_workbasket_case_field_case_type_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.workbasket_case_field
    ADD CONSTRAINT fk_workbasket_case_field_case_type_id FOREIGN KEY (case_type_id) REFERENCES public.case_type(id);


--
-- Name: workbasket_input_case_field fk_workbasket_input_case_field_case_field_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.workbasket_input_case_field
    ADD CONSTRAINT fk_workbasket_input_case_field_case_field_id FOREIGN KEY (case_field_id) REFERENCES public.case_field(id);


--
-- Name: workbasket_input_case_field fk_workbasket_input_case_field_case_type_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.workbasket_input_case_field
    ADD CONSTRAINT fk_workbasket_input_case_field_case_type_id FOREIGN KEY (case_type_id) REFERENCES public.case_type(id);


--
-- PostgreSQL database schema only dump complete
--

--
-- Reference data
--

-- Populate base types --

INSERT INTO public.field_type (created_at, reference, version)
VALUES (now(), 'Text', '1');

INSERT INTO public.field_type (created_at, reference, version)
VALUES (now(), 'Number', '1');

INSERT INTO public.field_type (created_at, reference, version)
VALUES (now(), 'Email', '1');

INSERT INTO public.field_type (created_at, reference, version)
VALUES (now(), 'YesOrNo', '1');

INSERT INTO public.field_type (created_at, reference, version)
VALUES (now(), 'Date', '1');

INSERT INTO public.field_type (created_at, reference, version)
VALUES (now(), 'FixedList', '1');

INSERT INTO public.field_type (created_at, reference, version, regular_expression)
VALUES (now(), 'Postcode', '1',
        '^([A-PR-UWYZ0-9][A-HK-Y0-9][AEHMNPRTVXY0-9]?[ABEHMNPRVWXY0-9]? {1,2}[0-9][ABD-HJLN-UW-Z]{2}|GIR 0AA)$');

INSERT INTO public.field_type (created_at, reference, version)
VALUES (now(), 'MoneyGBP', '1');

INSERT INTO public.field_type (created_at, reference, version, regular_expression)
VALUES (now(), 'PhoneUK', '1',
        '^(((\+44\s?\d{4}|\(?0\d{4}\)?)\s?\d{3}\s?\d{3})|((\+44\s?\d{3}|\(?0\d{3}\)?)\s?\d{3}\s?\d{4})|((\+44\s?\d{2}|\(?0\d{2}\)?)\s?\d{4}\s?\d{4}))(\s?\#(\d{4}|\d{3}))?$');

INSERT INTO public.field_type (created_at, reference, version)
VALUES (now(), 'TextArea', '1');

INSERT INTO public.field_type (created_at, reference, version)
VALUES (now(), 'Complex', '1');

INSERT INTO public.field_type (created_at, reference, version)
VALUES (now(), 'Collection', '1');

INSERT INTO public.field_type (created_at, reference, version)
VALUES (now(), 'MultiSelectList', '1');

INSERT INTO public.field_type (created_at, reference, version)
VALUES (now(), 'Document', '1');

INSERT INTO public.field_type (created_at, reference, version)
VALUES (now(), 'Label', '1');

INSERT INTO public.field_type (created_at, reference, version, base_field_type_id)
VALUES (now(), 'AddressGlobal', '1', (select id
                                      from field_type
                                      where reference = 'Complex'
                                        and jurisdiction_id is null
                                        and version = (select max(version)
                                                       from field_type
                                                       where reference = 'Complex'
                                                         and jurisdiction_id is null
                                                         and base_field_type_id is null)));

INSERT INTO public.field_type (created_at, reference, version, maximum, base_field_type_id)
VALUES (now(), 'TextMax50', '1', '50', (select id
                                        from field_type
                                        where reference = 'Text'
                                          and jurisdiction_id is null
                                          and version = (select max(version)
                                                         from field_type
                                                         where reference = 'Text'
                                                           and jurisdiction_id is null
                                                           and base_field_type_id is null)));

INSERT INTO public.field_type (created_at, reference, version, maximum, base_field_type_id)
VALUES (now(), 'TextMax150', '1', '150', (select id
                                          from field_type
                                          where reference = 'Text'
                                            and jurisdiction_id is null
                                            and version = (select max(version)
                                                           from field_type
                                                           where reference = 'Text'
                                                             and jurisdiction_id is null
                                                             and base_field_type_id is null)));

INSERT INTO public.field_type (created_at, reference, version, maximum, base_field_type_id)
VALUES (now(), 'TextMax14', '1', '14', (select id
                                        from field_type
                                        where reference = 'Text'
                                          and jurisdiction_id is null
                                          and version = (select max(version)
                                                         from field_type
                                                         where reference = 'Text'
                                                           and jurisdiction_id is null
                                                           and base_field_type_id is null)));

INSERT INTO public.field_type (created_at, reference, version, base_field_type_id)
VALUES (now(), 'AddressGlobalUK', '1', (select id
                                        from field_type
                                        where reference = 'Complex'
                                          and jurisdiction_id is null
                                          and version = (select max(version)
                                                         from field_type
                                                         where reference = 'Complex'
                                                           and jurisdiction_id is null
                                                           and base_field_type_id is null)));

INSERT INTO public.field_type (created_at, reference, version, base_field_type_id)
VALUES (now(), 'AddressUK', '1', (select id
                                  from field_type
                                  where reference = 'Complex'
                                    and jurisdiction_id is null
                                    and version = (select max(version)
                                                   from field_type
                                                   where reference = 'Complex'
                                                     and jurisdiction_id is null
                                                     and base_field_type_id is null)));

INSERT INTO public.field_type (created_at, reference, version)
VALUES (now(), 'DateTime', '1');

INSERT INTO public.field_type (created_at, reference, base_field_type_id, version)
VALUES (now(), 'OrderSummary', (select id
                                from field_type
                                where reference = 'Complex'
                                  and jurisdiction_id is null
                                  and version = (select max(version)
                                                 from field_type
                                                 where reference = 'Complex'
                                                   and jurisdiction_id is null
                                                   and base_field_type_id is null)), '1');

INSERT INTO public.field_type (created_at, reference, base_field_type_id, version)
VALUES (now(), 'Fee', (select id
                       from field_type
                       where reference = 'Complex'
                         and jurisdiction_id is null
                         and version = (select max(version)
                                        from field_type
                                        where reference = 'Complex'
                                          and jurisdiction_id is null
                                          and base_field_type_id is null)), '1');

INSERT INTO public.field_type (created_at, reference, base_field_type_id, collection_field_type_id, version)
VALUES (now(), 'FeesList',
        (select id from field_type where reference = 'Collection' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'Fee' and version = 1 and jurisdiction_id is null), '1');

INSERT INTO public.field_type (created_at, reference, version)
VALUES (now(), 'CasePaymentHistoryViewer', '1');

INSERT INTO public.field_type (created_at, reference, version)
VALUES (now(), 'FixedRadioList', '1');

INSERT INTO public.field_type (created_at, reference, version, base_field_type_id)
VALUES (now(), 'CaseLink', '1', (select id
                                 from field_type
                                 where reference = 'Complex'
                                   and jurisdiction_id is null
                                   and version = (select max(version)
                                                  from field_type
                                                  where reference = 'Complex'
                                                    and jurisdiction_id is null
                                                    and base_field_type_id is null)));

INSERT INTO public.field_type (created_at, reference, version, base_field_type_id, regular_expression)
VALUES (now(), 'TextCaseReference', '1', (select id
                                          from field_type
                                          where reference = 'Text'
                                            and jurisdiction_id is null
                                            and version = (select max(version)
                                                           from field_type
                                                           where reference = 'Text'
                                                             and jurisdiction_id is null
                                                             and base_field_type_id is null)),
        '(?:^[0-9]{16}$|^\d{4}-\d{4}-\d{4}-\d{4}$)');

INSERT INTO public.field_type (created_at, reference, version)
VALUES (now(), 'CaseHistoryViewer', '1');

INSERT INTO public.field_type (created_at, reference, version)
VALUES (now(), 'DynamicList', '1');

INSERT INTO public.field_type (created_at, reference, version, base_field_type_id)
VALUES (now(), 'Organisation', '1', (select id
                                     from field_type
                                     where reference = 'Complex'
                                       and jurisdiction_id is null
                                       and version = (select max(version)
                                                      from field_type
                                                      where reference = 'Complex'
                                                        and jurisdiction_id is null
                                                        and base_field_type_id is null)));

INSERT INTO public.field_type (created_at, reference, version, base_field_type_id)
VALUES (now(), 'OrganisationPolicy', '1', (select id
                                           from field_type
                                           where reference = 'Complex'
                                             and jurisdiction_id is null
                                             and version = (select max(version)
                                                            from field_type
                                                            where reference = 'Complex'
                                                              and jurisdiction_id is null
                                                              and base_field_type_id is null)));

INSERT INTO public.field_type (created_at, reference, version, base_field_type_id)
VALUES (now(), 'ChangeOrganisationRequest', '1', (select id
                                                  from field_type
                                                  where reference = 'Complex'
                                                    and jurisdiction_id is null
                                                    and version = (select max(version)
                                                                   from field_type
                                                                   where reference = 'Complex'
                                                                     and jurisdiction_id is null
                                                                     and base_field_type_id is null)));

-- Populate complex types --

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('AddressLine1', 'Building and Street', 'PUBLIC',
        (select id from field_type where reference = 'TextMax150' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressGlobal' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('AddressLine2', 'Address Line 2', 'PUBLIC',
        (select id from field_type where reference = 'TextMax50' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressGlobal' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('AddressLine3', 'Address Line 3', 'PUBLIC',
        (select id from field_type where reference = 'TextMax50' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressGlobal' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('PostTown', 'Town or City', 'PUBLIC',
        (select id from field_type where reference = 'TextMax50' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressGlobal' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('County', 'County/State', 'PUBLIC',
        (select id from field_type where reference = 'TextMax50' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressGlobal' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('Country', 'Country', 'PUBLIC',
        (select id from field_type where reference = 'TextMax50' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressGlobal' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('PostCode', 'Postcode/Zipcode', 'PUBLIC',
        (select id from field_type where reference = 'TextMax14' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressGlobal' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('AddressLine1', 'Building and Street', 'PUBLIC',
        (select id from field_type where reference = 'TextMax150' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressGlobalUK' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('AddressLine2', 'Address Line 2', 'PUBLIC',
        (select id from field_type where reference = 'TextMax50' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressGlobalUK' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('AddressLine3', 'Address Line 3', 'PUBLIC',
        (select id from field_type where reference = 'TextMax50' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressGlobalUK' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('PostTown', 'Town or City', 'PUBLIC',
        (select id from field_type where reference = 'TextMax50' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressGlobalUK' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('County', 'County/State', 'PUBLIC',
        (select id from field_type where reference = 'TextMax50' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressGlobalUK' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('Country', 'Country', 'PUBLIC',
        (select id from field_type where reference = 'TextMax50' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressGlobalUK' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('PostCode', 'Postcode/Zipcode', 'PUBLIC',
        (select id from field_type where reference = 'TextMax14' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressGlobalUK' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('AddressLine1', 'Building and Street', 'PUBLIC',
        (select id from field_type where reference = 'TextMax150' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressUK' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('AddressLine2', 'Address Line 2', 'PUBLIC',
        (select id from field_type where reference = 'TextMax50' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressUK' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('AddressLine3', 'Address Line 3', 'PUBLIC',
        (select id from field_type where reference = 'TextMax50' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressUK' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('PostTown', 'Town or City', 'PUBLIC',
        (select id from field_type where reference = 'TextMax50' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressUK' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('County', 'County', 'PUBLIC',
        (select id from field_type where reference = 'TextMax50' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressUK' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('PostCode', 'Postcode/Zipcode', 'PUBLIC',
        (select id from field_type where reference = 'TextMax14' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressUK' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('Country', 'Country', 'PUBLIC',
        (select id from field_type where reference = 'TextMax50' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'AddressUK' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('PaymentReference', 'Payment Reference', 'PUBLIC',
        (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'OrderSummary' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('PaymentTotal', 'Total', 'PUBLIC',
        (select id from field_type where reference = 'MoneyGBP' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'OrderSummary' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('FeeCode', 'Fee Code', 'PUBLIC',
        (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'Fee' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('FeeDescription', 'Fee Description', 'PUBLIC',
        (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'Fee' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('FeeAmount', 'Fee Amount', 'PUBLIC',
        (select id from field_type where reference = 'MoneyGBP' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'Fee' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('FeeVersion', 'Fee Version', 'PUBLIC',
        (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'Fee' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('Fees', 'Fees', 'PUBLIC',
        (select id from field_type where reference = 'FeesList' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'OrderSummary' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('CaseReference', 'Case Reference', 'PUBLIC',
        (select id from field_type where reference = 'TextCaseReference' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'CaseLink' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('OrganisationID', 'Organisation ID', 'PUBLIC',
        (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'Organisation' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('OrganisationName', 'Name', 'PUBLIC',
        (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'Organisation' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('Organisation', 'Organisation', 'PUBLIC',
        (select id from field_type where reference = 'Organisation' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'OrganisationPolicy' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('OrgPolicyCaseAssignedRole', 'Case Assigned Role', 'PUBLIC',
        (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'OrganisationPolicy' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('OrgPolicyReference', 'Reference', 'PUBLIC',
        (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null),
        (select id from field_type where reference = 'OrganisationPolicy' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('OrganisationToAdd', 'Organisation To Add', 'PUBLIC',
        (select id from field_type where reference = 'Organisation' and version = 1 and jurisdiction_id is null),
        (select id
         from field_type
         where reference = 'ChangeOrganisationRequest' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('OrganisationToRemove', 'Organisation To Remove', 'PUBLIC',
        (select id from field_type where reference = 'Organisation' and version = 1 and jurisdiction_id is null),
        (select id
         from field_type
         where reference = 'ChangeOrganisationRequest' and version = 1 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('CaseRoleId', 'Case Role Id', 'PUBLIC',
        (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null), (select id
                                                                                                           from field_type
                                                                                                           where reference = 'ChangeOrganisationRequest'
                                                                                                             and version = 1
                                                                                                             and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('Reason', 'Reason', 'PUBLIC',
        (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null), (select id
                                                                                                           from field_type
                                                                                                           where reference = 'ChangeOrganisationRequest'
                                                                                                             and version = 1
                                                                                                             and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('RequestTimestamp', 'Request Timestamp', 'PUBLIC',
        (select id from field_type where reference = 'DateTime' and version = 1 and jurisdiction_id is null), (select id
                                                                                                               from field_type
                                                                                                               where reference = 'ChangeOrganisationRequest'
                                                                                                                 and version = 1
                                                                                                                 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('ApprovalStatus', 'Approval Status', 'PUBLIC',
        (select id from field_type where reference = 'Number' and version = 1 and jurisdiction_id is null), (select id
                                                                                                             from field_type
                                                                                                             where reference = 'ChangeOrganisationRequest'
                                                                                                               and version = 1
                                                                                                               and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('ApprovalRejectionTimestamp', 'Approval Rejection Timestamp', 'PUBLIC',
        (select id from field_type where reference = 'DateTime' and version = 1 and jurisdiction_id is null), (select id
                                                                                                               from field_type
                                                                                                               where reference = 'ChangeOrganisationRequest'
                                                                                                                 and version = 1
                                                                                                                 and jurisdiction_id is null));

INSERT INTO public.complex_field (reference, label, security_classification, field_type_id, complex_field_type_id)
VALUES ('NotesReason', 'Notes Reason', 'PUBLIC',
        (select id from field_type where reference = 'Text' and version = 1 and jurisdiction_id is null), (select id
                                                                                                           from field_type
                                                                                                           where reference = 'ChangeOrganisationRequest'
                                                                                                             and version = 1
                                                                                                             and jurisdiction_id is null));

-- Populate meta case fields types --

INSERT INTO public.case_field (reference, live_from, label, hidden, security_classification, field_type_id,
                               data_field_type)
VALUES ('[JURISDICTION]', now(), 'Jurisdiction', 'false', 'PUBLIC',
        (select id from field_type where reference = 'Text' and jurisdiction_id is null order by version limit 1),
        'METADATA');

INSERT INTO public.case_field (reference, live_from, label, hidden, security_classification, field_type_id,
                               data_field_type)
VALUES ('[CASE_TYPE]', now(), 'Case Type', 'false', 'PUBLIC',
        (select id from field_type where reference = 'Text' and jurisdiction_id is null order by version limit 1),
        'METADATA');

INSERT INTO public.case_field (reference, live_from, label, hidden, security_classification, field_type_id,
                               data_field_type)
VALUES ('[SECURITY_CLASSIFICATION]', now(), 'Security Classification', 'false', 'PUBLIC',
        (select id from field_type where reference = 'Text' and jurisdiction_id is null order by version limit 1),
        'METADATA');

INSERT INTO public.case_field (reference, live_from, label, hidden, security_classification, field_type_id,
                               data_field_type)
VALUES ('[CASE_REFERENCE]', now(), 'Case Reference', 'false', 'PUBLIC',
        (select id from field_type where reference = 'Number' and jurisdiction_id is null order by version limit 1),
        'METADATA');

INSERT INTO public.case_field (reference, live_from, label, hidden, security_classification, field_type_id,
                               data_field_type)
VALUES ('[CREATED_DATE]', now(), 'Created Date', 'false', 'PUBLIC',
        (select id from field_type where reference = 'DateTime' and jurisdiction_id is null order by version limit 1),
        'METADATA');

INSERT INTO public.case_field (reference, live_from, label, hidden, security_classification, field_type_id,
                               data_field_type)
VALUES ('[LAST_MODIFIED_DATE]', now(), 'Last Modified Date', 'false', 'PUBLIC',
        (select id from field_type where reference = 'DateTime' and jurisdiction_id is null order by version limit 1),
        'METADATA');

INSERT INTO public.case_field (reference, live_from, label, hidden, security_classification, field_type_id,
                               data_field_type)
VALUES ('[LAST_STATE_MODIFIED_DATE]', now(), 'Last State Modified Date', 'false', 'PUBLIC',
        (select id from field_type where reference = 'DateTime' and jurisdiction_id is null order by version limit 1),
        'METADATA');

