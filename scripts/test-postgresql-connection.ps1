# Script test PostgreSQL connection

param(
    [string]$Host = "localhost",
    [int]$Port = 5432,
    [string]$Database = "coivietdb",
    [string]$Username = "coiviet_user",
    [string]$Password
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "PostgreSQL Connection Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if (-not $Password) {
    $Password = Read-Host "Enter password for user '$Username'" -AsSecureString
    $Password = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($Password))
}

Write-Host "Testing connection to:" -ForegroundColor Yellow
Write-Host "  Host: $Host" -ForegroundColor White
Write-Host "  Port: $Port" -ForegroundColor White
Write-Host "  Database: $Database" -ForegroundColor White
Write-Host "  Username: $Username" -ForegroundColor White
Write-Host ""

# Set PGPASSWORD
$env:PGPASSWORD = $Password

# Test connection
Write-Host "Connecting..." -ForegroundColor Yellow
$testQuery = "SELECT version(), current_database(), current_user;"
$result = psql -h $Host -p $Port -U $Username -d $Database -c $testQuery 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Connection successful!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Database Info:" -ForegroundColor Cyan
    Write-Host $result
    Write-Host ""
    
    # Test JDBC URL
    $jdbcUrl = "jdbc:postgresql://$Host`:$Port/$Database"
    Write-Host "JDBC URL: $jdbcUrl" -ForegroundColor Green
    Write-Host ""
    Write-Host "✅ Ready to use in application!" -ForegroundColor Green
} else {
    Write-Host "❌ Connection failed!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Error details:" -ForegroundColor Yellow
    Write-Host $result
    Write-Host ""
    Write-Host "Troubleshooting:" -ForegroundColor Yellow
    Write-Host "1. Verify PostgreSQL is running" -ForegroundColor White
    Write-Host "2. Check host, port, database name, username, and password" -ForegroundColor White
    Write-Host "3. Verify user has privileges on the database" -ForegroundColor White
}

# Clear password from environment
Remove-Item Env:\PGPASSWORD
