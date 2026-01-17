# Script tự động deploy MySQL và Spring Boot lên Fly.io
# Chạy script này sau khi đã add payment info trên Fly.io Dashboard

$ErrorActionPreference = "Stop"

# Add Fly CLI to PATH
$env:Path += ";$env:USERPROFILE\.fly\bin"

Write-Host "🚀 Bắt đầu deploy lên Fly.io..." -ForegroundColor Green

# ==========================================
# BƯỚC 1: TẠO VÀ DEPLOY MYSQL
# ==========================================
Write-Host "`n📦 Bước 1: Tạo MySQL Database..." -ForegroundColor Cyan

# Tạo MySQL app
Write-Host "Tạo MySQL app..." -ForegroundColor Yellow
fly apps create coiviet-mysql 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0 -and $LASTEXITCODE -ne 1) {
    Write-Host "⚠️  MySQL app có thể đã tồn tại hoặc có lỗi. Tiếp tục..." -ForegroundColor Yellow
}

# Tạo volume cho MySQL
Write-Host "Tạo volume cho MySQL (1GB, Singapore region)..." -ForegroundColor Yellow
fly volumes create coiviet_mysql_data --size 1 --region sin -a coiviet-mysql

# Set MySQL secrets
Write-Host "Set MySQL secrets..." -ForegroundColor Yellow
$mysqlRootPassword = "Coiviet@Root2024!" # Thay đổi password này
$mysqlPassword = "Coiviet@DB2024!" # Thay đổi password này
fly secrets set `
  MYSQL_ROOT_PASSWORD="$mysqlRootPassword" `
  MYSQL_PASSWORD="$mysqlPassword" `
  -a coiviet-mysql

Write-Host "✅ MySQL secrets đã được set!" -ForegroundColor Green
Write-Host "   MYSQL_ROOT_PASSWORD: $mysqlRootPassword" -ForegroundColor Gray
Write-Host "   MYSQL_PASSWORD: $mysqlPassword" -ForegroundColor Gray

# Deploy MySQL
Write-Host "`nDeploy MySQL..." -ForegroundColor Yellow
fly deploy -c fly.mysql.toml

Write-Host "✅ MySQL đã được deploy!" -ForegroundColor Green

# Đợi MySQL khởi động và kiểm tra health
Write-Host "Đợi MySQL khởi động và sẵn sàng..." -ForegroundColor Yellow
$maxRetries = 12  # 12 lần thử, mỗi lần 10 giây = 120 giây tối đa
$retryCount = 0
$mysqlReady = $false

while ($retryCount -lt $maxRetries -and -not $mysqlReady) {
    Start-Sleep -Seconds 10
    $retryCount++
    Write-Host "Kiểm tra MySQL lần $retryCount/$maxRetries..." -ForegroundColor Yellow
    
    # Kiểm tra MySQL status
    $status = fly status -a coiviet-mysql 2>&1
    if ($status -match "running|started") {
        Write-Host "✅ MySQL đã sẵn sàng!" -ForegroundColor Green
        $mysqlReady = $true
    } else {
        Write-Host "⏳ MySQL chưa sẵn sàng, đợi thêm..." -ForegroundColor Yellow
    }
}

if (-not $mysqlReady) {
    Write-Host "⚠️  Cảnh báo: MySQL có thể chưa sẵn sàng hoàn toàn. Tiếp tục deploy backend..." -ForegroundColor Yellow
}

# ==========================================
# BƯỚC 2: TẠO VÀ DEPLOY SPRING BOOT BACKEND
# ==========================================
Write-Host "`n☕ Bước 2: Tạo Spring Boot Backend..." -ForegroundColor Cyan

# Tạo backend app
Write-Host "Tạo backend app..." -ForegroundColor Yellow
fly apps create coiviet-api 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0 -and $LASTEXITCODE -ne 1) {
    Write-Host "⚠️  Backend app có thể đã tồn tại hoặc có lỗi. Tiếp tục..." -ForegroundColor Yellow
}

# Lấy URL của backend app (sẽ có sau khi deploy lần đầu)
$backendUrl = "https://coiviet-api.fly.dev"

# Set tất cả environment variables
Write-Host "Set environment variables cho backend..." -ForegroundColor Yellow

# Database connection string (sử dụng internal DNS của Fly.io)
# Thêm các tham số để retry connection và xử lý timeout tốt hơn
# Sử dụng single quotes để tránh PowerShell parse ký tự &
$dbConnection = 'jdbc:mysql://coiviet-mysql.internal:3306/coivietdb?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true&autoReconnect=true&failOverReadOnly=false&maxReconnects=10&initialTimeout=2&connectTimeout=30000&socketTimeout=30000'

fly secrets set `
  DBMS_CONNECTION="$dbConnection" `
  DBMS_USERNAME="coiviet_user" `
  DBMS_PASSWORD="$mysqlPassword" `
  MOMO_PARTNER_CODE="MOMOBKUN20180529" `
  MOMO_ACCESS_KEY="klm05TvNBzhg7h7j" `
  MOMO_SECRET_KEY="at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa" `
  MOMO_REDIRECT_URL="$backendUrl/api/public/payment/momo-return" `
  MOMO_NOTIFY_URL="$backendUrl/api/public/payment/momo-notify" `
  MAIL_USERNAME="truongltse180010@fpt.edu.vn" `
  MAIL_PASSWORD="zhvr axud xxnb jihr" `
  GOOGLE_CLIENT_ID="87846938671-76pcjrb3ucf7ngmkai7b2qni7uvrn9qt.apps.googleusercontent.com" `
  GOOGLE_CLIENT_SECRET="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" `
  GOOGLE_REDIRECT_URI="$backendUrl/login/oauth2/code/google" `
  JWT_SIGNER_KEY="3aF+lAiyA/tEAeeBtmlou0RwdTwXx0lU6SjH0MYBR7DRt9vyJzlv66uqnqHMP2NW" `
  JWT_VALID_DURATION="86400" `
  JWT_REFRESHABLE_DURATION="36000" `
  INITIAL_ADMIN_PASSWORD="admin123" `
  CLOUDINARY_CLOUD_NAME="dcs0lhrvh" `
  CLOUDINARY_API_KEY="718451452685618" `
  CLOUDINARY_API_SECRET="GXhU99xN-CpagV9OBgT6R2PipyQ" `
  OAUTH2_REDIRECT_SUCCESS="http://localhost:3000/oauth2/callback" `
  -a coiviet-api

Write-Host "✅ Environment variables đã được set!" -ForegroundColor Green

# Deploy backend
Write-Host ""
Write-Host 'Deploy Spring Boot backend...' -ForegroundColor Yellow
fly deploy

Write-Host ""
Write-Host 'DEPLOYMENT COMPLETED!' -ForegroundColor Green
