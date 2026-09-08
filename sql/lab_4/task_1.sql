SELECT *
FROM ord
WHERE amt > (
    SELECT AVG(amt)
    FROM ord
);