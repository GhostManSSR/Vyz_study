SET search_path TO my_schema;

SELECT
    onum,
    pnum,
    cnum,
    snum,
    amt,

    ROW_NUMBER() OVER w AS row_number,
    RANK() OVER w AS rank,
    DENSE_RANK() OVER w AS dense_rank

FROM ord

         WINDOW w AS (
    PARTITION BY snum, cnum
    ORDER BY amt DESC
)

ORDER BY snum, cnum, amt DESC, onum;