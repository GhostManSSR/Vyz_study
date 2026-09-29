CREATE INDEX IF NOT EXISTS idx_cust_city
    ON my_schema.cust USING BTREE (city);