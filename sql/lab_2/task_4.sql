SELECT CONCAT(
               pnum, '.',
               UPPER(name), '.',
               UPPER(city)
       ) AS product_info
FROM my_schema.prod
WHERE name LIKE '%rd%'
   OR name LIKE '%RD%';