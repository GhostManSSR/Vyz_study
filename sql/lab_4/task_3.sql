SELECT *
FROM cust
WHERE cnum IN (
    SELECT cnum
    FROM ord
    WHERE snum = 3006
      AND pnum IN (
        SELECT pnum
        FROM ord
        WHERE snum = 3002
    )
);