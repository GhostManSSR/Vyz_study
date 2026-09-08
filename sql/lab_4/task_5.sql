SELECT *
FROM prod
WHERE weight > ALL (
    SELECT weight
    FROM prod
    WHERE city = 'Новосибирск'
);