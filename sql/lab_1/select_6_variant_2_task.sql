SELECT pr.pnum, pr.weight, pr.name, pr.city
FROM my_schema.prod AS pr
WHERE pr.weight < 700 OR pr.weight = 700;