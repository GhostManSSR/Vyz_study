SELECT
    o.*,
    c.cnum,
    c.name,
    c.city AS customer_city,
    c.rating
FROM ord o
         LEFT JOIN cust c
                   ON o.cnum = c.cnum;