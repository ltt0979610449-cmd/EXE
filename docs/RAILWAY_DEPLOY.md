# Hướng Dẫn Deploy lên Railway.app

## 🚀 Tổng Quan

Railway.app là một platform deploy dễ dùng, hỗ trợ Docker và có MySQL database tích hợp. Free tier cung cấp $5 credit mỗi tháng.

## 📋 Yêu Cầu

1. Tài khoản GitHub (để đăng nhập Railway)
2. Đăng ký Railway tại https://railway.app

## 🎯 Các Bước Deploy

### Bước 1: Đăng Ký và Tạo Project

1. Truy cập https://railway.app
2. Click "Login" → Chọn "Login with GitHub"
3. Authorize Railway để truy cập GitHub repos
4. Click "New Project"
5. Chọn "Deploy from GitHub repo"
6. Chọn repository `coiviet`
7. Railway sẽ tự động detect `Dockerfile` và bắt đầu build

### Bước 2: Thêm MySQL Database

1. Trong project dashboard, click nút **"New"** (màu xanh)
2. Chọn **"Database"** → **"MySQL"**
3. Railway sẽ tự động:
   - Tạo MySQL instance
   - Set các environment variables:
     - `MYSQLHOST`
     - `MYSQLUSER`
     - `MYSQLPASSWORD`
     - `MYSQLDATABASE`
     - `MYSQLPORT`

### Bước 3: Cấu Hình Database Connection

Railway tự động tạo các biến `MYSQL*`, nhưng Spring Boot cần format khác. Bạn cần tạo các biến sau:

1. Vào **Web Service** (Spring Boot app) → **Variables** tab
2. Thêm các biến sau:

```bash
# Database Connection String
DBMS_CONNECTION=jdbc:mysql://${MYSQLHOST}:${MYSQLPORT}/${MYSQLDATABASE}?useSSL=true&serverTimezone=Asia/Ho_Chi_Minh

# Database Credentials
DBMS_USERNAME=${MYSQLUSER}
DBMS_PASSWORD=${MYSQLPASSWORD}
```

**Lưu ý**: Railway hỗ trợ variable reference, nhưng để chắc chắn, bạn có thể:
1. Vào MySQL service → **Variables** tab
2. Copy giá trị thực tế của `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD`
3. Tạo connection string thủ công:
   ```
   DBMS_CONNECTION=jdbc:mysql://[MYSQLHOST_VALUE]:[MYSQLPORT_VALUE]/[MYSQLDATABASE_VALUE]?useSSL=true&serverTimezone=Asia/Ho_Chi_Minh
   DBMS_USERNAME=[MYSQLUSER_VALUE]
   DBMS_PASSWORD=[MYSQLPASSWORD_VALUE]
   ```

### Bước 4: Set Environment Variables

Vào **Web Service** → **Variables** tab, thêm các biến sau:

#### Spring Configuration
```bash
SPRING_PROFILES_ACTIVE=prod
PORT=8080
```

#### JWT Configuration
```bash
JWT_SIGNER_KEY=your-secret-key-here
JWT_VALID_DURATION=86400000
JWT_REFRESHABLE_DURATION=604800000
```

#### Mail Configuration
```bash
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password
```

#### OAuth2 Google Configuration
```bash
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URI=https://your-app.railway.app/login/oauth2/code/google
OAUTH2_REDIRECT_SUCCESS=https://your-frontend-url.com/oauth2/callback
```

**Lưu ý**: Cập nhật `GOOGLE_REDIRECT_URI` sau khi có Railway domain.

#### MoMo Payment Configuration
```bash
MOMO_PARTNER_CODE=your-partner-code
MOMO_ACCESS_KEY=your-access-key
MOMO_SECRET_KEY=your-secret-key
MOMO_REDIRECT_URL=https://your-app.railway.app/api/public/payment/momo-return
MOMO_NOTIFY_URL=https://your-app.railway.app/api/public/payment/momo-notify
```

**Lưu ý**: Cập nhật URLs sau khi có Railway domain.

#### Cloudinary Configuration
```bash
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
```

#### Admin Configuration
```bash
INITIAL_ADMIN_PASSWORD=your-admin-password
```

### Bước 5: Lấy Domain và Cập Nhật URLs

1. Vào **Web Service** → **Settings** → **Networking**
2. Click **"Generate Domain"** để tạo domain mặc định (ví dụ: `coiviet-production.up.railway.app`)
3. Hoặc thêm **Custom Domain** của bạn
4. Cập nhật lại các URLs trong env vars:
   - `GOOGLE_REDIRECT_URI`
   - `MOMO_REDIRECT_URL`
   - `MOMO_NOTIFY_URL`
   - `OAUTH2_REDIRECT_SUCCESS` (nếu cần)

### Bước 6: Deploy

Railway sẽ tự động deploy khi:
- Push code lên GitHub
- Thay đổi environment variables
- Click nút **"Deploy"** trong dashboard

### Bước 7: Kiểm Tra

1. Vào **Web Service** → **Deployments** tab để xem logs
2. Check health endpoint: `https://your-app.railway.app/actuator/health`
3. Test API: `https://your-app.railway.app/swagger-ui.html`

## 🔧 Cấu Hình CORS và WebSocket

Sau khi có domain, cần update code để cho phép Railway domain:

### 1. Update SecurityConfig.java

Thêm Railway domain vào CORS:

```java
configuration.addAllowedOriginPattern("https://*.railway.app");
configuration.addAllowedOriginPattern("https://*.up.railway.app");
```

### 2. Update WebSocketConfig.java

Thêm Railway domain vào WebSocket:

```java
registry.addEndpoint("/ws")
    .setAllowedOriginPatterns(
        "http://localhost:[*]",
        "https://*.ngrok-free.app",
        "https://*.railway.app",
        "https://*.up.railway.app"
    )
    .withSockJS();
```

## 📊 Monitoring và Logs

- **Logs**: Vào **Web Service** → **Deployments** → Click vào deployment → Xem logs
- **Metrics**: Railway cung cấp metrics về CPU, Memory, Network
- **Health Checks**: Railway tự động check `/actuator/health`

## 💰 Pricing

- **Free Tier**: $5 credit/tháng
- **Starter Plan**: $5/tháng (nếu hết free credit)
- **Developer Plan**: $20/tháng

## 🐛 Troubleshooting

### App không start
- Check logs trong Railway dashboard
- Đảm bảo database connection đúng
- Check env vars đã set đầy đủ

### Database connection timeout
- Tăng `connection-timeout` trong `application-prod.yaml`
- Check MySQL service đã running chưa
- Verify connection string đúng format

### Port binding error
- Railway tự động set `PORT` env var
- App đã được config để nhận `PORT` (xem `application-prod.yaml`)

### Build failed
- Check Dockerfile có đúng không
- Check logs trong build process
- Đảm bảo Maven build thành công

## 📚 Tài Liệu Tham Khảo

- Railway Docs: https://docs.railway.app
- Railway Discord: https://discord.gg/railway
- Railway Status: https://status.railway.app

## ✅ Checklist Deploy

- [ ] Đăng ký Railway account
- [ ] Tạo project từ GitHub repo
- [ ] Thêm MySQL database
- [ ] Set database connection env vars
- [ ] Set tất cả environment variables
- [ ] Generate domain
- [ ] Cập nhật URLs trong env vars
- [ ] Update CORS và WebSocket config trong code
- [ ] Deploy và test
- [ ] Check health endpoint
- [ ] Test API endpoints
