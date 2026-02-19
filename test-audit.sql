-- Test script to verify audit table and trigger functionality
-- Run with: docker exec local-postgres psql -U app_user -d app_db -f /path/to/test-audit.sql

\echo '=== Initial State ==='
\echo ''
\echo 'Widgets table:'
SELECT * FROM public.widgets ORDER BY id;

\echo ''
\echo 'Initial audit records (from seed data):'
SELECT audit_id, operation, widget_id, name, quantity, price
FROM public.widgets_audit
ORDER BY audit_id;

\echo ''
\echo '=== Test 1: INSERT Operation ==='
INSERT INTO public.widgets (name, quantity, price) VALUES ('delta', 15, 49.99);

\echo 'Audit records after INSERT:'
SELECT audit_id, operation, widget_id, name, quantity, price
FROM public.widgets_audit
WHERE operation = 'INSERT'
ORDER BY audit_id;

\echo ''
\echo '=== Test 2: UPDATE Operation ==='
UPDATE public.widgets SET quantity = 100, price = 99.99 WHERE name = 'alpha';

\echo 'Audit records after UPDATE:'
SELECT audit_id, operation, widget_id, name, quantity, price
FROM public.widgets_audit
WHERE operation = 'UPDATE'
ORDER BY audit_id;

\echo ''
\echo '=== Test 3: DELETE Operation ==='
DELETE FROM public.widgets WHERE name = 'beta';

\echo 'Audit records after DELETE:'
SELECT audit_id, operation, widget_id, name, quantity, price
FROM public.widgets_audit
WHERE operation = 'DELETE'
ORDER BY audit_id;

\echo ''
\echo '=== Final Results ==='
\echo ''
\echo 'Current widgets table:'
SELECT * FROM public.widgets ORDER BY id;

\echo ''
\echo 'Complete audit trail:'
SELECT audit_id, operation, widget_id, name, quantity, price, changed_at
FROM public.widgets_audit
ORDER BY audit_id;

\echo ''
\echo 'Summary by operation type:'
SELECT operation, COUNT(*) as count
FROM public.widgets_audit
GROUP BY operation
ORDER BY operation;

