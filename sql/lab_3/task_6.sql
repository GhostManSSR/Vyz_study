SELECT
    cnum,
    pnum,
    COUNT(*) AS orders_count
FROM my_schema.ord
GROUP BY cnum, pnum;