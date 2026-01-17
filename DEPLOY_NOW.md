# 🚀 Deploy Ngay Lên Railway - Hướng Dẫn Từng Bước

## ✅ Đã Chuẩn Bị Sẵn

- ✅ File env vars đã điền sẵn: `RAILWAY_ENV_VARS_READY.txt`
- ✅ Script tự động: `deploy-railway-auto.ps1`
- ✅ Helper script: `railway-db-helper.ps1`

## 🎯 Bắt Đầu Deploy (5-10 phút)

### Cách 1: Chạy Script Tự Động (Khuyên dùng)

```powershell
.\deploy-railway-auto.ps1
```

Script sẽ hướng dẫn bạn từng bước.

### Cách 2: Làm Thủ Công

## 📋 Các Bước Chi Tiết

### Bước 1: Đăng Ký Railway (nếu chưa có)

1. Vào https://railway.app
2. Click "Login" → "Login with GitHub"
3. Authorize Railway

### Bước 2: Tạo Project

1. Click **"New Project"**
2. Chọn **"Deploy from GitHub repo"**
3. Chọn repository **"coiviet"**
4. Railway tự động detect Dockerfile và build

⏱️ Đợi build xong (2-5 phút)

### Bước 3: Thêm MySQL Database

1. Trong project dashboard, click nút **"New"** (màu xanh, góc trên bên phải)
2. Chọn **"Database"** → **"MySQL"**
3. Railway tự động tạo MySQL instance

⏱️ Đợi MySQL khởi động (1-2 phút)

### Bước 4: Lấy Database Connection Info

**Cách A: Dùng Helper Script (Dễ nhất)**

```powershell
.\railway-db-helper.ps1
```

Script sẽ hỏi các giá trị từ MySQL service và tạo connection string tự động.

**Cách B: Làm Thủ Công**

1. Click vào **MySQL service** trong dashboard
2. Vào tab **"Variables"**
3. Copy các giá trị:
   - `MYSQLHOST`
   - `MYSQLPORT`
   - `MYSQLDATABASE`
   - `MYSQLUSER`
   - `MYSQLPASSWORD`

4. Tạo connection string:
   ```
   DBMS_CONNECTION=jdbc:mysql://[MYSQLHOST]:[MYSQLPORT]/[MYSQLDATABASE]?useSSL=true&serverTimezone=Asia/Ho_Chi_Minh
   DBMS_USERNAME=[MYSQLUSER]
   DBMS_PASSWORD=[MYSQLPASSWORD]
   ```

### Bước 5: Set Environment Variables

1. Vào **Web Service** (Spring Boot app) → Tab **"Variables"**
2. Click **"Raw Editor"** (để paste nhiều biến cùng lúc)
3. Copy tất cả nội dung từ file `RAILWAY_ENV_VARS_READY.txt`
4. Paste vào Raw Editor
5. **QUAN TRỌNG**: Thay thế 3 dòng database bằng giá trị từ Bước 4:
   ```
   DBMS_CONNECTION=jdbc:mysql://[giá trị thực tế]
   DBMS_USERNAME=[giá trị thực tế]
   DBMS_PASSWORD=[giá trị thực tế]
   ```
6. Click **"Update"**

### Bước 6: Generate Domain

1. Vào **Web Service** → **Settings** → **Networking**
2. Click **"Generate Domain"**
3. Copy domain (ví dụ: `coiviet-production.up.railway.app`)

### Bước 7: Cập Nhật URLs

Quay lại **Variables** và cập nhật 3 biến sau (thay `your-app.railway.app` bằng domain thực tế):

```
GOOGLE_REDIRECT_URI=https://[YOUR-DOMAIN]/login/oauth2/code/google
MOMO_REDIRECT_URL=https://[YOUR-DOMAIN]/api/public/payment/momo-return
MOMO_NOTIFY_URL=https://[YOUR-DOMAIN]/api/public/payment/momo-notify
```

### Bước 8: Test

1. Railway tự động deploy khi bạn set env vars
2. Vào **Deployments** tab để xem logs
3. Test các endpoint:
   - Health: `https://[YOUR-DOMAIN]/actuator/health`
   - Swagger: `https://[YOUR-DOMAIN]/swagger-ui.html`

## ✅ Checklist

- [ ] Đăng ký Railway
- [ ] Tạo project từ GitHub
- [ ] Thêm MySQL database
- [ ] Lấy database connection info (dùng helper script)
- [ ] Set tất cả env vars từ `RAILWAY_ENV_VARS_READY.txt`
- [ ] Set database connection vars (DBMS_CONNECTION, DBMS_USERNAME, DBMS_PASSWORD)
- [ ] Generate domain
- [ ] Cập nhật URLs (GOOGLE_REDIRECT_URI, MOMO_REDIRECT_URL, MOMO_NOTIFY_URL)
- [ ] Test health endpoint
- [ ] Test Swagger

## 🆘 Troubleshooting

### App không start
- Check logs trong **Deployments** tab
- Verify tất cả env vars đã set đúng
- Check database connection string

### Database connection error
- Verify `DBMS_CONNECTION` format đúng
- Check MySQL service đã running
- Dùng `railway-db-helper.ps1` để tạo lại

### Build failed
- Check Dockerfile có đúng không
- Check logs trong build process

## 📞 Hỗ Trợ

- Railway Docs: https://docs.railway.app
- Railway Discord: https://discord.gg/railway
