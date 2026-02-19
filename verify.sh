#!/bin/bash
set -e

echo "=== Verifying quantity and price fields ==="

# Start fresh
echo "Stopping existing containers..."
docker compose -f /home/samuel/projects/postgres/docker-compose.yml down -v > /dev/null 2>&1 || true

echo "Starting PostgreSQL..."
docker compose -f /home/samuel/projects/postgres/docker-compose.yml up -d

echo "Waiting for PostgreSQL to be ready..."
sleep 3

echo ""
echo "Querying widgets table:"
docker exec local-postgres psql -U app_user -d app_db -c "SELECT id, name, quantity, price, created_at FROM widgets;"

echo ""
echo "Table schema:"
docker exec local-postgres psql -U app_user -d app_db -c "\d widgets"

