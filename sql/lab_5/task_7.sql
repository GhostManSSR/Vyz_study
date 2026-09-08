SELECT city
FROM prod
WHERE name ILIKE '%e%'

INTERSECT

SELECT city
FROM sal
WHERE name ILIKE '%e%';