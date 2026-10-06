SELECT
    onum,
    ord_date,
    snum,
    cnum,
    amt,

    ROW_NUMBER() OVER (
        PARTITION BY snum
        ORDER BY amt DESC
    ) AS row_number,

    RANK() OVER (
        PARTITION BY snum
        ORDER BY amt DESC
    ) AS rank,

    DENSE_RANK() OVER (
        PARTITION BY snum
        ORDER BY amt DESC
    ) AS dense_rank

FROM ord
ORDER BY snum, amt DESC;