ALTER TABLE public.challenge_question ADD COLUMN IF NOT EXISTS ignore_null_fields BOOLEAN DEFAULT FALSE;
ALTER TABLE public.challenge_question ALTER COLUMN answer_field DROP NOT NULL;
