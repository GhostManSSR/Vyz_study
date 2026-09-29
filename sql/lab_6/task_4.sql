CREATE TABLE my_schema.lab6_orders AS
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

WHERE o.amt > (
    SELECT AVG(amt)
    FROM my_schema.ord
)

  AND p.city <> 'Saint Petersburg'

  AND s.snum IN (
    SELECT snum
    FROM my_schema.ord
    GROUP BY snum
    HAVING COUNT(*) <= 10
)

  AND c.rating >= (
    SELECT MIN(rating)
    FROM my_schema.cust
    WHERE city = 'Moscow'
);

-- Вывод информации
SELECT *
FROM my_schema.lab6_orders
ORDER BY order_number;