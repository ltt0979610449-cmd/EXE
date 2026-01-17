# Hướng Dẫn Deploy Nhanh

## 🚀 Railway.app (Khuyên dùng - Dễ nhất)

### Bước 1: Đăng ký
1. Vào https://railway.app
2. Đăng nhập bằng GitHub

### Bước 2: Tạo Project
1. Click "New Project"
2. Chọn "Deploy from GitHub repo"
3. Chọn repo `coiviet`

### Bước 3: Thêm MySQL Database
1. Trong project, click "New"
2. Chọn "Database" → "MySQL"
3. Railway sẽ tự tạo database và set env vars:
   - `MYSQLHOST`
   - `MYSQLUSER`
   - `MYSQLPASSWORD`
   - `MYSQLDATABASE`
   - `MYSQLPORT`

### Bước 4: Set Environment Variables
Trong service settings, thêm các env vars:

```bash
SPRING_PROFILES_ACTIVE=prod
PORT=8080

# Database (Railway tự set, nhưng cần convert sang format Spring Boot)
DBMS_CONNECTION=jdbc:mysql://${MYSQLHOST}:${MYSQLPORT}/${MYSQLDATABASE}?useSSL=true&serverTimezone=Asia/Ho_Chi_Minh
DBMS_USERNAME=${MYSQLUSER}
DBMS_PASSWORD=${MYSQLPASSWORD}

# JWT
JWT_SIGNER_KEY=your-secret-key
JWT_VALID_DURATION=86400000
JWT_REFRESHABLE_DURATION=604800000

# Mail
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URI=https://your-app.railway.app/login/oauth2/code/google
OAUTH2_REDIRECT_SUCCESS=https://your-frontend-url.com/oauth2/callback

# MoMo Payment
MOMO_PARTNER_CODE=your-partner-code
MOMO_ACCESS_KEY=your-access-key
MOMO_SECRET_KEY=your-secret-key
MOMO_REDIRECT_URL=https://your-app.railway.app/payment/momo/callback
MOMO_NOTIFY_URL=https://your-app.railway.app/payment/momo/notify

# Cloudinary
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret

# Admin
INITIAL_ADMIN_PASSWORD=your-admin-password
```

### Bước 5: Deploy
- Railway tự động deploy khi push code lên GitHub
- Hoặc click "Deploy" trong dashboard

### Bước 6: Lấy URL
- Railway tự tạo domain: `your-app.railway.app`
- Có thể thêm custom domain trong settings

---

## 🎨 Render.com

### Bước 1: Đăng ký
1. Vào https://render.com
2. Đăng nhập bằng GitHub

### Bước 2: Tạo Web Service
1. Click "New +" → "Web Service"
2. Connect GitHub repo
3. Chọn repo `coiviet`

### Bước 3: Cấu hình Build
- **Name**: `coiviet-api`
- **Environment**: `Docker`
- **Dockerfile Path**: `./Dockerfile`
- **Docker Context**: `.`
- **Start Command**: (để trống, dùng từ Dockerfile)

### Bước 4: Thêm PostgreSQL Database
1. Click "New +" → "PostgreSQL"
2. Chọn "Free" plan
3. Lưu connection string

**Lưu ý**: Render free tier chỉ có PostgreSQL, không có MySQL. Có 2 options:
- **Option 1**: Dùng PostgreSQL (cần đổi dialect trong code)
- **Option 2**: Dùng external MySQL (PlanetScale, Aiven free tier)

### Bước 5: Set Environment Variables
Tương tự Railway, nhưng database connection sẽ khác nếu dùng PostgreSQL.

### Bước 6: Deploy
- Render tự động deploy
- App sẽ sleep sau 15 phút không dùng (free tier)

---

## ☁️ Google Cloud Run

### Bước 1: Setup
```bash
# Install gcloud CLI
# https://cloud.google.com/sdk/docs/install

# Login
gcloud auth login

# Set project
gcloud config set project YOUR_PROJECT_ID
```

### Bước 2: Enable APIs
```bash
gcloud services enable cloudbuild.googleapis.com
gcloud services enable run.googleapis.com
gcloud services enable containerregistry.googleapis.com
```

### Bước 3: Build và Deploy
```bash
# Build image
gcloud builds submit --tag gcr.io/YOUR_PROJECT_ID/coiviet

# Deploy
gcloud run deploy coiviet-api \
  --image gcr.io/YOUR_PROJECT_ID/coiviet \
  --platform managed \
  --region asia-southeast1 \
  --allow-unauthenticated \
  --memory 512Mi \
  --cpu 1 \
  --port 8080 \
  --set-env-vars SPRING_PROFILES_ACTIVE=prod \
  --set-env-vars PORT=8080
```

### Bước 4: Set Environment Variables
```bash
gcloud run services update coiviet-api \
  --update-env-vars DBMS_CONNECTION=jdbc:mysql://... \
  --update-env-vars DBMS_USERNAME=... \
  # ... các env vars khác
```

Hoặc set trong Cloud Console → Cloud Run → Service → Variables & Secrets

### Bước 5: Setup Database
- Dùng Cloud SQL (có free tier) hoặc external MySQL

---

## 📝 Lưu Ý Quan Trọng

### 1. Database Connection
- **Railway**: Tự động tạo MySQL, dùng env vars `MYSQL*`
- **Render**: Chỉ có PostgreSQL free tier
- **Cloud Run**: Cần external database (Cloud SQL hoặc external)

### 2. Port Configuration
- App đã được config để nhận `PORT` env var
- Nếu platform không set `PORT`, mặc định dùng 8080

### 3. CORS Configuration
Cần update `SecurityConfig.java` để thêm domain mới:
```java
configuration.addAllowedOriginPattern("https://*.railway.app");
configuration.addAllowedOriginPattern("https://*.onrender.com");
configuration.addAllowedOriginPattern("https://*.run.app");
```

### 4. WebSocket
Cần update `WebSocketConfig.java` để thêm domain mới:
```java
registry.addEndpoint("/ws")
    .setAllowedOriginPatterns(
        "http://localhost:[*]",
        "https://*.ngrok-free.app",
        "https://*.railway.app",
        "https://*.onrender.com"
    )
    .withSockJS();
```

### 5. Health Check
Tất cả platform đều dùng `/actuator/health` để check health

---

## 🔍 Troubleshooting

### App không start
- Check logs trong platform dashboard
- Đảm bảo database connection đúng
- Check env vars đã set đầy đủ

### Database connection timeout
- Tăng `connection-timeout` trong config
- Check database đã sẵn sàng chưa
- Check firewall/network rules

### Port binding error
- Đảm bảo app listen trên port từ `PORT` env var
- Check Dockerfile expose đúng port

---

## 💰 So Sánh Chi Phí

| Platform | Free Tier | Giới hạn | Sleep |
|----------|-----------|----------|-------|
| **Railway** | $5 credit/tháng | ~500 hours | ❌ |
| **Render** | Free | Unlimited | ✅ (15min) |
| **Cloud Run** | 2M requests/tháng | 360K GB-seconds | ❌ |
| **Fly.io** | 3 VMs shared-cpu | 160GB storage | ❌ |

---

## 🎯 Khuyến Nghị

- **Cho demo/portfolio**: Railway hoặc Render
- **Cho production nhỏ**: Fly.io (hiện tại) hoặc Railway
- **Cho production lớn**: Google Cloud Run hoặc AWS
