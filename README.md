# PostgreSQL Docker Compose Bootstrap

This project starts a local PostgreSQL instance and initializes the database from SQL files on disk.

## Files
- `docker-compose.yml`: Compose config for PostgreSQL.
- `.env`: Local credentials and database name.
- `initdb/01-init.sql`: Example schema + seed data.

## Quick start
```bash
docker compose up -d
```

## Verify
```bash
docker compose exec postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT * FROM widgets;"
```

## Reset (re-run init scripts)
```bash
docker compose down -v
```

## Notes
- Init scripts in `initdb/` only run on the first startup when the data directory is empty.
- Add more SQL files to `initdb/` to extend the schema or seed data.

