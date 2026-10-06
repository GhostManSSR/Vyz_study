SELECT
    onum,
    ord_date,
    snum,
    cnum,
    amt,

    COUNT(*) OVER (
        ORDER BY amt
        RANGE BETWEEN 1 PRECEDING AND 1 FOLLOWING
    ) AS orders_count

FROM ord

ORDER BY amt, ord_date, onum;