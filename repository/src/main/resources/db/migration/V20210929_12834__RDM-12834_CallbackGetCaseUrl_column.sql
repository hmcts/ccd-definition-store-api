ALTER TABLE case_type ADD COLUMN IF NOT EXISTS get_case_webhook_id INTEGER;

ALTER TABLE case_type ADD CONSTRAINT fk_case_type_get_case_webhook_id FOREIGN KEY (get_case_webhook_id) REFERENCES public.webhook(id);
