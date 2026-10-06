SELECT
    onum,
    ord_date,
    snum,
    cnum,
    amt,

    MAX(amt) OVER (
        ORDER BY ord_date, onum
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    ) AS max_amt_so_far

FROM ord

ORDER BY ord_date, onum;