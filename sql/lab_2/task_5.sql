SELECT CURRENT_DATE - MAX(ord_date) AS days_since_last_order
FROM my_schema.ord;