SELECT
    p.*,
    CASE
        WHEN city IN ('Saint Petersburg', 'Moscow')
            THEN 'федерального значения'

        WHEN city IN ('Novosibirsk', 'Yekaterinburg')
            THEN 'миллионник'

        ELSE 'другое'
        END AS "статус города продукта"
FROM prod p;