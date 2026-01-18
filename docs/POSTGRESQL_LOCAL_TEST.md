# Hướng dẫn Test Local với PostgreSQL

## Tổng quan

Hướng dẫn này sẽ giúp bạn setup và test PostgreSQL local để verify migration trước khi deploy lên Render.

## Bước 1: Cài đặt PostgreSQL

### Option 1: Sử dụng Chocolatey (Recommended cho Windows)

```powershell
# Cài Chocolatey nếu chưa có
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Cài PostgreSQL
choco install postgresql -y
```

### Option 2: Download từ Website

1. Truy cập: https://www.postgresql.org/download/windows/
2. Download PostgreSQL installer
3. Chạy installer và làm theo hướng dẫn
4. Nhớ password cho user `postgres` (admin user)

### Option 3: Sử dụng Docker (Nếu đã có Docker)

```bash
# Chạy PostgreSQL container
docker run --name postgres-coiviet `
  -e POSTGRES_PASSWORD=postgres `
  -e POSTGRES_DB=coivietdb `
  -p 5432:5432 `
  -d postgres:16

# Tạo user và grant privileges
docker exec -it postgres-coiviet psql -U postgres -c "CREATE USER coiviet_user WITH PASSWORD 'coiviet_password';"
docker exec -it postgres-coiviet psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE coivietdb TO coiviet_user;"
```

## Bước 2: Setup Database (Tự động)

Sử dụng script tự động:

```powershell
cd c:\CN8\EXE2\coiviet\coiviet
.\scripts\setup-postgresql-local.ps1
```

Script sẽ:
- Kiểm tra PostgreSQL đã được cài đặt
- Tạo database `coivietdb`
- Tạo user `coiviet_user`
- Grant privileges
- Tạo file `.env.example` với cấu hình

## Bước 3: Setup Database (Thủ công)

Nếu muốn setup thủ công:

### 3.1. Kết nối PostgreSQL

```powershell
# Sử dụng psql command line
psql -U postgres

# Hoặc nếu có password prompt
psql -U postgres -h localhost
```

### 3.2. Tạo Database và User

```sql
-- Tạo database
CREATE DATABASE coivietdb;

-- Tạo user
CREATE USER coiviet_user WITH PASSWORD 'your_secure_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE coivietdb TO coiviet_user;

-- Connect to database và grant schema privileges
\c coivietdb
GRANT ALL ON SCHEMA public TO coiviet_user;
```

### 3.3. Verify Setup

```sql
-- Test connection với user mới
\c coivietdb coiviet_user

-- Kiểm tra privileges
\du

-- List databases
\l
```

## Bước 4: Cấu hình Application

### 4.1. Tạo file .env

Copy từ `.env.example` (nếu có) hoặc tạo mới:

```env
# PostgreSQL Database Configuration
DBMS_CONNECTION=jdbc:postgresql://localhost:5432/coivietdb
DBMS_USERNAME=coiviet_user
DBMS_PASSWORD=your_secure_password

# Spring Profile
SPRING_PROFILES_ACTIVE=default

# JWT Configuration (tạm thời cho testing)
JWT_SIGNER_KEY=test_jwt_key_for_local_development_only
JWT_VALID_DURATION=86400
JWT_REFRESHABLE_DURATION=36000

# Other required variables (set dummy values for local testing)
CLOUDINARY_CLOUD_NAME=test
CLOUDINARY_API_KEY=test
CLOUDINARY_API_SECRET=test
MAIL_USERNAME=test@test.com
MAIL_PASSWORD=test
MOMO_PARTNER_CODE=test
MOMO_ACCESS_KEY=test
MOMO_SECRET_KEY=test
MOMO_REDIRECT_URL=http://localhost:8080/api/public/payment/momo-return
MOMO_NOTIFY_URL=http://localhost:8080/api/public/payment/momo-notify
GOOGLE_CLIENT_ID=test
GOOGLE_CLIENT_SECRET=test
GOOGLE_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google
OAUTH2_REDIRECT_SUCCESS=http://localhost:3000/oauth2/callback
INITIAL_ADMIN_PASSWORD=admin123
```

### 4.2. Test Connection

Sử dụng script test:

```powershell
.\scripts\test-postgresql-connection.ps1
```

Hoặc test thủ công:

```powershell
$env:PGPASSWORD="your_password"
psql -h localhost -U coiviet_user -d coivietdb -c "SELECT version();"
```

## Bước 5: Build và Run Application

### 5.1. Build Project

```powershell
mvn clean package -DskipTests
```

### 5.2. Run Application

```powershell
java -jar target/coiviet-0.0.1-SNAPSHOT.jar
```

Hoặc với Maven:

```powershell
mvn spring-boot:run
```

### 5.3. Verify Application

1. **Kiểm tra logs:**
   - App khởi động thành công
   - Không có lỗi database connection
   - Schema được tạo tự động (nếu dùng `ddl-auto: update`)

2. **Test Health Endpoint:**
   ```powershell
   curl http://localhost:8080/actuator/health
   ```
   Kết quả mong đợi:
   ```json
   {
     "status": "UP",
     "components": {
       "db": {
         "status": "UP"
       }
     }
   }
   ```

3. **Test API Endpoints:**
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Test các endpoints chính

## Bước 6: Verify Database Schema

### 6.1. Kiểm tra Tables đã được tạo

```sql
-- Connect to database
psql -U coiviet_user -d coivietdb

-- List tables
\dt

-- Xem structure của một table
\d users

-- Count records (nếu có data)
SELECT COUNT(*) FROM users;
```

### 6.2. Test CRUD Operations

Thông qua API hoặc trực tiếp trong database:

```sql
-- Insert test data
INSERT INTO users (username, email, password_hash, full_name, role, status, created_at)
VALUES ('testuser', 'test@test.com', 'hashed_password', 'Test User', 'USER', 'ACTIVE', NOW());

-- Select data
SELECT * FROM users;

-- Update data
UPDATE users SET full_name = 'Updated Name' WHERE username = 'testuser';

-- Delete test data
DELETE FROM users WHERE username = 'testuser';
```

## Troubleshooting

### Lỗi: "psql: command not found"

**Nguyên nhân:** PostgreSQL chưa được cài đặt hoặc không có trong PATH

**Giải pháp:**
1. Cài đặt PostgreSQL (xem Bước 1)
2. Thêm PostgreSQL bin vào PATH:
   ```powershell
   # Thường là: C:\Program Files\PostgreSQL\16\bin
   $env:Path += ";C:\Program Files\PostgreSQL\16\bin"
   ```

### Lỗi: "password authentication failed"

**Nguyên nhân:** Username hoặc password sai

**Giải pháp:**
1. Verify username và password trong `.env`
2. Reset password nếu cần:
   ```sql
   ALTER USER coiviet_user WITH PASSWORD 'new_password';
   ```

### Lỗi: "database does not exist"

**Nguyên nhân:** Database chưa được tạo

**Giải pháp:**
```sql
CREATE DATABASE coivietdb;
```

### Lỗi: "permission denied"

**Nguyên nhân:** User không có quyền truy cập database

**Giải pháp:**
```sql
GRANT ALL PRIVILEGES ON DATABASE coivietdb TO coiviet_user;
\c coivietdb
GRANT ALL ON SCHEMA public TO coiviet_user;
```

### Lỗi: "Connection refused"

**Nguyên nhân:** PostgreSQL service chưa chạy

**Giải pháp:**
```powershell
# Start PostgreSQL service
net start postgresql-x64-16

# Hoặc qua Services
services.msc
# Tìm và start "postgresql-x64-16"
```

### Lỗi: "port 5432 already in use"

**Nguyên nhân:** Port 5432 đã được sử dụng

**Giải pháp:**
1. Tìm process đang dùng port:
   ```powershell
   netstat -ano | findstr :5432
   ```
2. Stop process hoặc đổi port PostgreSQL

## Checklist

- [ ] PostgreSQL đã được cài đặt
- [ ] Database `coivietdb` đã được tạo
- [ ] User `coiviet_user` đã được tạo và có privileges
- [ ] File `.env` đã được tạo với đúng cấu hình
- [ ] Connection test thành công
- [ ] Application build thành công
- [ ] Application khởi động thành công
- [ ] Database schema được tạo tự động
- [ ] Health endpoint trả về status UP
- [ ] API endpoints hoạt động đúng

## Next Steps

Sau khi test local thành công:

1. **Commit changes:**
   ```bash
   git add .
   git commit -m "Migrate to PostgreSQL - tested locally"
   git push origin main
   ```

2. **Deploy lên Render:**
   - Xem `docs/RENDER_QUICK_START.md`
   - Tạo PostgreSQL database trên Render
   - Deploy web service

## Resources

- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [PostgreSQL Windows Installation](https://www.postgresql.org/download/windows/)
- [Spring Boot PostgreSQL](https://spring.io/guides/gs/accessing-data-mysql/)
