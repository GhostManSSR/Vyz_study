SELECT pnum,
       COUNT(*) AS quantity
FROM my_schema.ord
GROUP BY pnum
ORDER BY quantity DESC;