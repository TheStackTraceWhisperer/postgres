# Shadow Audit Table Implementation

## Overview

The schema has been enhanced with a **shadow audit table** (`widgets_audit`) and a **database trigger** that automatically captures all changes (INSERT, UPDATE, DELETE) to the `widgets` table.

---

## Schema Components

### 1. Main Table: `widgets`
```sql
CREATE TABLE public.widgets (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  quantity INTEGER NOT NULL,
  price NUMERIC(10, 2) NOT NULL
);
```

### 2. Shadow Audit Table: `widgets_audit`
```sql
CREATE TABLE public.widgets_audit (
  audit_id BIGSERIAL PRIMARY KEY,
  operation VARCHAR(10) NOT NULL,      -- INSERT, UPDATE, DELETE
  widget_id BIGINT,                    -- ID from widgets table
  name TEXT,
  created_at TIMESTAMPTZ,
  quantity INTEGER,
  price NUMERIC(10, 2),
  changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),  -- When the change occurred
  changed_by VARCHAR(100) DEFAULT current_user     -- Who made the change
);
```

**Key Features:**
- **audit_id**: Unique identifier for each audit record
- **operation**: Type of operation (INSERT/UPDATE/DELETE)
- **widget_id**: References the ID from the widgets table
- **Snapshot fields**: Copies of all widget columns at time of change
- **changed_at**: Timestamp of when the change occurred
- **changed_by**: Database user who made the change

### 3. Performance Indexes
```sql
CREATE INDEX idx_widgets_audit_widget_id ON public.widgets_audit(widget_id);
CREATE INDEX idx_widgets_audit_changed_at ON public.widgets_audit(changed_at);
```

These indexes enable fast queries like:
- "Show me all changes for widget ID 5"
- "Show me all changes in the last 24 hours"

---

## Trigger Implementation

### Trigger Function: `audit_widgets_changes()`

```sql
CREATE OR REPLACE FUNCTION public.audit_widgets_changes()
RETURNS TRIGGER AS $$
BEGIN
  IF (TG_OP = 'DELETE') THEN
    -- Capture the OLD values before deletion
    INSERT INTO public.widgets_audit (operation, widget_id, name, created_at, quantity, price)
    VALUES ('DELETE', OLD.id, OLD.name, OLD.created_at, OLD.quantity, OLD.price);
    RETURN OLD;
    
  ELSIF (TG_OP = 'UPDATE') THEN
    -- Capture the NEW values after update
    INSERT INTO public.widgets_audit (operation, widget_id, name, created_at, quantity, price)
    VALUES ('UPDATE', NEW.id, NEW.name, NEW.created_at, NEW.quantity, NEW.price);
    RETURN NEW;
    
  ELSIF (TG_OP = 'INSERT') THEN
    -- Capture the NEW values being inserted
    INSERT INTO public.widgets_audit (operation, widget_id, name, created_at, quantity, price)
    VALUES ('INSERT', NEW.id, NEW.name, NEW.created_at, NEW.quantity, NEW.price);
    RETURN NEW;
  END IF;
  
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;
```

### Trigger: `widgets_audit_trigger`

```sql
CREATE TRIGGER widgets_audit_trigger
  AFTER INSERT OR UPDATE OR DELETE ON public.widgets
  FOR EACH ROW
  EXECUTE FUNCTION public.audit_widgets_changes();
```

**How it works:**
- Fires **AFTER** each INSERT, UPDATE, or DELETE operation
- Executes **FOR EACH ROW** (captures individual records, not batch operations)
- Automatically inserts a record into `widgets_audit` with the operation details

---

## Behavior Examples

### Example 1: INSERT
```sql
INSERT INTO widgets (name, quantity, price) VALUES ('delta', 15, 49.99);
```

**Result in widgets_audit:**
```
audit_id | operation | widget_id | name  | quantity | price | changed_at          
---------|-----------|-----------|-------|----------|-------|--------------------
4        | INSERT    | 4         | delta | 15       | 49.99 | 2026-02-19 23:30:00
```

### Example 2: UPDATE
```sql
UPDATE widgets SET quantity = 100, price = 99.99 WHERE name = 'alpha';
```

**Result in widgets_audit:**
```
audit_id | operation | widget_id | name  | quantity | price | changed_at          
---------|-----------|-----------|-------|----------|-------|--------------------
5        | UPDATE    | 1         | alpha | 100      | 99.99 | 2026-02-19 23:31:00
```

### Example 3: DELETE
```sql
DELETE FROM widgets WHERE name = 'beta';
```

**Result in widgets_audit:**
```
audit_id | operation | widget_id | name | quantity | price | changed_at          
---------|-----------|-----------|------|----------|-------|--------------------
6        | DELETE    | 2         | beta | 25       | 29.99 | 2026-02-19 23:32:00
```

---

## Testing the Audit Functionality

### 1. Reset the database with new schema
```bash
cd /home/samuel/projects/postgres
docker compose down -v
docker compose up -d
sleep 5
```

### 2. Verify initial seed data captured
```bash
docker exec local-postgres psql -U app_user -d app_db -c \
  "SELECT audit_id, operation, widget_id, name, quantity, price 
   FROM widgets_audit ORDER BY audit_id;"
```

Expected: 3 INSERT records (alpha, beta, gamma)

### 3. Test INSERT operation
```bash
docker exec local-postgres psql -U app_user -d app_db -c \
  "INSERT INTO widgets (name, quantity, price) VALUES ('delta', 15, 49.99);"

docker exec local-postgres psql -U app_user -d app_db -c \
  "SELECT * FROM widgets_audit WHERE operation = 'INSERT';"
```

### 4. Test UPDATE operation
```bash
docker exec local-postgres psql -U app_user -d app_db -c \
  "UPDATE widgets SET quantity = 100 WHERE name = 'alpha';"

docker exec local-postgres psql -U app_user -d app_db -c \
  "SELECT * FROM widgets_audit WHERE operation = 'UPDATE';"
```

### 5. Test DELETE operation
```bash
docker exec local-postgres psql -U app_user -d app_db -c \
  "DELETE FROM widgets WHERE name = 'beta';"

docker exec local-postgres psql -U app_user -d app_db -c \
  "SELECT * FROM widgets_audit WHERE operation = 'DELETE';"
```

### 6. View complete audit trail
```bash
docker exec local-postgres psql -U app_user -d app_db -c \
  "SELECT audit_id, operation, widget_id, name, quantity, price, changed_at 
   FROM widgets_audit ORDER BY audit_id;"
```

---

## Useful Audit Queries

### Show all changes for a specific widget
```sql
SELECT * FROM widgets_audit 
WHERE widget_id = 1 
ORDER BY changed_at DESC;
```

### Show changes in the last 24 hours
```sql
SELECT * FROM widgets_audit 
WHERE changed_at > NOW() - INTERVAL '24 hours' 
ORDER BY changed_at DESC;
```

### Count operations by type
```sql
SELECT operation, COUNT(*) as count 
FROM widgets_audit 
GROUP BY operation;
```

### Show audit trail for deleted items
```sql
SELECT * FROM widgets_audit 
WHERE operation = 'DELETE' 
ORDER BY changed_at DESC;
```

### Track price changes for a widget
```sql
SELECT audit_id, operation, price, changed_at 
FROM widgets_audit 
WHERE widget_id = 1 AND operation IN ('INSERT', 'UPDATE')
ORDER BY changed_at;
```

---

## Benefits

1. **Complete Audit Trail**: Every change is captured automatically
2. **No Application Code Required**: Database-level trigger handles everything
3. **Tamper Resistant**: Audit records are separate from main data
4. **Performance Optimized**: Indexed for fast queries
5. **User Tracking**: Captures who made the change (`changed_by`)
6. **Timestamp Tracking**: Captures when the change occurred (`changed_at`)
7. **Individual Row Tracking**: `FOR EACH ROW` ensures every record is captured

---

## Notes

- The trigger captures the **committed** state of records (AFTER trigger)
- For UPDATE operations, it captures the NEW values (after the update)
- For DELETE operations, it captures the OLD values (before deletion)
- For INSERT operations, it captures the NEW values (what was inserted)
- The audit table grows over time; consider implementing a retention policy
- The `changed_by` field captures the database user, not the application user

---

## File Location

**Schema file:** `/home/samuel/projects/postgres/initdb/01-init.sql`

The schema will be automatically applied on first database initialization or after running:
```bash
docker compose down -v
docker compose up -d
```

