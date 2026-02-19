-- Test database initialization script for Testcontainers
-- This creates the same schema as the production init script

CREATE TABLE IF NOT EXISTS public.widgets (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  quantity INTEGER NOT NULL,
  price NUMERIC(10, 2) NOT NULL
);

-- Shadow audit table for widgets
CREATE TABLE IF NOT EXISTS public.widgets_audit (
  audit_id BIGSERIAL PRIMARY KEY,
  operation VARCHAR(10) NOT NULL,  -- INSERT, UPDATE, DELETE
  widget_id BIGINT,
  name TEXT,
  created_at TIMESTAMPTZ,
  quantity INTEGER,
  price NUMERIC(10, 2),
  changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  changed_by VARCHAR(100) DEFAULT current_user
);

-- Create index on widget_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_widgets_audit_widget_id ON public.widgets_audit(widget_id);
CREATE INDEX IF NOT EXISTS idx_widgets_audit_changed_at ON public.widgets_audit(changed_at);

-- Trigger function to capture changes
CREATE OR REPLACE FUNCTION public.audit_widgets_changes()
RETURNS TRIGGER AS $$
BEGIN
  IF (TG_OP = 'DELETE') THEN
    INSERT INTO public.widgets_audit (
      operation,
      widget_id,
      name,
      created_at,
      quantity,
      price
    ) VALUES (
      'DELETE',
      OLD.id,
      OLD.name,
      OLD.created_at,
      OLD.quantity,
      OLD.price
    );
    RETURN OLD;
  ELSIF (TG_OP = 'UPDATE') THEN
    INSERT INTO public.widgets_audit (
      operation,
      widget_id,
      name,
      created_at,
      quantity,
      price
    ) VALUES (
      'UPDATE',
      NEW.id,
      NEW.name,
      NEW.created_at,
      NEW.quantity,
      NEW.price
    );
    RETURN NEW;
  ELSIF (TG_OP = 'INSERT') THEN
    INSERT INTO public.widgets_audit (
      operation,
      widget_id,
      name,
      created_at,
      quantity,
      price
    ) VALUES (
      'INSERT',
      NEW.id,
      NEW.name,
      NEW.created_at,
      NEW.quantity,
      NEW.price
    );
    RETURN NEW;
  END IF;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to fire AFTER each row operation
CREATE TRIGGER widgets_audit_trigger
  AFTER INSERT OR UPDATE OR DELETE ON public.widgets
  FOR EACH ROW
  EXECUTE FUNCTION public.audit_widgets_changes();

