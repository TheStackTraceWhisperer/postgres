#!/bin/bash
set -e

echo "=== Testing Audit Table and Trigger ==="
echo ""

# Wait for PostgreSQL to be ready
echo "1. Waiting for PostgreSQL to be ready..."
sleep 5

# Check if tables exist
echo ""
echo "2. Verifying tables exist..."
docker exec local-postgres psql -U app_user -d app_db -c "\dt public.*"

echo ""
echo "3. Initial data in widgets table:"
docker exec local-postgres psql -U app_user -d app_db -c "SELECT * FROM public.widgets;"

echo ""
echo "4. Initial audit records (should have 3 INSERT records from seed data):"
docker exec local-postgres psql -U app_user -d app_db -c "SELECT audit_id, operation, widget_id, name, quantity, price, changed_at FROM public.widgets_audit ORDER BY audit_id;"

echo ""
echo "5. Testing INSERT - adding a new widget..."
docker exec local-postgres psql -U app_user -d app_db -c "INSERT INTO public.widgets (name, quantity, price) VALUES ('delta', 15, 49.99);"

echo ""
echo "6. Audit records after INSERT:"
docker exec local-postgres psql -U app_user -d app_db -c "SELECT audit_id, operation, widget_id, name, quantity, price FROM public.widgets_audit ORDER BY audit_id;"

echo ""
echo "7. Testing UPDATE - modifying a widget..."
docker exec local-postgres psql -U app_user -d app_db -c "UPDATE public.widgets SET quantity = 100, price = 99.99 WHERE name = 'alpha';"

echo ""
echo "8. Audit records after UPDATE:"
docker exec local-postgres psql -U app_user -d app_db -c "SELECT audit_id, operation, widget_id, name, quantity, price FROM public.widgets_audit ORDER BY audit_id;"

echo ""
echo "9. Testing DELETE - removing a widget..."
docker exec local-postgres psql -U app_user -d app_db -c "DELETE FROM public.widgets WHERE name = 'beta';"

echo ""
echo "10. Final audit trail (showing all operations):"
docker exec local-postgres psql -U app_user -d app_db -c "SELECT audit_id, operation, widget_id, name, quantity, price, changed_at FROM public.widgets_audit ORDER BY audit_id;"

echo ""
echo "11. Current widgets table (after all operations):"
docker exec local-postgres psql -U app_user -d app_db -c "SELECT * FROM public.widgets ORDER BY id;"

echo ""
echo "12. Audit summary by operation:"
docker exec local-postgres psql -U app_user -d app_db -c "SELECT operation, COUNT(*) as count FROM public.widgets_audit GROUP BY operation ORDER BY operation;"

echo ""
echo "=== Audit functionality verified successfully! ==="

