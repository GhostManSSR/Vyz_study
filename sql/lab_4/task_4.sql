SELECT name
FROM cust
WHERE cnum NOT IN (
    SELECT o.cnum
    FROM ord o
    WHERE o.pnum IN (
        SELECT p.pnum
        FROM prod p
        WHERE p.city = 'Obninsk'
    )
);