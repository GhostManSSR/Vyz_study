SELECT pr.pnum, pr.weight, pr.name, pr.city
FROM prod AS pr
WHERE pr.weight < 700 OR pr.weight = 700;