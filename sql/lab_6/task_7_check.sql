SELECT
    o.onum,
    o.amt,
    p.name AS product,
    p.city AS product_city,
    c.name AS customer,
    c.rating,
    c.city AS customer_city,
    s.name AS seller,
    s.city AS seller_city
FROM my_schema.ord o
         JOIN my_schema.prod p
              ON p.pnum = o.pnum
         JOIN my_schema.cust c
              ON c.cnum = o.cnum
         JOIN my_schema.sal s
              ON s.snum = o.snum
WHERE o.amt > (SELECT AVG(amt) FROM my_schema.ord)
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
)
ORDER BY o.onum;



| Структура           | Что хранит                  | Когда выполняется запрос    | Автоматически обновляется |
| ------------------- | --------------------------- | --------------------------- | ------------------------- |
| `TABLE AS`          | Результат запроса           | При `CREATE TABLE AS`       | Нет                       |
| `VIEW`              | Только SQL-запрос           | При каждом `SELECT`         | Да                        |
| `MATERIALIZED VIEW` | Результат запроса           | При создании/`REFRESH`      | Нет                       |
| `WITH` / CTE        | Временный результат запроса | Во время выполнения запроса | Не применимо              |
