-- Migration: delete_sal
-- Created: Mon Aug 31 20:51:59 NOVT 2026
DELETE FROM my_schema.sal
WHERE snum = 3007;
