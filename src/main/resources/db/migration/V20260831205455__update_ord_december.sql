-- Migration: update_ord_december
-- Created: Mon Aug 31 20:54:55 NOVT 2026

UPDATE my_schema.ord
SET ord_date = DATE '2025-12-31'
WHERE pnum = 1002;