# Hướng dẫn Deploy lên Render

## Tổng quan

Tài liệu này hướng dẫn deploy MySQL database và Spring Boot backend lên Render platform.

## Kiến trúc

- **MySQL Database**: Render PostgreSQL/MySQL service (hoặc có thể dùng external MySQL)
- **Backend API**: Spring Boot application deploy dưới dạng Docker container

## Bước 1: Tạo Database trên Render

### Option 1: Sử dụng PostgreSQL trên Render (Recommended)

Render chủ yếu hỗ trợ PostgreSQL, không phải MySQL. Nếu chọn option này:

1. Đăng nhập vào [Render Dashboard](https://dashboard.render.com)
2. Click "New +" → "PostgreSQL"
3. Cấu hình:
   - **Name**: `coiviet-db`
   - **Database**: Render tự động tạo
   - **Region**: Singapore (hoặc region gần nhất)
   - **Plan**: Starter ($7/month) hoặc Free tier (limited)
4. Lưu lại các thông tin connection (Render tự động tạo):
   - DATABASE_URL
   - DATABASE_HOST
   - DATABASE_PORT (thường 5432)
   - DATABASE_NAME
   - DATABASE_USER
   - DATABASE_PASSWORD

**Cần migrate code sang PostgreSQL:**
- Thay đổi dependency trong `pom.xml`: từ `mysql-connector-j` sang PostgreSQL driver
- Update `application-prod.yaml`: thay `MySQLDialect` thành `PostgreSQLDialect`
- Test migrations và queries

### Option 2: Sử dụng External MySQL Service

Nếu muốn giữ MySQL, sử dụng external service:

**Các options:**
- **PlanetScale**: Free tier available, serverless MySQL
- **AWS RDS**: Pay-as-you-go
- **DigitalOcean Managed Database**: $15/month
- **Railway**: MySQL support, free tier available

**Cấu hình:**
1. Tạo MySQL database trên service đã chọn
2. Lưu connection details
3. Set environment variables thủ công trong Render (xem Bước 3)

## Bước 2: Tạo Web Service cho Backend

1. Click "New +" → "Web Service"
2. Connect repository (GitHub/GitLab)
3. Cấu hình:
   - **Name**: `coiviet-api`
   - **Runtime**: Docker
   - **Dockerfile Path**: `./Dockerfile`
   - **Docker Context**: `.`
   - **Region**: Singapore (cùng region với database)
   - **Plan**: Free tier hoặc Starter ($7/month)
   - **Health Check Path**: `/actuator/health`

## Bước 3: Cấu hình Environment Variables

### Database Connection Variables

Render sẽ tự động tạo các biến sau khi link database service:
- `DATABASE_URL` - Full connection string
- `DATABASE_HOST` - Database hostname
- `DATABASE_PORT` - Database port (thường 3306)
- `DATABASE_NAME` - Database name
- `DATABASE_USER` - Database username
- `DATABASE_PASSWORD` - Database password

**Cần set thủ công trong Render Dashboard:**

#### Database Configuration

**Nếu dùng PostgreSQL trên Render:**
```
DBMS_CONNECTION=jdbc:postgresql://${DATABASE_HOST}:${DATABASE_PORT}/${DATABASE_NAME}?sslmode=require
DBMS_USERNAME=${DATABASE_USER}
DBMS_PASSWORD=${DATABASE_PASSWORD}
```

Render sẽ tự động inject các biến `DATABASE_*` khi link database service.

**Nếu dùng External MySQL:**
```
DBMS_CONNECTION=jdbc:mysql://<your-mysql-host>:3306/coivietdb?useSSL=true&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true
DBMS_USERNAME=coiviet_user
DBMS_PASSWORD=your_password_here
```

**Ví dụ với PlanetScale:**
```
DBMS_CONNECTION=jdbc:mysql://aws.connect.psdb.cloud/coivietdb?sslMode=REQUIRED&serverTimezone=Asia/Ho_Chi_Minh
DBMS_USERNAME=your_planetscale_user
DBMS_PASSWORD=your_planetscale_password
```

**Helper Scripts:**
- Xem `scripts/format-jdbc-url.sh` (Linux/Mac) hoặc `scripts/format-jdbc-url.ps1` (Windows) để format JDBC URL

#### Application Configuration
```
SPRING_PROFILES_ACTIVE=prod
PORT=8080
```
**Lưu ý**: `PORT` được Render tự động set, không cần set thủ công.

#### JWT Configuration
```
JWT_SIGNER_KEY=your_jwt_signer_key_here
JWT_VALID_DURATION=86400
JWT_REFRESHABLE_DURATION=36000
```

**Giải thích:**
- `JWT_SIGNER_KEY`: Secret key để sign JWT tokens (nên dùng strong random string)
- `JWT_VALID_DURATION`: Thời gian hiệu lực của token (giây) - 86400 = 24 giờ
- `JWT_REFRESHABLE_DURATION`: Thời gian có thể refresh token (giây) - 36000 = 10 giờ

#### Cloudinary Configuration
```
CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
CLOUDINARY_API_KEY=your_cloudinary_api_key
CLOUDINARY_API_SECRET=your_cloudinary_api_secret
```

#### Email Configuration (Gmail SMTP)
```
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```

**Lưu ý**: Với Gmail, cần sử dụng App Password, không phải password thường.

#### MoMo Payment Configuration
```
MOMO_PARTNER_CODE=your_momo_partner_code
MOMO_ACCESS_KEY=your_momo_access_key
MOMO_SECRET_KEY=your_momo_secret_key
MOMO_REDIRECT_URL=https://coiviet-api.onrender.com/api/public/payment/momo-return
MOMO_NOTIFY_URL=https://coiviet-api.onrender.com/api/public/payment/momo-notify
```

**Lưu ý**: 
- Thay `coiviet-api.onrender.com` bằng URL thực tế của bạn trên Render
- Cần update các URL này trong MoMo dashboard

#### Google OAuth2 Configuration
```
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
GOOGLE_REDIRECT_URI=https://coiviet-api.onrender.com/login/oauth2/code/google
OAUTH2_REDIRECT_SUCCESS=https://your-frontend-domain.com/oauth2/callback
```

**Lưu ý**:
- Thay `coiviet-api.onrender.com` bằng URL thực tế của bạn
- Cần thêm redirect URI vào Google Cloud Console:
  - Vào [Google Cloud Console](https://console.cloud.google.com)
  - APIs & Services → Credentials
  - Chỉnh sửa OAuth 2.0 Client
  - Thêm `https://coiviet-api.onrender.com/login/oauth2/code/google` vào Authorized redirect URIs

#### Admin Configuration
```
INITIAL_ADMIN_PASSWORD=your_secure_admin_password
```

**Lưu ý**: Password này sẽ được dùng để tạo admin user lần đầu (nếu có DataInitializer).

## Bước 4: Link Database với Web Service

1. Trong Web Service settings
2. Tìm phần "Environment" hoặc "Linked Services"
3. Link với MySQL database service đã tạo
4. Render sẽ tự động inject database connection variables

## Bước 5: Deploy

### Option 1: Sử dụng render.yaml (Recommended)

1. Đảm bảo file `render.yaml` đã có trong repository
2. Render sẽ tự động detect và sử dụng cấu hình từ file này
3. Có thể cần điều chỉnh một số giá trị trong file

### Option 2: Manual Setup

1. Tạo services thủ công từ Dashboard
2. Set environment variables như hướng dẫn ở trên
3. Deploy từ Dashboard

## Bước 6: Verify Deployment

1. Kiểm tra logs trong Render Dashboard
2. Test health endpoint: `https://coiviet-api.onrender.com/actuator/health`
3. Test API endpoints
4. Kiểm tra database connection

## Troubleshooting

### Lỗi Database Connection

**Vấn đề**: App không thể kết nối database

**Giải pháp**:
1. Kiểm tra `DBMS_CONNECTION` format đúng
2. Đảm bảo database service đã running
3. Kiểm tra firewall/network settings
4. Verify username/password đúng
5. Kiểm tra SSL settings (Render thường yêu cầu SSL)

### Lỗi Build

**Vấn đề**: Build fails trên Render

**Giải pháp**:
1. Kiểm tra Dockerfile syntax
2. Đảm bảo Maven dependencies có thể download
3. Kiểm tra Java version (cần Java 21)
4. Xem build logs để tìm lỗi cụ thể

### Lỗi Health Check

**Vấn đề**: Health check fails

**Giải pháp**:
1. Đảm bảo `/actuator/health` endpoint accessible
2. Kiểm tra `management.endpoints.web.exposure.include=health` trong config
3. Kiểm tra app đã start thành công chưa
4. Xem application logs

### Lỗi Port

**Vấn đề**: App không start hoặc port conflict

**Giải pháp**:
1. Đảm bảo `application-prod.yaml` có `server.port: ${PORT:8080}`
2. Render tự động set `PORT` environment variable
3. App phải listen trên port từ biến `PORT`

## Checklist trước khi Deploy

- [ ] MySQL database đã được tạo và running
- [ ] Tất cả environment variables đã được set
- [ ] Google OAuth2 redirect URI đã được update
- [ ] MoMo payment URLs đã được update
- [ ] Frontend callback URLs đã được update
- [ ] Database connection string format đúng
- [ ] Health check endpoint accessible
- [ ] Dockerfile đã được test locally (nếu có thể)
- [ ] Application logs được monitor

## Cost Estimation

### Free Tier
- **Web Service**: 
  - Free tier có limitations (sleep sau 15 phút không có traffic)
  - Build time: ~5-10 phút
- **Database**: 
  - PostgreSQL free tier có limitations
  - MySQL có thể không có free tier, cần Starter plan ($7/month)

### Recommended for Production
- **Web Service**: Starter plan ($7/month) - không sleep, faster builds
- **Database**: Starter plan ($7/month) - better performance và reliability
- **Total**: ~$14/month

## Additional Resources

- [Render Documentation](https://render.com/docs)
- [Render Environment Variables](https://render.com/docs/environment-variables)
- [Render Health Checks](https://render.com/docs/health-checks)
- [Render Database Connections](https://render.com/docs/databases)

## Notes

1. **Database Type**: Render có thể chỉ cung cấp PostgreSQL thay vì MySQL. Nếu vậy:
   - Có thể migrate sang PostgreSQL
   - Hoặc sử dụng external MySQL service (PlanetScale, AWS RDS, DigitalOcean, etc.)

2. **Auto Deploy**: Render tự động deploy khi push code lên main branch (nếu enable auto-deploy)

3. **Custom Domain**: Có thể thêm custom domain trong Render Dashboard

4. **SSL**: Render tự động cung cấp SSL certificate cho tất cả services

5. **Logs**: Xem logs real-time trong Render Dashboard

6. **Metrics**: Render cung cấp basic metrics (CPU, Memory, Requests) trong Dashboard
