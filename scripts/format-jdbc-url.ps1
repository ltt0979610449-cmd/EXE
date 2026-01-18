# PowerShell script helper để format JDBC connection string
# Sử dụng khi cần set DBMS_CONNECTION thủ công trong Render Dashboard

Write-Host "JDBC URL Format Helper" -ForegroundColor Green
Write-Host "======================" -ForegroundColor Green
Write-Host ""

Write-Host "PostgreSQL (Render default):" -ForegroundColor Yellow
Write-Host "jdbc:postgresql://`${DATABASE_HOST}:`${DATABASE_PORT}/`${DATABASE_NAME}?sslmode=require"
Write-Host ""

Write-Host "MySQL (External service):" -ForegroundColor Yellow
Write-Host "jdbc:mysql://`${DATABASE_HOST}:`${DATABASE_PORT}/`${DATABASE_NAME}?useSSL=true&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true"
Write-Host ""

Write-Host "MySQL (với specific host):" -ForegroundColor Yellow
Write-Host "jdbc:mysql://<your-mysql-host>:3306/coivietdb?useSSL=true&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true"
Write-Host ""

Write-Host "Lưu ý: Thay <your-mysql-host> bằng hostname thực tế của MySQL service" -ForegroundColor Cyan
