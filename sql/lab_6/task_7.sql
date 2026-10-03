WITH
    average_order AS (
        SELECT AVG(amt) AS avg_amt
        FROM my_schema.ord
    ),

    seller_orders AS (
        SELECT
            snum,
            COUNT(*) AS orders_count
        FROM my_schema.ord
        GROUP BY snum
        HAVING COUNT(*) <= 10
    ),

    moscow_rating AS (
        SELECT MIN(rating) AS min_rating
        FROM my_schema.cust
        WHERE city = 'Moscow'
    )

SELECT
    o.onum AS order_number,
    o.amt AS amount,

    p.pnum AS product_number,
    p.name AS product_name,
    p.weight AS product_weight,
    p.city AS product_city,

    c.cnum AS customer_number,
    c.name AS customer_name,
    c.rating AS customer_rating,
    c.city AS customer_city,

    s.snum AS seller_number,
    s.name AS seller_name,
    s.comm AS seller_commission,
    s.city AS seller_city

FROM my_schema.ord o

         JOIN my_schema.prod p
              ON p.pnum = o.pnum

         JOIN my_schema.cust c
              ON c.cnum = o.cnum

         JOIN my_schema.sal s
              ON s.snum = o.snum

         CROSS JOIN average_order ao

         JOIN seller_orders so
              ON so.snum = s.snum

         CROSS JOIN moscow_rating mr

WHERE o.amt > ao.avg_amt
  AND p.city <> 'Saint Petersburg'
  AND c.rating >= mr.min_rating

ORDER BY o.onum;