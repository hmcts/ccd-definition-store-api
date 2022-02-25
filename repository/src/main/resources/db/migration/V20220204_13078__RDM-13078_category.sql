

CREATE SEQUENCE public.category_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


CREATE TABLE public.category (
                                                 id integer NOT NULL DEFAULT nextval('public.category_id_seq'::regclass),
                                                 category_id character varying(70) NOT NULL,
                                                 category_label character varying(70) NOT NULL,
                                                 parent_category_id character varying(70),
                                                 live_from date,
                                                 live_to date,
                                                 display_order integer NOT NULL,
                                                 case_type_id integer NOT NULL
);


ALTER SEQUENCE public.category_id_seq OWNED BY public.category.id;

ALTER TABLE ONLY public.category
ADD CONSTRAINT fk_category_case_type_id FOREIGN KEY (case_type_id) REFERENCES public.case_type(id);
