SELECT *
FROM sal s
WHERE s.snum IN (
    SELECT o.snum
    FROM ord o
    WHERE o.pnum IN (
        SELECT p.pnum
        FROM prod p
        WHERE p.city = s.city
    )
);