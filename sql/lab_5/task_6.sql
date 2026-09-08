SELECT city
FROM sal
WHERE city ILIKE '%a%'

UNION

SELECT city
FROM cust
WHERE city ILIKE '%a%'

UNION

SELECT city
FROM prod
WHERE city ILIKE '%a%';