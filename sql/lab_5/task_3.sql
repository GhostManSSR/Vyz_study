SELECT c.name, c.rating,
    CASE
        WHEN c.rating >= 300 THEN o.max_order
        ELSE NULL
        END AS max_order
FROM cust c LEFT JOIN (
    SELECT
        cnum,
        MAX(amt) AS max_order
    FROM ord
    GROUP BY cnum
) o ON c.cnum = o.cnum;