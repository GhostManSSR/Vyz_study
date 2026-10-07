# setup.ps1 - creates user pickup_user and databases pickup + pickup_test.
# Lives in PickupPoint.Db. Run from this folder: .\setup.ps1

param(
    [string]$PostgresPassword = "",
    [string]$DbUser     = "pickup_user",
    [string]$DbPassword = "pickup_pass",
    [string]$MainDb     = "pickup",
    [string]$TestDb     = "pickup_test"
)

$ErrorActionPreference = "Stop"

# --- Find psql ---
$psql = $null
$candidates = @(
    "psql",
    "C:\Program Files\PostgreSQL\17\bin\psql.exe",
    "C:\Program Files\PostgreSQL\16\bin\psql.exe",
    "C:\Program Files\PostgreSQL\15\bin\psql.exe",
    "C:\Program Files\PostgreSQL\14\bin\psql.exe"
)
foreach ($c in $candidates) {
    try {
        & $c --version 2>$null | Out-Null
        $psql = $c
        break
    } catch { }
}
if (-not $psql) {
    Write-Host "psql not found. Install PostgreSQL or add psql to PATH." -ForegroundColor Red
    exit 1
}
Write-Host "Using psql: $psql"

# --- Postgres password ---
if ([string]::IsNullOrEmpty($PostgresPassword)) {
    $sec = Read-Host "Password for postgres user" -AsSecureString
    $PostgresPassword = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [Runtime.InteropServices.Marshal]::SecureStringToBSTR($sec))
}
$env:PGPASSWORD = $PostgresPassword

# --- Create user ---
Write-Host "Creating user $DbUser..."
$sqlCreateUser = @"
DO `$`$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '$DbUser') THEN
        CREATE USER $DbUser WITH PASSWORD '$DbPassword';
    END IF;
END
`$`$;
"@
& $psql -h localhost -U postgres -d postgres -v ON_ERROR_STOP=1 -c $sqlCreateUser

# --- Create databases ---
foreach ($db in @($MainDb, $TestDb)) {
    Write-Host "Creating database $db..."
    $sqlCreateDb = "SELECT 'CREATE DATABASE $db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$db')\gexec"
    & $psql -h localhost -U postgres -d postgres -v ON_ERROR_STOP=1 -c $sqlCreateDb

    Write-Host "Granting privileges on $db..."
    $sqlGrant = @"
GRANT ALL PRIVILEGES ON DATABASE $db TO $DbUser;
GRANT ALL ON SCHEMA public TO $DbUser;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO $DbUser;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO $DbUser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO $DbUser;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO $DbUser;
"@
    & $psql -h localhost -U postgres -d $db -v ON_ERROR_STOP=1 -c $sqlGrant
}

# --- Done ---
$mainConn = "Host=localhost;Database=$MainDb;Username=$DbUser;Password=$DbPassword"
$testConn = "Host=localhost;Database=$TestDb;Username=$DbUser;Password=$DbPassword"

Write-Host ""
Write-Host "Done. Set environment variables in this session:" -ForegroundColor Green
Write-Host ""
Write-Host "  `$env:PICKUP_DB      = `"$mainConn`""
Write-Host "  `$env:PICKUP_DB_TEST = `"$testConn`""
Write-Host ""