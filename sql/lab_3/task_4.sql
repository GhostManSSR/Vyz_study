SELECT COUNT(DISTINCT cnum) AS customer_count
FROM my_schema.ord
WHERE snum IN (3001, 3002, 3006);