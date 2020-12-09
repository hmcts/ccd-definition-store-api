ALTER TABLE ONLY public.challenge_question
 ADD CONSTRAINT unique_id_case_type_id_display_order_question_id UNIQUE (id, case_type_id, display_order, question_id);
