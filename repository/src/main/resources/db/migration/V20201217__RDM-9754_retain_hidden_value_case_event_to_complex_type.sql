--
-- Author Rebecca Baker
-- RDM-9754
-- add retain hidden value to case event to complex type tab
--

ALTER TABLE ONLY public.event_case_field_complex_type
ADD retain_hidden_value Boolean NULL;
