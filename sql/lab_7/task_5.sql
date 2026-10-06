SELECT
    onum,
    ord_date,
    snum,
    cnum,
    amt,

    MAX(amt) OVER (
        PARTITION BY snum
        ORDER BY ord_date, onum
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    ) AS max_amt_so_far

FROM ord

ORDER BY snum, ord_date, onum;