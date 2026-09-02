SELECT name
FROM my_schema.sal
WHERE name ~* '^[^a-d]'
  AND name ~* 's';