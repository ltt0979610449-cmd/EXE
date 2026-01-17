# 📦 Tóm Tắt Deployment Setup

## ✅ Đã Hoàn Thành

### 1. Xóa tất cả file Fly.io
- ✅ `fly.toml`
- ✅ `fly.mysql.toml`
- ✅ `deploy-fly.ps1`
- ✅ `deploy-backend-only.ps1`
- ✅ `deploy-mysql-only.ps1`
- ✅ `docs/DEPLOYMENT.md`

### 2. Tạo file Railway
- ✅ `railway.json` - Config Railway
- ✅ `deploy-railway.ps1` - Script hướng dẫn
- ✅ `railway-db-helper.ps1` - Helper tạo DB connection string
- ✅ `RAILWAY_README.md` - Quick start guide
- ✅ `RAILWAY_ENV_VARS.txt` - Template env vars
- ✅ `docs/RAILWAY_DEPLOY.md` - Tài liệu chi tiết
- ✅ `docs/RAILWAY_SETUP_SUMMARY.md` - Tóm tắt setup

### 3. Cập nhật Code
- ✅ `application-prod.yaml` - Hỗ trợ `PORT` env var
- ✅ `SecurityConfig.java` - Thêm Railway domain vào CORS
- ✅ `WebSocketConfig.java` - Thêm Railway domain vào WebSocket
- ✅ `.gitignore` - Cập nhật
- ✅ `.dockerignore` - Cập nhật

## 🚀 Bước Tiếp Theo: Deploy lên Railway

### Quick Start (5 phút)

1. **Đăng ký Railway**
   ```
   https://railway.app → Login with GitHub
   ```

2. **Tạo Project**
   ```
   New Project → Deploy from GitHub repo → Chọn "coiviet"
   ```

3. **Thêm MySQL**
   ```
   New → Database → MySQL
   ```

4. **Set Database Connection**
   - Chạy script: `.\railway-db-helper.ps1`
   - Hoặc xem hướng dẫn trong `RAILWAY_ENV_VARS.txt`

5. **Set Environment Variables**
   - Copy từ `RAILWAY_ENV_VARS.txt`
   - Paste vào Railway Dashboard → Service → Variables

6. **Generate Domain**
   - Settings → Networking → Generate Domain
   - Cập nhật URLs trong env vars (Google OAuth, MoMo)

7. **Deploy & Test**
   - Railway tự động deploy
   - Test: `https://your-app.railway.app/actuator/health`

## 📚 Tài Liệu

| File | Mô tả |
|------|-------|
| `RAILWAY_README.md` | Quick start guide |
| `RAILWAY_ENV_VARS.txt` | Template env vars để copy |
| `docs/RAILWAY_DEPLOY.md` | Hướng dẫn chi tiết đầy đủ |
| `docs/DEPLOYMENT_OPTIONS.md` | So sánh các platform |
| `docs/QUICK_START_DEPLOY.md` | Quick start cho nhiều platform |

## 🔧 Scripts

| Script | Mô tả |
|--------|-------|
| `deploy-railway.ps1` | Hiển thị hướng dẫn deploy Railway |
| `railway-db-helper.ps1` | Helper tạo DB connection string |

## ⚠️ Lưu Ý Quan Trọng

1. **Database Connection**: Railway tự tạo `MYSQL*` vars, nhưng Spring Boot cần `DBMS_CONNECTION` format
   - Dùng script `railway-db-helper.ps1` để tạo tự động
   - Hoặc xem hướng dẫn trong `RAILWAY_ENV_VARS.txt`

2. **Environment Variables**: Set thủ công trên Railway dashboard (không có CLI như Fly.io)

3. **Domain**: Railway tự generate domain, cần cập nhật lại URLs trong env vars sau khi có domain

4. **CORS/WebSocket**: Đã được cấu hình sẵn cho Railway domain (`*.railway.app`, `*.up.railway.app`)

## 🎯 Checklist Deploy

- [ ] Đăng ký Railway account
- [ ] Tạo project từ GitHub repo
- [ ] Thêm MySQL database
- [ ] Chạy `railway-db-helper.ps1` để lấy DB connection string
- [ ] Set database env vars (`DBMS_CONNECTION`, `DBMS_USERNAME`, `DBMS_PASSWORD`)
- [ ] Set tất cả env vars từ `RAILWAY_ENV_VARS.txt`
- [ ] Generate domain trong Railway
- [ ] Cập nhật URLs trong env vars (Google OAuth, MoMo)
- [ ] Push code hoặc click Deploy
- [ ] Test health endpoint
- [ ] Test API endpoints
- [ ] Cập nhật frontend URLs

## 🆘 Troubleshooting

### App không start
- Check logs trong Railway dashboard
- Verify tất cả env vars đã set đúng
- Check database connection string

### Database connection error
- Verify `DBMS_CONNECTION` format đúng
- Check MySQL service đã running
- Dùng `railway-db-helper.ps1` để tạo lại connection string

### CORS error
- Đảm bảo frontend URL đã được thêm vào CORS config
- Check `SecurityConfig.java` có Railway domain chưa

## 📞 Hỗ Trợ

- Railway Docs: https://docs.railway.app
- Railway Discord: https://discord.gg/railway
- Railway Status: https://status.railway.app
