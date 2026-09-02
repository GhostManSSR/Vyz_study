SELECT
    ord_date,
    COUNT(DISTINCT cnum) AS customers_count
FROM my_schema.ord
GROUP BY ord_date
HAVING COUNT(DISTINCT cnum) > 3;