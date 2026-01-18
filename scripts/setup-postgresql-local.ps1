# Script setup PostgreSQL local cho testing
# Chạy script này với quyền Administrator

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "PostgreSQL Local Setup Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Kiểm tra PostgreSQL đã được cài đặt chưa
Write-Host "Checking PostgreSQL installation..." -ForegroundColor Yellow
$psqlPath = Get-Command psql -ErrorAction SilentlyContinue

if (-not $psqlPath) {
    Write-Host "PostgreSQL chưa được cài đặt hoặc không có trong PATH." -ForegroundColor Red
    Write-Host ""
    Write-Host "Các cách cài đặt PostgreSQL:" -ForegroundColor Yellow
    Write-Host "1. Sử dụng Chocolatey (Recommended):" -ForegroundColor Green
    Write-Host "   choco install postgresql" -ForegroundColor White
    Write-Host ""
    Write-Host "2. Download từ website:" -ForegroundColor Green
    Write-Host "   https://www.postgresql.org/download/windows/" -ForegroundColor White
    Write-Host ""
    Write-Host "3. Sử dụng Docker (Nếu đã có Docker):" -ForegroundColor Green
    Write-Host "   docker run --name postgres-coiviet -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=coivietdb -p 5432:5432 -d postgres:16" -ForegroundColor White
    Write-Host ""
    
    $installChoice = Read-Host "Bạn muốn cài đặt PostgreSQL ngay bây giờ? (y/n)"
    if ($installChoice -eq "y" -or $installChoice -eq "Y") {
        Write-Host "Đang kiểm tra Chocolatey..." -ForegroundColor Yellow
        $choco = Get-Command choco -ErrorAction SilentlyContinue
        if ($choco) {
            Write-Host "Sử dụng Chocolatey để cài đặt PostgreSQL..." -ForegroundColor Green
            choco install postgresql -y
        } else {
            Write-Host "Chocolatey chưa được cài đặt. Vui lòng:" -ForegroundColor Red
            Write-Host "1. Cài Chocolatey: https://chocolatey.org/install" -ForegroundColor Yellow
            Write-Host "2. Hoặc download PostgreSQL từ: https://www.postgresql.org/download/windows/" -ForegroundColor Yellow
            exit 1
        }
    } else {
        Write-Host "Vui lòng cài đặt PostgreSQL trước khi tiếp tục." -ForegroundColor Yellow
        exit 1
    }
}

Write-Host "PostgreSQL đã được cài đặt!" -ForegroundColor Green
Write-Host ""

# Lấy thông tin PostgreSQL
Write-Host "Nhập thông tin PostgreSQL:" -ForegroundColor Yellow
$pgHost = Read-Host "Host (default: localhost)"
if ([string]::IsNullOrWhiteSpace($pgHost)) { $pgHost = "localhost" }

$pgPort = Read-Host "Port (default: 5432)"
if ([string]::IsNullOrWhiteSpace($pgPort)) { $pgPort = "5432" }

$pgAdminUser = Read-Host "PostgreSQL Admin User (default: postgres)"
if ([string]::IsNullOrWhiteSpace($pgAdminUser)) { $pgAdminUser = "postgres" }

$pgAdminPassword = Read-Host "PostgreSQL Admin Password" -AsSecureString
$pgAdminPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($pgAdminPassword))

$dbName = Read-Host "Database Name (default: coivietdb)"
if ([string]::IsNullOrWhiteSpace($dbName)) { $dbName = "coivietdb" }

$dbUser = Read-Host "Database User (default: coiviet_user)"
if ([string]::IsNullOrWhiteSpace($dbUser)) { $dbUser = "coiviet_user" }

$dbPassword = Read-Host "Database User Password" -AsSecureString
$dbPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($dbPassword))

Write-Host ""
Write-Host "Đang tạo database và user..." -ForegroundColor Yellow

# Set PGPASSWORD environment variable
$env:PGPASSWORD = $pgAdminPasswordPlain

# Tạo database
$createDbQuery = "CREATE DATABASE $dbName;"
psql -h $pgHost -p $pgPort -U $pgAdminUser -d postgres -c $createDbQuery 2>&1 | Out-Null

if ($LASTEXITCODE -eq 0) {
    Write-Host "Database '$dbName' đã được tạo!" -ForegroundColor Green
} else {
    Write-Host "Database có thể đã tồn tại hoặc có lỗi. Tiếp tục..." -ForegroundColor Yellow
}

# Tạo user
$createUserQuery = "CREATE USER $dbUser WITH PASSWORD '$dbPasswordPlain';"
psql -h $pgHost -p $pgPort -U $pgAdminUser -d postgres -c $createUserQuery 2>&1 | Out-Null

if ($LASTEXITCODE -eq 0) {
    Write-Host "User '$dbUser' đã được tạo!" -ForegroundColor Green
} else {
    Write-Host "User có thể đã tồn tại hoặc có lỗi. Tiếp tục..." -ForegroundColor Yellow
}

# Grant privileges
$grantQuery = "GRANT ALL PRIVILEGES ON DATABASE $dbName TO $dbUser;"
psql -h $pgHost -p $pgPort -U $pgAdminUser -d postgres -c $grantQuery 2>&1 | Out-Null

# Grant schema privileges
$grantSchemaQuery = "GRANT ALL ON SCHEMA public TO $dbUser;"
psql -h $pgHost -p $pgPort -U $pgAdminUser -d $dbName -c $grantSchemaQuery 2>&1 | Out-Null

Write-Host "Privileges đã được grant!" -ForegroundColor Green
Write-Host ""

# Tạo file .env.example
Write-Host "Tạo file .env.example với cấu hình..." -ForegroundColor Yellow

$envContent = @"
# PostgreSQL Database Configuration
DBMS_CONNECTION=jdbc:postgresql://$pgHost`:$pgPort/$dbName
DBMS_USERNAME=$dbUser
DBMS_PASSWORD=$dbPasswordPlain

# Other environment variables (set these manually)
SPRING_PROFILES_ACTIVE=default
JWT_SIGNER_KEY=your_jwt_signer_key_here
JWT_VALID_DURATION=86400
JWT_REFRESHABLE_DURATION=36000
CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
CLOUDINARY_API_KEY=your_cloudinary_api_key
CLOUDINARY_API_SECRET=your_cloudinary_api_secret
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
MOMO_PARTNER_CODE=your_momo_partner_code
MOMO_ACCESS_KEY=your_momo_access_key
MOMO_SECRET_KEY=your_momo_secret_key
MOMO_REDIRECT_URL=http://localhost:8080/api/public/payment/momo-return
MOMO_NOTIFY_URL=http://localhost:8080/api/public/payment/momo-notify
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
GOOGLE_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google
OAUTH2_REDIRECT_SUCCESS=http://localhost:3000/oauth2/callback
INITIAL_ADMIN_PASSWORD=admin123
"@

$envContent | Out-File -FilePath ".env.example" -Encoding UTF8
Write-Host "File .env.example đã được tạo!" -ForegroundColor Green
Write-Host ""

# Test connection
Write-Host "Testing database connection..." -ForegroundColor Yellow
$env:PGPASSWORD = $dbPasswordPlain
$testQuery = "SELECT version();"
$result = psql -h $pgHost -p $pgPort -U $dbUser -d $dbName -c $testQuery 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Connection test thành công!" -ForegroundColor Green
    Write-Host $result
} else {
    Write-Host "❌ Connection test thất bại!" -ForegroundColor Red
    Write-Host $result
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Setup hoàn tất!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Các bước tiếp theo:" -ForegroundColor Yellow
Write-Host "1. Copy .env.example thành .env và điền các giá trị còn thiếu" -ForegroundColor White
Write-Host "2. Chạy: mvn clean package" -ForegroundColor White
Write-Host "3. Chạy: java -jar target/coiviet-0.0.1-SNAPSHOT.jar" -ForegroundColor White
Write-Host ""

# Clear password from environment
Remove-Item Env:\PGPASSWORD
