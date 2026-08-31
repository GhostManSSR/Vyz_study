# DB Worker

Учебный проект для работы с **PostgreSQL** через **Spring Boot**, **JDBC** и **Flyway**.

Проект позволяет:

* подключаться к PostgreSQL;
* автоматически применять миграции при запуске приложения;
* создавать новые Flyway-миграции через Gradle;
* выполнять `SELECT`-запросы из SQL-файлов через консоль;
* выполнять `INSERT`, `UPDATE`, `DELETE` и другие изменения через SQL-файлы;
* хранить структуру базы данных и изменения схемы в Git.

---

## Стек

* Java 21
* Spring Boot 4.0.1
* Gradle 9.7.1
* PostgreSQL 17
* Flyway 11
* Spring JDBC
* HikariCP

---

## Структура проекта

```text
db_worker/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ru/example/dbworker/
│   │   │       └── DbWorkerApplication.java
│   │   │
│   │   └── resources/
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V20260831200633__initial.sql
│   │       │       ├── V20260831201540__select_prod.sql
│   │       │       └── ...
│   │       │
│   │       └── application.yml
│   │
│   └── test/
│
├── sql/
│   ├── select_all.sql
│   ├── update_user.sql
│   └── ...
│
├── build.gradle
├── gradlew
├── gradlew.bat
├── settings.gradle
└── README.md
```

---

# Подключение к PostgreSQL

Настройки находятся в:

```text
src/main/resources/application.yml
```

Пример:

```yaml
spring:
  application:
    name: db-worker

  datasource:
    url: jdbc:postgresql://localhost:5433/my_bd
    username: postgres
    password: Segnal96
    driver-class-name: org.postgresql.Driver

  flyway:
    enabled: true
    locations: classpath:db/migration
    validate-on-migrate: true
```

Параметры подключения:

| Параметр | Значение   |
| -------- | ---------- |
| Host     | localhost  |
| Port     | 5433       |
| Database | my_bd      |
| User     | postgres   |
| Driver   | PostgreSQL |

> Пароль не рекомендуется хранить непосредственно в `application.yml`, если проект будет выкладываться в Git.

Например, лучше использовать переменные окружения:

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

---

# Flyway

Flyway используется для версионирования структуры базы данных.

Все миграции находятся здесь:

```text
src/main/resources/db/migration/
```

Имена файлов имеют формат:

```text
V<version>__<description>.sql
```

Например:

```text
V20260831200633__initial.sql
V20260831201540__select_prod.sql
V20260831204950__insert_sal.sql
```

Flyway выполняет миграции **по порядку версий**.

---

# Как создать миграцию

В проекте добавлена собственная Gradle-задача:

```bash
.\gradlew.bat migration -PmigrationName=add_product
```

Будет создан файл примерно такого вида:

```text
src/main/resources/db/migration/V20260831210000__add_product.sql
```

Содержимое:

```sql
-- Migration: add_product
-- Created: Mon Aug 31 21:00:00 NOVT 2026
```

После этого SQL необходимо написать самостоятельно.

Например:

```sql
ALTER TABLE prod
ADD COLUMN description varchar(255);
```

---

# Начальная миграция базы

Если база создаётся с нуля, рекомендуется иметь первую миграцию, которая описывает структуру БД.

Например:

```sql
CREATE TABLE prod (
    pnum int PRIMARY KEY,
    name varchar(20) NOT NULL,
    weight int NOT NULL,
    city varchar(20) NOT NULL
);

CREATE TABLE cust (
    cnum int PRIMARY KEY,
    name varchar(20) NOT NULL,
    rating int NOT NULL,
    city varchar(20) NOT NULL
);

CREATE TABLE sal (
    snum int PRIMARY KEY,
    name varchar(20) NOT NULL,
    comm numeric(7,2) NOT NULL,
    city varchar(20) NOT NULL
);

CREATE TABLE ord (
    onum int PRIMARY KEY,
    pnum int NOT NULL,
    cnum int NOT NULL,
    snum int NOT NULL,

    amt int NOT NULL,

    FOREIGN KEY (pnum) REFERENCES prod(pnum),
    FOREIGN KEY (cnum) REFERENCES cust(cnum),
    FOREIGN KEY (snum) REFERENCES sal(snum)
);
```

После этого последующие миграции должны содержать только изменения относительно предыдущего состояния БД.

---

# Запуск приложения

```bash
.\gradlew.bat bootRun
```

При запуске Spring Boot:

1. подключается к PostgreSQL;
2. запускается Flyway;
3. Flyway проверяет миграции;
4. определяет текущую версию БД;
5. применяет новые миграции;
6. запускает Spring Boot.

Пример:

```text
Successfully validated 6 migrations

Current version of schema "public": 20260831202148

Migrating schema "public" to version
"20260831204950 - insert sal"

Successfully applied 1 migration
```

Если новых миграций нет:

```text
Current version of schema "public": 20260831204950
```

и новых миграций применяться не будет.

---

# Важно: Flyway не синхронизирует существующую БД

Flyway **не сравнивает автоматически существующую базу с SQL-файлами**.

Например, если в PostgreSQL уже есть:

```text
prod
cust
sal
ord
```

Flyway не создаст автоматически SQL:

```sql
CREATE TABLE prod ...
CREATE TABLE cust ...
```

Изменения должны быть описаны в миграциях вручную.

Поэтому для существующей базы обычно создают начальную миграцию, которая описывает её структуру.

---

# Данные и миграции

Миграции могут содержать не только `CREATE TABLE`, но и начальные данные.

Например:

```sql
INSERT INTO prod
    (pnum, name, weight, city)
VALUES
    (1001, 'Monitor', 2000, 'Obninsk');

INSERT INTO prod
    (pnum, name, weight, city)
VALUES
    (1002, 'Keyboard', 500, 'Yekaterinburg');
```

Но важно разделять:

### Структуру

```text
V1__create_tables.sql
```

### Начальные данные

```text
V2__insert_initial_products.sql
V3__insert_initial_customers.sql
V4__insert_initial_sales.sql
```

### Последующие изменения

```text
V5__add_product_description.sql
V6__add_customer_email.sql
```

---

# Выполнение SELECT-запросов

Для выполнения SQL-запроса из файла используется:

```bash
.\gradlew.bat dbQuery "-Pfile=.\sql\select_all.sql"
```

Например:

```text
sql/select_all.sql
```

содержит:

```sql
SELECT * FROM prod;
```

Запуск:

```bash
.\gradlew.bat dbQuery "-Pfile=.\sql\select_all.sql"
```

Результат выводится непосредственно в консоль.

Пример:

```text
pnum | name     | weight | city
1001 | Monitor  | 2000   | Obninsk
1002 | Keyboard | 500    | Yekaterinburg
1003 | Mouse    | 100    | Novosibirsk
```

---

# Выполнение UPDATE / INSERT / DELETE

Для изменения данных используется:

```bash
.\gradlew.bat dbUpdate "-Pfile=.\sql\update_user.sql"
```

Например:

```sql
UPDATE cust
SET rating = 500
WHERE cnum = 2001;
```

После выполнения Gradle выводит количество изменённых строк:

```text
SQL успешно выполнен.
Изменено строк: 1
```

---

# Когда использовать dbUpdate, а когда Flyway

Это важное различие.

## dbQuery

Используется для обычных запросов:

```sql
SELECT *
FROM prod;
```

Запрос выполняется и результат выводится в консоль.

```bash
.\gradlew.bat dbQuery "-Pfile=.\sql\select_all.sql"
```

---

## dbUpdate

Используется для разового изменения данных:

```sql
UPDATE prod
SET weight = 2500
WHERE pnum = 1001;
```

```bash
.\gradlew.bat dbUpdate "-Pfile=.\sql/update_product.sql"
```

Такое изменение **не становится миграцией**.

---

## Flyway migration

Используется для изменения состояния БД, которое должно быть воспроизводимо на другой машине.

Например:

```sql
ALTER TABLE prod
ADD COLUMN description varchar(255);
```

Создаём:

```bash
.\gradlew.bat migration -Pname=add_product_description
```

Затем помещаем SQL в созданный файл.

При следующем:

```bash
.\gradlew.bat bootRun
```

Flyway применит миграцию.

---

# Почему нельзя просто менять старую миграцию

После того как миграция была применена:

```text
V20260831200633__initial.sql
```

не следует изменять её содержимое.

Например, если она уже содержит:

```sql
CREATE TABLE prod (...);
```

не нужно потом добавлять туда:

```sql
ALTER TABLE prod ADD COLUMN description varchar(255);
```

Вместо этого создаётся новая миграция:

```text
V20260901090000__add_product_description.sql
```

с:

```sql
ALTER TABLE prod
ADD COLUMN description varchar(255);
```

Это позволяет всем разработчикам получить одинаковую историю изменений БД.

---

# Ошибка duplicate key

Если при запуске появляется:

```text
ERROR: duplicate key value violates unique constraint
```

например:

```text
Key (snum)=(3006) already exists
```

это означает, что миграция пытается добавить запись, которая уже существует в базе.

Например:

```sql
INSERT INTO sal
VALUES (3006, 'DNS', 0.11, 'Moscow');
```

но:

```sql
SELECT *
FROM sal
WHERE snum = 3006;
```

уже возвращает запись.

Flyway не должен повторно вставлять уже существующие данные.

В таком случае нужно определить, должна ли эта запись:

* существовать только в исходной базе;
* добавляться миграцией;
* обновляться через `UPDATE`;
* или миграция вообще больше не нужна.

---

# История миграций

Flyway хранит информацию о выполненных миграциях в таблице:

```text
flyway_schema_history
```

Посмотреть её можно:

```sql
SELECT *
FROM flyway_schema_history
ORDER BY installed_rank;
```

Там можно увидеть:

```text
version
description
type
script
installed_on
success
```

Например:

```text
1
initial
SQL
V1__initial.sql
...
true
```

---

# Проверка таблиц

Посмотреть таблицы PostgreSQL можно через `psql`:

```sql
\dt
```

или обычным SQL:

```sql
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public';
```

Посмотреть структуру таблицы:

```sql
\d prod
```

---

# Работа с Docker PostgreSQL

Если PostgreSQL запущен в Docker-контейнере, приложение может подключаться к нему через проброшенный порт:

```text
localhost:5433
```

Например:

```text
PostgreSQL container
        │
        │ port mapping
        ▼
localhost:5433
        │
        ▼
Spring Boot
```

При этом PostgreSQL внутри контейнера может слушать стандартный порт:

```text
5432
```

а наружу он проброшен как:

```text
5433
```

---

# Работа с pgAdmin

pgAdmin может подключаться к тому же PostgreSQL.

Если pgAdmin работает на Windows, параметры подключения:

```text
Host: localhost
Port: 5433
Database: my_bd
Username: postgres
Password: <пароль>
```

Важно: pgAdmin, Spring Boot и Docker должны подключаться к **одному и тому же экземпляру PostgreSQL**.

Если данные видны в pgAdmin, но приложение их не видит, первым делом нужно проверить:

```text
host
port
database
username
```

---

# Основные команды

### Запустить приложение

```bash
.\gradlew.bat bootRun
```

### Создать миграцию

```bash
.\gradlew.bat migration -Pname=add_product_description
```

### Выполнить SELECT

```bash
.\gradlew.bat dbQuery "-Pfile=.\sql\select_all.sql"
```

### Выполнить UPDATE / INSERT / DELETE

```bash
.\gradlew.bat dbUpdate "-Pfile=.\sql\update_product.sql"
```

### Очистить build

```bash
.\gradlew.bat clean
```

### Посмотреть доступные Gradle-задачи

```bash
.\gradlew.bat tasks
```

---

# Рекомендуемый workflow

При изменении структуры БД:

```text
1. Создать миграцию
       ↓
2. Написать SQL
       ↓
3. Запустить bootRun
       ↓
4. Flyway применяет миграцию
       ↓
5. Проверить БД через pgAdmin
       ↓
6. Закоммитить миграцию в Git
```

Например:

```bash
.\gradlew.bat migration -Pname=add_product_description
```

Создаётся:

```text
V20260831213000__add_product_description.sql
```

Пишем:

```sql
ALTER TABLE prod
ADD COLUMN description varchar(255);
```

Запускаем:

```bash
.\gradlew.bat bootRun
```

Flyway применяет миграцию.

---

# Главное правило проекта

**SQL, который должен воспроизводимо изменить структуру базы данных, должен быть Flyway-миграцией.**

**SQL для разовых операций и анализа данных можно выполнять через `dbQuery` / `dbUpdate`.**

Таким образом:

```text
Flyway
  └── история изменений БД

dbQuery
  └── SELECT и просмотр данных

dbUpdate
  └── разовые INSERT / UPDATE / DELETE

pgAdmin
  └── визуальная работа с PostgreSQL
```
