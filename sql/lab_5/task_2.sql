SELECT
    o.onum,
    o.amt,
    p.name,
    p.city
FROM ord o
         JOIN prod p
              ON o.pnum = p.pnum
WHERE p.city <> 'Обнинск';