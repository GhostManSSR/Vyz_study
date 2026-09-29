-- SET enable_seqscan = OFF;

EXPLAIN ANALYZE
SELECT *
FROM my_schema.cust
ORDER BY city;

-- SET enable_seqscan = ON;