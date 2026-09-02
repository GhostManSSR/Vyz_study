SELECT city
FROM my_schema.cust
WHERE city ~* '^[^aeiou][aeiou]';