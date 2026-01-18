# Script export environment variables cho Render Dashboard
# Copy output và paste vào Render Dashboard → Environment Variables

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Render Environment Variables" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Copy các dòng sau và paste vào Render Dashboard:" -ForegroundColor Yellow
Write-Host ""

# Database (sẽ được tự động từ linked database, nhưng cần format JDBC URL)
Write-Host "# Database Configuration" -ForegroundColor Green
Write-Host "SPRING_PROFILES_ACTIVE=prod"
Write-Host "DBMS_CONNECTION=jdbc:postgresql://`${DATABASE_HOST}:`${DATABASE_PORT}/`${DATABASE_NAME}?sslmode=require"
Write-Host "DBMS_USERNAME=`${DATABASE_USER}"
Write-Host "DBMS_PASSWORD=`${DATABASE_PASSWORD}"
Write-Host ""

# JWT
Write-Host "# JWT Configuration" -ForegroundColor Green
Write-Host "JWT_SIGNER_KEY=3aF+lAiyA/tEAeeBtmlou0RwdTwXx0lU6SjH0MYBR7DRt9vyJzlv66uqnqHMP2NW"
Write-Host "JWT_VALID_DURATION=86400"
Write-Host "JWT_REFRESHABLE_DURATION=36000"
Write-Host ""

# Cloudinary
Write-Host "# Cloudinary Configuration" -ForegroundColor Green
Write-Host "CLOUDINARY_CLOUD_NAME=dcs0lhrvh"
Write-Host "CLOUDINARY_API_KEY=718451452685618"
Write-Host "CLOUDINARY_API_SECRET=GXhU99xN-CpagV9OBgT6R2PipyQ"
Write-Host ""

# Email
Write-Host "# Email Configuration" -ForegroundColor Green
Write-Host "MAIL_USERNAME=truongltse180010@fpt.edu.vn"
Write-Host "MAIL_PASSWORD=zhvr axud xxnb jihr"
Write-Host ""

# MoMo Payment
Write-Host "# MoMo Payment Configuration" -ForegroundColor Green
Write-Host "MOMO_PARTNER_CODE=MOMOBKUN20180529"
Write-Host "MOMO_ACCESS_KEY=klm05TvNBzhg7h7j"
Write-Host "MOMO_SECRET_KEY=at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa"
Write-Host "MOMO_REDIRECT_URL=https://coiviet-api.onrender.com/api/public/payment/momo-return"
Write-Host "MOMO_NOTIFY_URL=https://coiviet-api.onrender.com/api/public/payment/momo-notify"
Write-Host ""

# Google OAuth2
Write-Host "# Google OAuth2 Configuration" -ForegroundColor Green
Write-Host "GOOGLE_CLIENT_ID=87846938671-76pcjrb3ucf7ngmkai7b2qni7uvrn9qt.apps.googleusercontent.com"
Write-Host "GOOGLE_CLIENT_SECRET=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"
Write-Host "GOOGLE_REDIRECT_URI=https://coiviet-api.onrender.com/login/oauth2/code/google"
Write-Host "OAUTH2_REDIRECT_SUCCESS=https://your-frontend-domain.com/oauth2/callback"
Write-Host ""

# Admin
Write-Host "# Admin Configuration" -ForegroundColor Green
Write-Host "INITIAL_ADMIN_PASSWORD=admin123"
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Lưu ý:" -ForegroundColor Yellow
Write-Host "1. Thay 'coiviet-api.onrender.com' bằng URL thực tế sau khi deploy" -ForegroundColor White
Write-Host "2. Update Google OAuth2 redirect URI trong Google Cloud Console" -ForegroundColor White
Write-Host "3. Update MoMo URLs trong MoMo dashboard" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
