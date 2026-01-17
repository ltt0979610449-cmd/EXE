# Script tự động hóa deploy lên Railway
# Lưu ý: Railway không có CLI đầy đủ như Fly.io, một số bước cần làm thủ công

$ErrorActionPreference = "Stop"

Write-Host "🚂 Railway Auto Deploy Script" -ForegroundColor Cyan
Write-Host "===============================" -ForegroundColor Cyan

Write-Host "`n⚠️  Lưu ý: Railway không có CLI đầy đủ, một số bước cần làm thủ công trên dashboard" -ForegroundColor Yellow

Write-Host "`n📋 Checklist trước khi deploy:" -ForegroundColor Yellow
Write-Host "1. Đã đăng ký Railway: https://railway.app" -ForegroundColor White
Write-Host "2. Đã đăng nhập bằng GitHub" -ForegroundColor White
Write-Host "3. GitHub repo 'coiviet' đã public hoặc Railway có quyền truy cập" -ForegroundColor White

$continue = Read-Host "`nBạn đã hoàn thành các bước trên chưa? (y/n)"
if ($continue -ne "y" -and $continue -ne "Y") {
    Write-Host "`nVui lòng hoàn thành các bước trên trước khi tiếp tục." -ForegroundColor Red
    exit
}

Write-Host "`n📝 Hướng dẫn deploy từng bước:" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Gray

Write-Host "`n🔹 BƯỚC 1: Tạo Project trên Railway" -ForegroundColor Yellow
Write-Host "1. Vào https://railway.app/dashboard" -ForegroundColor White
Write-Host "2. Click 'New Project'" -ForegroundColor White
Write-Host "3. Chọn 'Deploy from GitHub repo'" -ForegroundColor White
Write-Host "4. Chọn repository 'coiviet'" -ForegroundColor White
Write-Host "5. Railway sẽ tự động detect Dockerfile và bắt đầu build" -ForegroundColor White

$step1 = Read-Host "`nĐã tạo project chưa? (y/n)"
if ($step1 -ne "y" -and $step1 -ne "Y") {
    Write-Host "Vui lòng tạo project trước. Script sẽ dừng ở đây." -ForegroundColor Red
    exit
}

Write-Host "`n🔹 BƯỚC 2: Thêm MySQL Database" -ForegroundColor Yellow
Write-Host "1. Trong project dashboard, click nút 'New' (màu xanh)" -ForegroundColor White
Write-Host "2. Chọn 'Database' → 'MySQL'" -ForegroundColor White
Write-Host "3. Railway sẽ tự động tạo MySQL instance" -ForegroundColor White
Write-Host "4. Đợi MySQL khởi động (khoảng 1-2 phút)" -ForegroundColor White

$step2 = Read-Host "`nĐã tạo MySQL database chưa? (y/n)"
if ($step2 -ne "y" -and $step2 -ne "Y") {
    Write-Host "Vui lòng tạo MySQL database trước. Script sẽ dừng ở đây." -ForegroundColor Red
    exit
}

Write-Host "`n🔹 BƯỚC 3: Lấy Database Connection Info" -ForegroundColor Yellow
Write-Host "1. Click vào MySQL service trong dashboard" -ForegroundColor White
Write-Host "2. Vào tab 'Variables'" -ForegroundColor White
Write-Host "3. Copy các giá trị: MYSQLHOST, MYSQLPORT, MYSQLDATABASE, MYSQLUSER, MYSQLPASSWORD" -ForegroundColor White

Write-Host "`n💡 Hoặc chạy script helper:" -ForegroundColor Cyan
Write-Host "   .\railway-db-helper.ps1" -ForegroundColor Gray

$runHelper = Read-Host "`nBạn muốn chạy helper script để tạo DB connection string không? (y/n)"
if ($runHelper -eq "y" -or $runHelper -eq "Y") {
    Write-Host "`nĐang mở helper script..." -ForegroundColor Cyan
    & ".\railway-db-helper.ps1"
}

Write-Host "`n🔹 BƯỚC 4: Set Environment Variables" -ForegroundColor Yellow
Write-Host "1. Vào Web Service (Spring Boot app) → Tab 'Variables'" -ForegroundColor White
Write-Host "2. Click 'New Variable' và thêm từng biến" -ForegroundColor White
Write-Host "3. Hoặc click 'Raw Editor' để paste nhiều biến cùng lúc" -ForegroundColor White

Write-Host "`n📄 File env vars đã được chuẩn bị sẵn:" -ForegroundColor Cyan
Write-Host "   RAILWAY_ENV_VARS_READY.txt" -ForegroundColor Gray
Write-Host "`nMở file này và copy các biến vào Railway dashboard" -ForegroundColor White

$openFile = Read-Host "`nBạn muốn mở file RAILWAY_ENV_VARS_READY.txt không? (y/n)"
if ($openFile -eq "y" -or $openFile -eq "Y") {
    notepad "RAILWAY_ENV_VARS_READY.txt"
}

Write-Host "`n⚠️  QUAN TRỌNG: Nhớ set database connection vars:" -ForegroundColor Yellow
Write-Host "   - DBMS_CONNECTION" -ForegroundColor Gray
Write-Host "   - DBMS_USERNAME" -ForegroundColor Gray
Write-Host "   - DBMS_PASSWORD" -ForegroundColor Gray
Write-Host "   (Dùng giá trị từ MySQL service variables)" -ForegroundColor Gray

$step4 = Read-Host "`nĐã set tất cả environment variables chưa? (y/n)"
if ($step4 -ne "y" -and $step4 -ne "Y") {
    Write-Host "Vui lòng set env vars trước. Script sẽ dừng ở đây." -ForegroundColor Red
    exit
}

Write-Host "`n🔹 BƯỚC 5: Generate Domain và Cập Nhật URLs" -ForegroundColor Yellow
Write-Host "1. Vào Web Service → Settings → Networking" -ForegroundColor White
Write-Host "2. Click 'Generate Domain' để tạo domain mặc định" -ForegroundColor White
Write-Host "3. Copy domain (ví dụ: coiviet-production.up.railway.app)" -ForegroundColor White
Write-Host "4. Quay lại Variables và cập nhật:" -ForegroundColor White
Write-Host "   - GOOGLE_REDIRECT_URI=https://[YOUR-DOMAIN]/login/oauth2/code/google" -ForegroundColor Gray
Write-Host "   - MOMO_REDIRECT_URL=https://[YOUR-DOMAIN]/api/public/payment/momo-return" -ForegroundColor Gray
Write-Host "   - MOMO_NOTIFY_URL=https://[YOUR-DOMAIN]/api/public/payment/momo-notify" -ForegroundColor Gray

$domain = Read-Host "`nNhập Railway domain của bạn (hoặc Enter để bỏ qua):"
if ($domain) {
    Write-Host "`n✅ Domain: $domain" -ForegroundColor Green
    Write-Host "`nCập nhật các biến sau trong Railway Variables:" -ForegroundColor Yellow
    Write-Host "GOOGLE_REDIRECT_URI=https://$domain/login/oauth2/code/google" -ForegroundColor Cyan
    Write-Host "MOMO_REDIRECT_URL=https://$domain/api/public/payment/momo-return" -ForegroundColor Cyan
    Write-Host "MOMO_NOTIFY_URL=https://$domain/api/public/payment/momo-notify" -ForegroundColor Cyan
}

Write-Host "`n🔹 BƯỚC 6: Deploy và Test" -ForegroundColor Yellow
Write-Host "1. Railway sẽ tự động deploy khi bạn set env vars" -ForegroundColor White
Write-Host "2. Hoặc click 'Deploy' trong dashboard để deploy lại" -ForegroundColor White
Write-Host "3. Check logs trong 'Deployments' tab" -ForegroundColor White
Write-Host "4. Test health endpoint: https://[YOUR-DOMAIN]/actuator/health" -ForegroundColor White
Write-Host "5. Test Swagger: https://[YOUR-DOMAIN]/swagger-ui.html" -ForegroundColor White

Write-Host "`n✅ Hoàn thành!" -ForegroundColor Green
Write-Host "`n📚 Tài liệu tham khảo:" -ForegroundColor Cyan
Write-Host "   - docs/RAILWAY_DEPLOY.md (hướng dẫn chi tiết)" -ForegroundColor Gray
Write-Host "   - RAILWAY_README.md (quick start)" -ForegroundColor Gray

Write-Host "`n🎉 Chúc bạn deploy thành công!" -ForegroundColor Green
