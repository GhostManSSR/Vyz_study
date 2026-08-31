-- Migration: init_schema
-- Created: Mon Aug 31 20:21:48 NOVT 2026

-- Создание таблицы продуктов
CREATE TABLE prod (
                      pnum INT PRIMARY KEY,
                      name VARCHAR(20) NOT NULL,
                      weight INT NOT NULL,
                      city VARCHAR(20) NOT NULL
);

-- Создание таблицы покупателей
CREATE TABLE cust (
                      cnum INT PRIMARY KEY,
                      name VARCHAR(20) NOT NULL,
                      rating INT NOT NULL,
                      city VARCHAR(20) NOT NULL
);

-- Создание таблицы продавцов
CREATE TABLE sal (
                     snum INT PRIMARY KEY,
                     name VARCHAR(20) NOT NULL,
                     comm NUMERIC(7, 2) NOT NULL,
                     city VARCHAR(20) NOT NULL
);

-- Создание таблицы заказов
CREATE TABLE ord (
                     onum INT PRIMARY KEY,
                     pnum INT NOT NULL,
                     cnum INT NOT NULL,
                     snum INT NOT NULL,
                     amt INT NOT NULL,

                     FOREIGN KEY (pnum) REFERENCES prod(pnum),
                     FOREIGN KEY (cnum) REFERENCES cust(cnum),
                     FOREIGN KEY (snum) REFERENCES sal(snum)
);


-- Начальные данные продуктов
INSERT INTO prod (pnum, name, weight, city) VALUES
                                                (1001, 'Monitor', 2000, 'Obninsk'),
                                                (1002, 'Keyboard', 500, 'Yekaterinburg'),
                                                (1003, 'Mouse', 100, 'Novosibirsk'),
                                                (1004, 'Printer', 1500, 'Saint Petersburg'),
                                                (1005, 'Hard drive', 300, 'Moscow'),
                                                (1006, 'Speakers', 700, 'Novosibirsk');


-- Начальные данные покупателей
INSERT INTO cust (cnum, name, rating, city) VALUES
                                                (2001, 'Ivanov', 100, 'Perm'),
                                                (2002, 'Petrov', 100, 'Moscow'),
                                                (2003, 'Vasiliev', 200, 'Yekaterinburg'),
                                                (2004, 'Dmitriev', 200, 'Krasnoyarsk'),
                                                (2005, 'Skvortcov', 300, 'Moscow'),
                                                (2006, 'Avdeev', 400, 'Novosibirsk'),
                                                (2007, 'Smirnov', 100, 'Omsk');


-- Начальные данные продавцов
INSERT INTO sal (snum, name, comm, city) VALUES
                                             (3001, 'DNS', 0.11, 'Novosibirsk'),
                                             (3002, 'Citylink', 0.12, 'Saint Petersburg'),
                                             (3003, 'MVideo', 0.15, 'Yekaterinburg'),
                                             (3004, 'Inline', 0.13, 'Vladivostok'),
                                             (3005, 'Elbrus', 0.11, 'Moscow');


-- Начальные данные заказов
INSERT INTO ord (onum, pnum, cnum, snum, amt) VALUES
                                                  (4001, 1001, 2001, 3001, 2),
                                                  (4002, 1001, 2002, 3001, 1),
                                                  (4003, 1001, 2006, 3002, 10),
                                                  (4004, 1001, 2003, 3003, 5),
                                                  (4005, 1001, 2004, 3004, 5),
                                                  (4006, 1002, 2001, 3005, 7),
                                                  (4007, 1002, 2002, 3001, 8),
                                                  (4008, 1003, 2001, 3002, 3),
                                                  (4009, 1003, 2006, 3003, 1),
                                                  (4010, 1003, 2007, 3004, 9),
                                                  (4011, 1003, 2004, 3003, 6),
                                                  (4012, 1004, 2002, 3001, 6),
                                                  (4013, 1004, 2001, 3002, 4),
                                                  (4014, 1004, 2001, 3004, 4),
                                                  (4015, 1004, 2006, 3003, 3),
                                                  (4016, 1004, 2004, 3005, 3),
                                                  (4017, 1004, 2007, 3002, 2),
                                                  (4018, 1005, 2001, 3005, 1),
                                                  (4019, 1006, 2004, 3005, 2),
                                                  (4020, 1006, 2003, 3002, 3);