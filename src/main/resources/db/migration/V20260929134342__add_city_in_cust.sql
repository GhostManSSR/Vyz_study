-- Migration: add_city_in_cust
-- Created: Tue Sep 29 13:43:42 NOVT 2026

INSERT INTO my_schema.cust (cnum, cname, rating, city)
SELECT
    COALESCE(MAX(cnum), 0) + n,
    'test_name',
    100,
    'test_city'
FROM generate_series(1, 1000) AS s(n)
         CROSS JOIN my_schema.cust;