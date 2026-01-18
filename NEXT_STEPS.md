# Các bước tiếp theo - Deploy lên Render

## ✅ Đã hoàn thành

1. ✅ **Migration sang PostgreSQL**
   - Đã thay đổi dependency trong `pom.xml`
   - Đã cập nhật `application.yaml` và `application-prod.yaml`
   - Đã cập nhật `render.yaml`

2. ✅ **Test Local với PostgreSQL**
   - PostgreSQL Docker container đã được setup
   - Database `coivietdb` đã được tạo
   - User `coiviet_user` đã được tạo
   - Connection test thành công
   - File `.env` đã được cấu hình với các giá trị thực tế

3. ✅ **Code Compilation**
   - Code đã compile thành công
   - PostgreSQL dependency hoạt động đúng

## 🚀 Bước tiếp theo: Deploy lên Render

### Option 1: Test Application Local trước (Khuyến nghị)

**Bước 1: Build và Run Application**
```powershell
# Build
mvn clean package -DskipTests

# Run
java -jar target/coiviet-0.0.1-SNAPSHOT.jar
```

**Bước 2: Verify**
- Health check: http://localhost:8080/actuator/health
- Swagger UI: http://localhost:8080/swagger-ui.html
- Test một vài API endpoints

**Bước 3: Kiểm tra Database Schema**
```powershell
docker exec -it postgres-coiviet psql -U coiviet_user -d coivietdb -c "\dt"
```

### Option 2: Deploy trực tiếp lên Render

Nếu đã chắc chắn mọi thứ hoạt động, có thể deploy ngay:

## 📋 Checklist trước khi Deploy

- [ ] Application chạy thành công local (nếu test)
- [ ] Database schema được tạo đúng
- [ ] Tất cả environment variables đã được chuẩn bị
- [ ] Code đã được commit và push lên repository

## 🎯 Deploy lên Render - Các bước chi tiết

### Bước 1: Commit và Push Code

```powershell
git add .
git commit -m "Migrate to PostgreSQL and prepare for Render deployment"
git push origin main
```

### Bước 2: Tạo Render Account (nếu chưa có)

1. Truy cập: https://dashboard.render.com
2. Đăng ký/đăng nhập bằng GitHub/GitLab account

### Bước 3: Tạo PostgreSQL Database trên Render

1. Click "New +" → "PostgreSQL"
2. Cấu hình:
   - **Name**: `coiviet-db`
   - **Region**: Singapore (hoặc region gần nhất)
   - **Plan**: Free (hoặc Starter $7/month)
3. Lưu lại connection details (Render sẽ tự động tạo)

### Bước 4: Tạo Web Service trên Render

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

### Bước 5: Link Database với Web Service

1. Trong Web Service settings
2. Tìm phần "Environment" hoặc "Linked Services"
3. Click "Link Database" hoặc "Add Database"
4. Chọn database service `coiviet-db`
5. Render sẽ tự động inject `DATABASE_*` variables

### Bước 6: Set Environment Variables

Trong Web Service → Environment, set các biến sau:

**Database (sẽ tự động từ linked database, nhưng cần format JDBC URL):**
```
SPRING_PROFILES_ACTIVE=prod
DBMS_CONNECTION=jdbc:postgresql://${DATABASE_HOST}:${DATABASE_PORT}/${DATABASE_NAME}?sslmode=require
DBMS_USERNAME=${DATABASE_USER}
DBMS_PASSWORD=${DATABASE_PASSWORD}
```

**Application:**
```
SPRING_PROFILES_ACTIVE=prod
```

**JWT:**
```
JWT_SIGNER_KEY=3aF+lAiyA/tEAeeBtmlou0RwdTwXx0lU6SjH0MYBR7DRt9vyJzlv66uqnqHMP2NW
JWT_VALID_DURATION=86400
JWT_REFRESHABLE_DURATION=36000
```

**Cloudinary:**
```
CLOUDINARY_CLOUD_NAME=dcs0lhrvh
CLOUDINARY_API_KEY=718451452685618
CLOUDINARY_API_SECRET=GXhU99xN-CpagV9OBgT6R2PipyQ
```

**Email:**
```
MAIL_USERNAME=truongltse180010@fpt.edu.vn
MAIL_PASSWORD=zhvr axud xxnb jihr
```

**MoMo Payment:**
```
MOMO_PARTNER_CODE=MOMOBKUN20180529
MOMO_ACCESS_KEY=klm05TvNBzhg7h7j
MOMO_SECRET_KEY=at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa
MOMO_REDIRECT_URL=https://coiviet-api.onrender.com/api/public/payment/momo-return
MOMO_NOTIFY_URL=https://coiviet-api.onrender.com/api/public/payment/momo-notify
```

**Google OAuth2:**
```
GOOGLE_CLIENT_ID=87846938671-76pcjrb3ucf7ngmkai7b2qni7uvrn9qt.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=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
GOOGLE_REDIRECT_URI=https://coiviet-api.onrender.com/login/oauth2/code/google
OAUTH2_REDIRECT_SUCCESS=https://your-frontend-domain.com/oauth2/callback
```

**Admin:**
```
INITIAL_ADMIN_PASSWORD=admin123
```

**Lưu ý:** 
- Thay `coiviet-api.onrender.com` bằng URL thực tế sau khi deploy
- Update Google OAuth2 redirect URI trong Google Cloud Console

### Bước 7: Deploy

1. Sau khi set tất cả environment variables
2. Click "Manual Deploy" → "Deploy latest commit"
3. Hoặc push code mới lên repository (nếu enable auto-deploy)

### Bước 8: Verify Deployment

1. **Kiểm tra Build Logs:**
   - Xem logs trong Render Dashboard
   - Đảm bảo build thành công

2. **Kiểm tra Health Check:**
   - URL: `https://coiviet-api.onrender.com/actuator/health`
   - Phải trả về `{"status":"UP"}`

3. **Test API Endpoints:**
   - Swagger UI: `https://coiviet-api.onrender.com/swagger-ui.html`
   - Test các endpoints chính

4. **Update URLs:**
   - Update MoMo redirect/notify URLs với URL thực tế
   - Update Google OAuth2 redirect URI trong Google Console

## 📚 Tài liệu tham khảo

- **Chi tiết đầy đủ**: `docs/RENDER_QUICK_START.md`
- **Troubleshooting**: `docs/RENDER_DEPLOYMENT.md`
- **Environment Variables**: `docs/RENDER_DEPLOYMENT.md` phần "Environment Variables"

## ⚠️ Lưu ý quan trọng

1. **Google OAuth2 Redirect URI:**
   - Cần thêm `https://coiviet-api.onrender.com/login/oauth2/code/google` vào Google Cloud Console
   - Vào: https://console.cloud.google.com → APIs & Services → Credentials

2. **MoMo Payment URLs:**
   - Cần update trong MoMo dashboard sau khi có URL production

3. **Free Tier Limitations:**
   - Web Service sẽ sleep sau 15 phút không có traffic
   - Database có limitations về storage và connections

4. **Cost:**
   - Free tier: Có limitations
   - Starter plan: $7/month cho mỗi service (database + web service = $14/month)

## 🎉 Hoàn thành

Sau khi deploy thành công, bạn sẽ có:
- ✅ PostgreSQL database trên Render
- ✅ Spring Boot backend API trên Render
- ✅ Tất cả services hoạt động với production URLs
