ALTER TABLE public.display_group
    ADD COLUMN IF NOT EXISTS default_focus BOOLEAN;
