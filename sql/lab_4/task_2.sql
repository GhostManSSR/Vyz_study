SELECT o.pnum, SUM(o.amt) AS total_amount
FROM ord o
GROUP BY o.pnum
HAVING SUM(o.amt) < (
    SELECT SUM(o2.amt)
    FROM ord o2
    WHERE o2.pnum = 1002
);