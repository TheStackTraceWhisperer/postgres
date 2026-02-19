# PostgreSQL Docker Compose Bootstrap

This project starts a local PostgreSQL instance and initializes the database from SQL files on disk.

## Files
- `docker-compose.yml`: Compose config for PostgreSQL.
- `.env`: Local credentials and database name.
- `initdb/01-init.sql`: Schema with widgets table, audit table, and triggers.

## Features
- **Main Table**: `widgets` - stores widget data with quantity and price
- **Shadow Audit Table**: `widgets_audit` - automatically captures all INSERT, UPDATE, DELETE operations
- **Database Trigger**: Captures each row change individually before commit
- **Indexes**: Optimized for fast audit queries

## Quick start
```bash
docker compose up -d
```

## Verify
```bash
# View widgets
docker compose exec postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT * FROM widgets;"

# View audit trail
docker compose exec postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT * FROM widgets_audit ORDER BY audit_id;"
```

## Test Audit Functionality
```bash
# Run the audit test script
docker exec local-postgres psql -U app_user -d app_db -f /docker-entrypoint-initdb.d/../test-audit.sql

# Or test manually
docker compose exec postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"
# Then run: INSERT INTO widgets (name, quantity, price) VALUES ('test', 1, 1.00);
# Check audit: SELECT * FROM widgets_audit WHERE widget_id = (SELECT id FROM widgets WHERE name = 'test');
```

## Reset (re-run init scripts)
```bash
docker compose down -v
```

## Notes
- Init scripts in `initdb/` only run on the first startup when the data directory is empty.
- Add more SQL files to `initdb/` to extend the schema or seed data.
- The audit trigger captures ALL changes automatically at the database level.
- See `AUDIT_TABLE_DOCUMENTATION.md` for detailed audit functionality documentation.

