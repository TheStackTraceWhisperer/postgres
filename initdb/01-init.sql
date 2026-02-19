-- Runs on first container start when the data directory is empty.

CREATE TABLE IF NOT EXISTS public.widgets (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO public.widgets (name) VALUES
  ('alpha'),
  ('beta'),
  ('gamma');

