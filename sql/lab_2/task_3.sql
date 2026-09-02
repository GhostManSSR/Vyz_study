SELECT REPLACE(
               REPLACE(
                       REPLACE(
                               REPLACE(
                                       REPLACE(
                                               LOWER(city),
                                               'a', 'o'
                                       ),
                                       'e', 'o'
                               ),
                               'i', 'o'
                       ),
                       'o', 'o'
               ),
               'u', 'o'
       ) AS city_modified
FROM my_schema.prod
WHERE LENGTH(city) > 6;


-- SELECT TRANSLATE(LOWER(city), 'aeiou', 'ooooo') AS city_modified
-- FROM my_schema.prod
-- WHERE LENGTH(city) > 6;