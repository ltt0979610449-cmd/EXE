# 🚀 Deploy lên Render - Hướng dẫn nhanh

## Bước 1: Commit và Push Code (2 phút)

```powershell
# Kiểm tra thay đổi
git status

# Add tất cả files
git add .

# Commit
git commit -m "Migrate to PostgreSQL - ready for Render deployment"

# Push lên repository
git push origin main
```

## Bước 2: Tạo Render Account (3 phút)

1. Truy cập: **https://dashboard.render.com**
2. Click **"Get Started for Free"**
3. Đăng nhập bằng **GitHub** hoặc **GitLab** account
4. Authorize Render để truy cập repositories

## Bước 3: Tạo PostgreSQL Database (2 phút)

1. Trong Render Dashboard, click **"New +"** → **"PostgreSQL"**
2. Điền thông tin:
   - **Name**: `coiviet-db`
   - **Region**: `Singapore`
   - **PostgreSQL Version**: `16` (hoặc latest)
   - **Plan**: `Free` (hoặc `Starter $7/month`)
3. Click **"Create Database"**
4. **Đợi 1-2 phút** để database được tạo

## Bước 4: Tạo Web Service (3 phút)

1. Click **"New +"** → **"Web Service"**
2. **Connect Repository:**
   - Chọn Git provider (GitHub/GitLab)
   - Chọn repository: `coiviet` (hoặc tên repo của bạn)
   - Chọn branch: `main`
3. **Configure:**
   - **Name**: `coiviet-api`
   - **Region**: `Singapore` (cùng với database)
   - **Runtime**: `Docker`
   - **Dockerfile Path**: `./Dockerfile`
   - **Docker Context**: `.`
   - **Plan**: `Free` (hoặc `Starter $7/month`)
4. **Advanced:**
   - **Health Check Path**: `/actuator/health`
5. Click **"Create Web Service"**

## Bước 5: Link Database (1 phút)

1. Trong Web Service settings
2. Tab **"Environment"**
3. Scroll xuống, tìm **"Link Database"** hoặc **"Add"** → **"Database"**
4. Chọn: `coiviet-db`
5. Render sẽ tự động thêm `DATABASE_*` variables

## Bước 6: Set Environment Variables (5 phút)

**Cách nhanh:** Chạy script để export:
```powershell
.\scripts\export-render-env-vars.ps1
```

Copy output và paste vào Render Dashboard.

**Hoặc set thủ công:** Vào Web Service → **Environment** tab, thêm từng biến:

### Database (Format JDBC URL)
```
DBMS_CONNECTION = jdbc:postgresql://${DATABASE_HOST}:${DATABASE_PORT}/${DATABASE_NAME}?sslmode=require
DBMS_USERNAME = ${DATABASE_USER}
DBMS_PASSWORD = ${DATABASE_PASSWORD}
SPRING_PROFILES_ACTIVE = prod
```

### JWT
```
JWT_SIGNER_KEY = 3aF+lAiyA/tEAeeBtmlou0RwdTwXx0lU6SjH0MYBR7DRt9vyJzlv66uqnqHMP2NW
JWT_VALID_DURATION = 86400
JWT_REFRESHABLE_DURATION = 36000
```

### Cloudinary
```
CLOUDINARY_CLOUD_NAME = dcs0lhrvh
CLOUDINARY_API_KEY = 718451452685618
CLOUDINARY_API_SECRET = GXhU99xN-CpagV9OBgT6R2PipyQ
```

### Email
```
MAIL_USERNAME = truongltse180010@fpt.edu.vn
MAIL_PASSWORD = zhvr axud xxnb jihr
```

### MoMo Payment
```
MOMO_PARTNER_CODE = MOMOBKUN20180529
MOMO_ACCESS_KEY = klm05TvNBzhg7h7j
MOMO_SECRET_KEY = at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa
MOMO_REDIRECT_URL = https://exe-1-k8ma.onrender.com/api/public/payment/momo-return
MOMO_NOTIFY_URL = https://exe-1-k8ma.onrender.com/api/public/payment/momo-notify
```

### Google OAuth2
```
GOOGLE_CLIENT_ID = 87846938671-76pcjrb3ucf7ngmkai7b2qni7uvrn9qt.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET = 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
GOOGLE_REDIRECT_URI = https://exe-1-k8ma.onrender.com/login/oauth2/code/google
OAUTH2_REDIRECT_SUCCESS = https://your-frontend-domain.com/oauth2/callback
```

### Admin
```
INITIAL_ADMIN_PASSWORD = admin123
```

**Click "Save Changes"**

## Bước 7: Deploy (5-10 phút)

1. Tab **"Manual Deploy"**
2. Click **"Deploy latest commit"**
3. **Đợi build** (5-10 phút lần đầu)
4. Xem build logs để đảm bảo không có lỗi

## Bước 8: Verify (2 phút)

1. **Lấy URL production:**
   - Trong Dashboard → Web Service
   - URL sẽ là: `https://exe-1-k8ma.onrender.com` (hoặc tương tự)

2. **Test Health:**
   - Mở: `https://exe-1-k8ma.onrender.com/actuator/health`
   - Phải trả về: `{"status":"UP"}`

3. **Test Swagger:**
   - Mở: `https://exe-1-k8ma.onrender.com/swagger-ui.html`

## Bước 9: Update URLs (Sau khi có URL thực tế)

### Update MoMo URLs:
1. Vào Render Dashboard → Environment
2. Update `MOMO_REDIRECT_URL` với URL thực tế
3. Update `MOMO_NOTIFY_URL` với URL thực tế
4. Save và redeploy

### Update Google OAuth2:
1. Vào: https://console.cloud.google.com
2. APIs & Services → Credentials
3. Edit OAuth 2.0 Client
4. Thêm vào "Authorized redirect URIs":
   - `https://exe-1-k8ma.onrender.com/login/oauth2/code/google`
5. Save
6. Update `GOOGLE_REDIRECT_URI` trong Render (nếu cần)
7. Redeploy

## ✅ Hoàn thành!

Application đã live trên Render!

**URL:** `https://exe-1-k8ma.onrender.com`

## 📚 Tài liệu chi tiết

- **Checklist đầy đủ**: `RENDER_DEPLOY_CHECKLIST.md`
- **Quick Start**: `docs/RENDER_QUICK_START.md`
- **Troubleshooting**: `docs/RENDER_DEPLOYMENT.md`

## ⚠️ Lưu ý

- **Free Tier**: Service sẽ sleep sau 15 phút không có traffic
- **First Request**: Có thể mất 30-60 giây để wake up
- **Build Time**: Lần đầu 5-10 phút, các lần sau nhanh hơn
