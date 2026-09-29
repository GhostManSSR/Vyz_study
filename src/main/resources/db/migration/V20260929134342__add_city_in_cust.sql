-- Migration: add_city_in_cust
-- Created: Tue Sep 29 13:43:42 NOVT 2026

insert into cust
values ( generate_series(1, 1000), 'test_name', 100, 'test_city' );
