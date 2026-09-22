SELECT s.name, SUM(o.amt) AS total_amount
FROM sal s JOIN ord o ON s.snum = o.snum
WHERE s.city NOT IN (
    SELECT c.city
    FROM cust c
)
GROUP BY s.snum, s.name;