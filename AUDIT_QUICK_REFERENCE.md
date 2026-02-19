# Shadow Audit Table - Quick Reference

## Schema Modified ✅
**File:** `/home/samuel/projects/postgres/initdb/01-init.sql`

## What Was Added

### 1️⃣ Shadow Audit Table
```sql
widgets_audit
├── audit_id       (BIGSERIAL PRIMARY KEY)
├── operation      (VARCHAR - INSERT/UPDATE/DELETE)
├── widget_id      (BIGINT - references widgets.id)
├── name           (TEXT - snapshot)
├── created_at     (TIMESTAMPTZ - snapshot)
├── quantity       (INTEGER - snapshot)
├── price          (NUMERIC - snapshot)
├── changed_at     (TIMESTAMPTZ - when change occurred)
└── changed_by     (VARCHAR - who made the change)
```

### 2️⃣ Performance Indexes
- `idx_widgets_audit_widget_id` - Fast lookups by widget
- `idx_widgets_audit_changed_at` - Fast time-based queries

### 3️⃣ Trigger Function
`audit_widgets_changes()` - PL/pgSQL function that captures:
- INSERT → NEW values
- UPDATE → NEW values
- DELETE → OLD values

### 4️⃣ Database Trigger
`widgets_audit_trigger` - Fires AFTER each row operation

---

## How It Works

```
┌──────────────┐
│   INSERT     │
│   UPDATE     │──────> Trigger fires ──────> Record saved to
│   DELETE     │        (AFTER)                widgets_audit
└──────────────┘
     ON widgets         FOR EACH ROW          (automatically)
```

---

## Testing

### Reset & Start
```bash
docker compose down -v && docker compose up -d && sleep 5
```

### Check Initial Audit (3 INSERTs from seed data)
```bash
docker exec local-postgres psql -U app_user -d app_db -c \
  "SELECT * FROM widgets_audit;"
```

### Test INSERT
```bash
docker exec local-postgres psql -U app_user -d app_db -c \
  "INSERT INTO widgets (name, quantity, price) VALUES ('test', 99, 9.99);"
```

### Test UPDATE
```bash
docker exec local-postgres psql -U app_user -d app_db -c \
  "UPDATE widgets SET quantity = 200 WHERE name = 'alpha';"
```

### Test DELETE
```bash
docker exec local-postgres psql -U app_user -d app_db -c \
  "DELETE FROM widgets WHERE name = 'beta';"
```

### View Complete Audit Trail
```bash
docker exec local-postgres psql -U app_user -d app_db -c \
  "SELECT audit_id, operation, widget_id, name, quantity, price, changed_at 
   FROM widgets_audit ORDER BY changed_at;"
```

---

## Useful Queries

### Show changes for specific widget
```sql
SELECT * FROM widgets_audit WHERE widget_id = 1 ORDER BY changed_at;
```

### Count operations
```sql
SELECT operation, COUNT(*) FROM widgets_audit GROUP BY operation;
```

### Recent changes (last hour)
```sql
SELECT * FROM widgets_audit WHERE changed_at > NOW() - INTERVAL '1 hour';
```

### All deletions
```sql
SELECT * FROM widgets_audit WHERE operation = 'DELETE';
```

---

## Key Features

✅ **Automatic** - No app code changes needed  
✅ **Row-level** - Captures each record individually  
✅ **Complete** - Captures INSERT, UPDATE, DELETE  
✅ **Timestamped** - Tracks when changes occur  
✅ **Attributed** - Tracks who made changes  
✅ **Indexed** - Fast queries  
✅ **Immutable** - Audit log is append-only  

---

## Documentation

📄 **Full Documentation:** `AUDIT_TABLE_DOCUMENTATION.md`  
📄 **Implementation Summary:** `AUDIT_IMPLEMENTATION_SUMMARY.md`  
📄 **Test Script:** `test-audit.sql`  
📄 **This Reference:** `AUDIT_QUICK_REFERENCE.md`

---

## Success! 🎉

The shadow audit table is now capturing all changes to the `widgets` table automatically at the database level.

