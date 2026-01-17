# Script helper để tạo Database Connection String cho Railway
# Chạy script này sau khi đã tạo MySQL service trên Railway

Write-Host "🔗 Railway Database Connection Helper" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan

Write-Host "`n📋 Hướng dẫn:" -ForegroundColor Yellow
Write-Host "1. Vào Railway Dashboard → MySQL Service → Variables tab" -ForegroundColor White
Write-Host "2. Copy các giá trị sau:" -ForegroundColor White
Write-Host "   - MYSQLHOST" -ForegroundColor Gray
Write-Host "   - MYSQLPORT" -ForegroundColor Gray
Write-Host "   - MYSQLDATABASE" -ForegroundColor Gray
Write-Host "   - MYSQLUSER" -ForegroundColor Gray
Write-Host "   - MYSQLPASSWORD" -ForegroundColor Gray

Write-Host "`n3. Nhập các giá trị vào dưới đây:" -ForegroundColor White

# Nhập các giá trị
$mysqlHost = Read-Host "`nMYSQLHOST"
$mysqlPort = Read-Host "MYSQLPORT"
$mysqlDatabase = Read-Host "MYSQLDATABASE"
$mysqlUser = Read-Host "MYSQLUSER"
$mysqlPassword = Read-Host "MYSQLPASSWORD" -AsSecureString
$mysqlPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($mysqlPassword))

# Tạo connection string
$connectionString = "jdbc:mysql://${mysqlHost}:${mysqlPort}/${mysqlDatabase}?useSSL=true&serverTimezone=Asia/Ho_Chi_Minh"

Write-Host "`n✅ Environment Variables cần set trong Railway:" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Gray
Write-Host ""
Write-Host "DBMS_CONNECTION=$connectionString" -ForegroundColor Cyan
Write-Host "DBMS_USERNAME=$mysqlUser" -ForegroundColor Cyan
Write-Host "DBMS_PASSWORD=$mysqlPasswordPlain" -ForegroundColor Cyan
Write-Host ""

Write-Host "💡 Copy 3 dòng trên và paste vào Railway Dashboard → Web Service → Variables" -ForegroundColor Yellow
