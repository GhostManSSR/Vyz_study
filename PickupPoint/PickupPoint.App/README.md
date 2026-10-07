1)
$env:PGPASSWORD = "1"
$psql = "C:\Program Files\PostgreSQL\18\bin\psql.exe"

2)
& $psql -h localhost -U postgres -d postgres -c "CREATE DATABASE pickup"
& $psql -h localhost -U postgres -d postgres -c "CREATE DATABASE pickup_test"

3)
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -h localhost -U postgres -d pickup -c "GRANT ALL ON SCHEMA public TO pickup_user; GRANT ALL ON ALL TABLES IN SCHEMA public TO pickup_user; GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO pickup_user; ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO pickup_user; ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO pickup_user;"
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -h localhost -U postgres -d pickup_test -c "GRANT ALL ON SCHEMA public TO pickup_user; GRANT ALL ON ALL TABLES IN SCHEMA public TO pickup_user; GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO pickup_user; ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO pickup_user; ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO pickup_user;"

4)
$env:PGPASSWORD = "1"
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -h localhost -U pickup_user -d pickup -f schema.sql
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -h localhost -U pickup_user -d pickup_test -f schema.sql




# 1. Права на pickup
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -h localhost -U postgres -d pickup -c "GRANT ALL ON SCHEMA public TO pickup_user; GRANT ALL ON ALL TABLES IN SCHEMA public TO pickup_user; GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO pickup_user; ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO pickup_user; ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO pickup_user;"

# 2. Права на pickup_test
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -h localhost -U postgres -d pickup_test -c "GRANT ALL ON SCHEMA public TO pickup_user; GRANT ALL ON ALL TABLES IN SCHEMA public TO pickup_user; GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO pickup_user; ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO pickup_user; ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO pickup_user;"

# 3. Схема в pickup
$env:PGPASSWORD = "pickup_pass"
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -h localhost -U pickup_user -d pickup -f schema.sql

# 4. Схема в pickup_test
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -h localhost -U pickup_user -d pickup_test -f schema.sql

# 5. Проверка
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -h localhost -U pickup_user -d pickup -c "\d orders"

# 6. Проект
cd C:\Users\Timofey\Desktop\git\PickupPoint\PickupPoint
$env:PICKUP_DB      = "Host=localhost;Database=pickup;Username=pickup_user;Password=pickup_pass"
$env:PICKUP_DB_TEST = "Host=localhost;Database=pickup_test;Username=pickup_user;Password=pickup_pass"
dotnet build
dotnet test
dotnet run --project PickupPoint.App