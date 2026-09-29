SELECT
    n.nspname AS schema_name,
    t.relname AS table_name,
    i.relname AS index_name,
    am.amname AS index_type,
    ix.indisunique AS is_unique,
    ix.indisprimary AS is_primary,
    ix.indisclustered AS is_clustered
FROM pg_index ix
         JOIN pg_class t
              ON t.oid = ix.indrelid
         JOIN pg_class i
              ON i.oid = ix.indexrelid
         JOIN pg_namespace n
              ON n.oid = t.relnamespace
         JOIN pg_am am
              ON am.oid = i.relam
WHERE n.nspname = 'my_schema'
ORDER BY t.relname, i.relname;


-- В PostgreSQL индексы являются отдельными структурами данных и физически хранятся отдельно от таблицы. Для первичных ключей автоматически создаются уникальные B-Tree индексы.
--
-- Индексы PostgreSQL не являются кластеризованными в смысле автоматического поддержания физического порядка строк таблицы. Признак indisclustered показывает, был ли индекс выбран командой CLUSTER, однако после изменения таблицы PostgreSQL автоматически не поддерживает кластеризацию.