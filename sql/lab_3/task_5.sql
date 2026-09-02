SELECT
    pnum,
    COUNT(*) AS orders_count
FROM my_schema.ord
GROUP BY pnum
HAVING COUNT(*) < 20;