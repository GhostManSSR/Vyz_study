-- Migration: add_city_in_cust
-- Created: Tue Sep 29 13:43:42 NOVT 2026

-- INSERT INTO my_schema.cust (cnum, cname, rating, city)
-- SELECT
--     COALESCE(MAX(cnum), 0) + n,
--     'test_name',
--     100,
--     'test_city'
-- FROM generate_series(1, 1000) AS s(n)
--          CROSS JOIN my_schema.cust;

INSERT INTO sal
VALUES (3007, 'Eldorado', 0.13, 'Moscow');

INSERT INTO sal
VALUES (3008, 'Ozon', 0.20, 'Novosibirsk');

INSERT INTO sal
VALUES (3009, 'Wildberries', 0.19, 'Novosibirsk');

INSERT INTO sal
VALUES (3010, 'CityCenter', 0.10, 'Novosibirsk');

INSERT INTO sal
VALUES (3011, 'YandexMarket', 0.09, 'Moscow');

INSERT INTO sal
VALUES (3012, 'BiblioGlobus', 0.15, 'Saint Petersburg');

INSERT INTO sal
VALUES (3013, 'Enter', 0.17, 'Yekaterinburg');

INSERT INTO sal
VALUES (3014, 'Technopark', 0.16, 'Innopolis');

INSERT INTO sal
VALUES (3015, 'Regard', 0.14, 'Saint Petersburg');

INSERT INTO sal
VALUES (3016, 'Nix', 0.11, 'Innopolis');