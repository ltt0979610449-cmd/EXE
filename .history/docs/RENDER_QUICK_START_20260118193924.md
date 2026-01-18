# Quick Start Guide - Deploy lên Render

## Tóm tắt các bước tiếp theo

### ✅ Đã hoàn thành
- [x] Tạo file `render.yaml` với cấu hình services
- [x] Cập nhật `application-prod.yaml` để hỗ trợ Render
- [x] Tối ưu Dockerfile cho production
- [x] Tạo build script
- [x] Tạo documentation đầy đủ
- [x] Setup Flyway migrations (optional)

### 📋 Các bước tiếp theo cần thực hiện

## Bước 1: Quyết định Database Strategy

Render chủ yếu hỗ trợ **PostgreSQL**, không phải MySQL. Bạn có 2 lựa chọn:

### Option A: Migrate sang PostgreSQL (Recommended nếu muốn dùng Render database)

**Cần làm:**
1. Thay đổi dependency trong `pom.xml`:
   ```xml
   <!-- Thay thế -->
   <dependency>
       <groupId>com.mysql</groupId>
       <artifactId>mysql-connector-j</artifactId>
   </dependency>
   
   <!-- Bằng -->
   <dependency>
       <groupId>org.postgresql</groupId>
       <artifactId>postgresql</artifactId>
       <scope>runtime</scope>
   </dependency>
   ```

2. Update `application-prod.yaml`:
   ```yaml
   spring:
     jpa:
       database-platform: org.hibernate.dialect.PostgreSQLDialect
       properties:
         hibernate:
           dialect: org.hibernate.dialect.PostgreSQLDialect
   ```

3. Test locally với PostgreSQL

### Option B: Sử dụng External MySQL Service (Giữ nguyên code)

**Các options:**
- **PlanetScale** (Recommended): Free tier, serverless MySQL
  - Website: https://planetscale.com
  - Dễ setup, có free tier
- **Railway**: MySQL support, free tier
  - Website: https://railway.app
- **AWS RDS**: Pay-as-you-go
- **DigitalOcean Managed Database**: $15/month

**Cần làm:**
1. Tạo MySQL database trên service đã chọn
2. Lưu connection details
3. Set environment variables trong Render (xem Bước 3)

## Bước 2: Chuẩn bị Repository

1. **Commit và push code lên GitHub/GitLab:**
   ```bash
   git add .
   git commit -m "Add Render deployment configuration"
   git push origin main
   ```

2. **Đảm bảo các file sau có trong repository:**
   - `render.yaml`
   - `Dockerfile`
   - `pom.xml`
   - `src/main/resources/application-prod.yaml`
   - Tất cả source code

## Bước 3: Tạo Render Account và Services

### 3.1. Đăng ký/Đăng nhập Render
- Truy cập: https://dashboard.render.com
- Đăng ký/đăng nhập bằng GitHub/GitLab account

### 3.2. Tạo Database Service

**Nếu chọn PostgreSQL:**
1. Click "New +" → "PostgreSQL"
2. Cấu hình:
   - Name: `coiviet-db`
   - Region: Singapore
   - Plan: Free (hoặc Starter $7/month)
3. Lưu lại connection details

**Nếu chọn External MySQL:**
- Skip bước này, sẽ set connection string thủ công

### 3.3. Tạo Web Service

1. Click "New +" → "Web Service"
2. Connect repository (GitHub/GitLab)
3. Chọn repository và branch
4. Cấu hình:
   - **Name**: `coiviet-api`
   - **Runtime**: Docker
   - **Dockerfile Path**: `./Dockerfile`
   - **Docker Context**: `.`
   - **Region**: Singapore (cùng region với database)
   - **Plan**: Free (hoặc Starter $7/month)
   - **Health Check Path**: `/actuator/health`

## Bước 4: Cấu hình Environment Variables

Trong Web Service settings → Environment, set các biến sau:

### Database (nếu dùng PostgreSQL trên Render)
Render tự động inject khi link database, nhưng cần format JDBC URL:

```
SPRING_PROFILES_ACTIVE=prod
DBMS_CONNECTION=jdbc:postgresql://${DATABASE_HOST}:${DATABASE_PORT}/${DATABASE_NAME}?sslmode=require
DBMS_USERNAME=${DATABASE_USER}
DBMS_PASSWORD=${DATABASE_PASSWORD}
```

**Hoặc nếu dùng External MySQL:**
```
DBMS_CONNECTION=jdbc:mysql://<your-mysql-host>:3306/coivietdb?useSSL=true&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true
DBMS_USERNAME=<your-username>
DBMS_PASSWORD=<your-password>
```

### Application
```
SPRING_PROFILES_ACTIVE=prod
```

### JWT
```
JWT_SIGNER_KEY=<generate-strong-random-key>
JWT_VALID_DURATION=86400
JWT_REFRESHABLE_DURATION=36000
```

**Tạo JWT_SIGNER_KEY:**
```bash
# Linux/Mac
openssl rand -base64 64

# Windows PowerShell
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
```

### Cloudinary
```
CLOUDINARY_CLOUD_NAME=<your-cloud-name>
CLOUDINARY_API_KEY=<your-api-key>
CLOUDINARY_API_SECRET=<your-api-secret>
```

### Email (Gmail)
```
MAIL_USERNAME=<your-email@gmail.com>
MAIL_PASSWORD=<your-app-password>
```

**Lưu ý**: Cần tạo App Password trong Google Account settings, không dùng password thường.

### MoMo Payment
```
MOMO_PARTNER_CODE=<your-partner-code>
MOMO_ACCESS_KEY=<your-access-key>
MOMO_SECRET_KEY=<your-secret-key>
MOMO_REDIRECT_URL=https://coiviet-api.onrender.com/api/public/payment/momo-return
MOMO_NOTIFY_URL=https://coiviet-api.onrender.com/api/public/payment/momo-notify
```

**Lưu ý**: Thay `coiviet-api.onrender.com` bằng URL thực tế sau khi deploy.

### Google OAuth2
```
GOOGLE_CLIENT_ID=<your-client-id>
GOOGLE_CLIENT_SECRET=<your-client-secret>
GOOGLE_REDIRECT_URI=https://coiviet-api.onrender.com/login/oauth2/code/google
OAUTH2_REDIRECT_SUCCESS=https://your-frontend-domain.com/oauth2/callback
```

**Cần update Google Cloud Console:**
1. Vào https://console.cloud.google.com
2. APIs & Services → Credentials
3. Chỉnh sửa OAuth 2.0 Client
4. Thêm vào "Authorized redirect URIs":
   - `https://coiviet-api.onrender.com/login/oauth2/code/google`

### Admin
```
INITIAL_ADMIN_PASSWORD=<secure-password>
```

## Bước 5: Link Database với Web Service

1. Trong Web Service settings
2. Tìm phần "Environment" hoặc "Linked Services"
3. Click "Link Database" hoặc "Add Database"
4. Chọn database service đã tạo
5. Render sẽ tự động inject database variables

## Bước 6: Deploy

### Option 1: Sử dụng render.yaml (Auto-deploy)

1. Đảm bảo `render.yaml` đã có trong repository
2. Render sẽ tự động detect và tạo services từ file này
3. Có thể cần điều chỉnh một số giá trị

### Option 2: Manual Deploy

1. Sau khi set tất cả environment variables
2. Click "Manual Deploy" → "Deploy latest commit"
3. Hoặc push code mới lên repository (nếu enable auto-deploy)

## Bước 7: Verify Deployment

1. **Kiểm tra Build Logs:**
   - Xem logs trong Render Dashboard
   - Đảm bảo build thành công

2. **Kiểm tra Health Check:**
   - URL: `https://coiviet-api.onrender.com/actuator/health`
   - Phải trả về `{"status":"UP"}`

3. **Test API Endpoints:**
   - Swagger UI: `https://coiviet-api.onrender.com/swagger-ui.html`
   - Test các endpoints chính

4. **Kiểm tra Database Connection:**
   - Xem application logs
   - Đảm bảo không có lỗi connection

5. **Update URLs:**
   - Update MoMo redirect/notify URLs với URL thực tế
   - Update Google OAuth2 redirect URI
   - Update frontend callback URLs

## Bước 8: Post-Deployment

1. **Custom Domain (Optional):**
   - Thêm custom domain trong Render Dashboard
   - Update DNS records
   - Update các URLs trong environment variables

2. **Monitoring:**
   - Xem metrics trong Render Dashboard
   - Setup alerts nếu cần

3. **Backup:**
   - Setup database backups (nếu dùng Render database)
   - Hoặc configure backups trên external database service

## Troubleshooting

### Build Fails
- Xem build logs để tìm lỗi cụ thể
- Kiểm tra Java version (cần Java 21)
- Kiểm tra Maven dependencies có thể download không

### Database Connection Fails
- Verify connection string format
- Kiểm tra database service đã running chưa
- Verify username/password
- Kiểm tra firewall/network settings
- Với PostgreSQL: đảm bảo có `sslmode=require`
- Với MySQL: đảm bảo có `useSSL=true`

### Health Check Fails
- Kiểm tra `/actuator/health` endpoint accessible
- Verify `management.endpoints.web.exposure.include=health` trong config
- Xem application logs

### App Crashes on Startup
- Xem application logs
- Kiểm tra environment variables đã set đầy đủ chưa
- Verify database connection
- Kiểm tra port configuration

## Checklist trước khi Deploy

- [ ] Đã quyết định database strategy (PostgreSQL hoặc External MySQL)
- [ ] Đã tạo database service (nếu dùng Render)
- [ ] Đã tạo Web Service trên Render
- [ ] Đã set tất cả environment variables
- [ ] Đã link database với Web Service (nếu dùng Render database)
- [ ] Đã update Google OAuth2 redirect URI
- [ ] Đã chuẩn bị tất cả credentials (JWT, Cloudinary, Email, MoMo, etc.)
- [ ] Code đã được push lên repository
- [ ] Đã test build locally (nếu có thể)

## Tài liệu tham khảo

- **Chi tiết đầy đủ**: Xem `docs/RENDER_DEPLOYMENT.md`
- **Environment Variables**: Xem `docs/RENDER_DEPLOYMENT.md` phần "Environment Variables"
- **Render Documentation**: https://render.com/docs
- **Troubleshooting**: Xem `docs/RENDER_DEPLOYMENT.md` phần "Troubleshooting"

## Hỗ trợ

Nếu gặp vấn đề:
1. Xem logs trong Render Dashboard
2. Kiểm tra `docs/RENDER_DEPLOYMENT.md` phần Troubleshooting
3. Xem Render documentation: https://render.com/docs
