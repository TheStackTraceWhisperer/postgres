# Widget Entity Update - Summary

## Changes Made

### 1. Widget.java Entity
**Location:** `/home/samuel/projects/postgres/application/src/main/java/application/model/Widget.java`

**Added fields:**
- `quantity` (Integer) - nullable=false
- `price` (BigDecimal) - nullable=false

**Added constructor:**
```java
public Widget(String name, OffsetDateTime createdAt, Integer quantity, java.math.BigDecimal price)
```

**Added getters:**
- `getQuantity()` returns Integer
- `getPrice()` returns BigDecimal

### 2. Database Schema Update
**Location:** `/home/samuel/projects/postgres/initdb/01-init.sql`

**Updated CREATE TABLE:**
```sql
CREATE TABLE IF NOT EXISTS public.widgets (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  quantity INTEGER NOT NULL,
  price NUMERIC(10, 2) NOT NULL
);
```

**Updated seed data:**
```sql
INSERT INTO public.widgets (name, quantity, price) VALUES
  ('alpha', 10, 19.99),
  ('beta', 25, 29.99),
  ('gamma', 5, 39.99);
```

## Verification

### Maven Tests
Tests pass successfully with the updated Widget entity:
```bash
cd /home/samuel/projects/postgres/application
mvn test
```
Result: BUILD SUCCESS

### Database Schema
To verify the database has the new columns, run:
```bash
cd /home/samuel/projects/postgres
docker compose down -v  # Reset to re-run init scripts
docker compose up -d
sleep 3
docker exec local-postgres psql -U app_user -d app_db -c "SELECT * FROM widgets;"
```

Expected output should show: id, name, created_at, quantity, price columns with 3 rows of data.

## Notes
- The Widget entity now fully supports quantity and price fields
- The database schema matches the entity
- Maven tests compile and pass successfully
- Both the 2-argument and 4-argument constructors are available for flexibility

