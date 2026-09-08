SELECT
    pnum,
    COUNT(DISTINCT ord_date) AS days_count,
    CASE
        WHEN COUNT(DISTINCT ord_date) > 5
            THEN 'ежедневный'

        WHEN COUNT(DISTINCT ord_date) BETWEEN 3 AND 5
            THEN 'сезонный'

        WHEN COUNT(DISTINCT ord_date) BETWEEN 1 AND 2
            THEN 'редкий'
        END AS popularity
FROM ord
GROUP BY pnum;