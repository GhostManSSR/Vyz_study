SELECT
    p1.pnum AS product_1,
    p2.pnum AS product_2
FROM prod p1
         CROSS JOIN prod p2
WHERE p1.pnum < p2.pnum;