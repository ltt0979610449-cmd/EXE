# Script hướng dẫn deploy lên Railway.app
# Railway không có CLI để set env vars tự động, cần set thủ công trên dashboard

$ErrorActionPreference = "Stop"

Write-Host "🚂 Hướng dẫn Deploy lên Railway.app" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

Write-Host "`n📋 Các bước deploy:" -ForegroundColor Yellow
Write-Host "1. Đăng ký tại https://railway.app (đăng nhập bằng GitHub)" -ForegroundColor White
Write-Host "2. Tạo New Project → Deploy from GitHub repo" -ForegroundColor White
Write-Host "3. Chọn repo 'coiviet'" -ForegroundColor White
Write-Host "4. Railway sẽ tự động detect Dockerfile và deploy" -ForegroundColor White

Write-Host "`n📦 Thêm MySQL Database:" -ForegroundColor Yellow
Write-Host "1. Trong project, click 'New' → 'Database' → 'MySQL'" -ForegroundColor White
Write-Host "2. Railway tự động tạo database và set env vars:" -ForegroundColor White
Write-Host "   - MYSQLHOST" -ForegroundColor Gray
Write-Host "   - MYSQLUSER" -ForegroundColor Gray
Write-Host "   - MYSQLPASSWORD" -ForegroundColor Gray
Write-Host "   - MYSQLDATABASE" -ForegroundColor Gray
Write-Host "   - MYSQLPORT" -ForegroundColor Gray

Write-Host "`n⚙️  Set Environment Variables:" -ForegroundColor Yellow
Write-Host "Trong service settings → Variables, thêm các biến sau:" -ForegroundColor White

Write-Host "`n📝 Danh sách Environment Variables cần set:" -ForegroundColor Cyan
Write-Host "----------------------------------------" -ForegroundColor Gray

$envVars = @"
# Spring Profile
SPRING_PROFILES_ACTIVE=prod
PORT=8080

# Database (Railway tự set MYSQL* vars, cần convert sang format Spring Boot)
# Lưu ý: Railway set MYSQLHOST, MYSQLUSER, etc. nhưng Spring Boot cần DBMS_CONNECTION
# Có thể dùng: jdbc:mysql://`${MYSQLHOST}:`${MYSQLPORT}/`${MYSQLDATABASE}?useSSL=true&serverTimezone=Asia/Ho_Chi_Minh
# Hoặc set trực tiếp connection string sau khi có MySQL service URL

# JWT
JWT_SIGNER_KEY=your-secret-key-here
JWT_VALID_DURATION=86400000
JWT_REFRESHABLE_DURATION=604800000

# Mail
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# OAuth2 Google
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URI=https://your-app.railway.app/login/oauth2/code/google
OAUTH2_REDIRECT_SUCCESS=https://your-frontend-url.com/oauth2/callback

# MoMo Payment
MOMO_PARTNER_CODE=your-partner-code
MOMO_ACCESS_KEY=your-access-key
MOMO_SECRET_KEY=your-secret-key
MOMO_REDIRECT_URL=https://your-app.railway.app/api/public/payment/momo-return
MOMO_NOTIFY_URL=https://your-app.railway.app/api/public/payment/momo-notify

# Cloudinary
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret

# Admin
INITIAL_ADMIN_PASSWORD=your-admin-password
"@

Write-Host $envVars -ForegroundColor White

Write-Host "`n💡 Lưu ý về Database Connection:" -ForegroundColor Yellow
Write-Host "Railway tự động tạo MySQL và set các biến MYSQLHOST, MYSQLUSER, etc." -ForegroundColor White
Write-Host "Bạn cần tạo biến DBMS_CONNECTION với format:" -ForegroundColor White
Write-Host "  jdbc:mysql://`${MYSQLHOST}:`${MYSQLPORT}/`${MYSQLDATABASE}?useSSL=true&serverTimezone=Asia/Ho_Chi_Minh" -ForegroundColor Gray
Write-Host "Và set DBMS_USERNAME=`${MYSQLUSER}, DBMS_PASSWORD=`${MYSQLPASSWORD}" -ForegroundColor Gray

Write-Host "`n🔗 Lấy MySQL Connection String:" -ForegroundColor Yellow
Write-Host "1. Click vào MySQL service trong Railway dashboard" -ForegroundColor White
Write-Host "2. Vào tab 'Connect' hoặc 'Variables'" -ForegroundColor White
Write-Host "3. Copy các giá trị MYSQLHOST, MYSQLPORT, MYSQLDATABASE, etc." -ForegroundColor White
Write-Host "4. Tạo connection string từ các giá trị đó" -ForegroundColor White

Write-Host "`n🌐 Custom Domain (Optional):" -ForegroundColor Yellow
Write-Host "1. Vào service settings → 'Networking'" -ForegroundColor White
Write-Host "2. Click 'Generate Domain' để có domain mặc định" -ForegroundColor White
Write-Host "3. Hoặc thêm custom domain của bạn" -ForegroundColor White

Write-Host "`n✅ Sau khi set xong env vars:" -ForegroundColor Green
Write-Host "- Railway sẽ tự động redeploy" -ForegroundColor White
Write-Host "- Hoặc click 'Deploy' để deploy lại" -ForegroundColor White
Write-Host "- Check logs trong Railway dashboard để xem kết quả" -ForegroundColor White

Write-Host "`n📚 Tài liệu tham khảo:" -ForegroundColor Cyan
Write-Host "- Railway Docs: https://docs.railway.app" -ForegroundColor Gray
Write-Host "- Railway Discord: https://discord.gg/railway" -ForegroundColor Gray

Write-Host "`n✨ Chúc bạn deploy thành công!" -ForegroundColor Green
