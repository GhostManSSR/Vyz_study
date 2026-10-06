SELECT
    onum,
    ord_date,
    snum,
    cnum,
    amt AS curr_amt,

    amt - COALESCE(
            LAG(amt) OVER (
                    PARTITION BY snum
            ORDER BY ord_date, onum
                     ),
            0
          ) AS diff_prev,

    COALESCE(
            LEAD(amt) OVER (
            PARTITION BY snum
            ORDER BY ord_date, onum
                      ),
            0
    ) - amt AS diff_next

FROM ord

ORDER BY snum, ord_date, onum;