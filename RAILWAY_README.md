# 🚂 Deploy lên Railway.app

## Quick Start

1. **Đăng ký**: https://railway.app (đăng nhập bằng GitHub)
2. **Tạo Project**: New Project → Deploy from GitHub repo → Chọn `coiviet`
3. **Thêm MySQL**: New → Database → MySQL
4. **Set Env Vars**: Xem file `RAILWAY_ENV_VARS.txt` để copy-paste
5. **Deploy**: Railway tự động deploy khi push code

## 📋 Checklist

- [ ] Đăng ký Railway account
- [ ] Tạo project từ GitHub
- [ ] Thêm MySQL database
- [ ] Set database connection env vars
- [ ] Set tất cả environment variables (xem `RAILWAY_ENV_VARS.txt`)
- [ ] Generate domain
- [ ] Cập nhật URLs trong env vars (Google OAuth, MoMo, etc.)
- [ ] Test health endpoint: `https://your-app.railway.app/actuator/health`

## 📚 Tài Liệu Chi Tiết

- **Hướng dẫn đầy đủ**: `docs/RAILWAY_DEPLOY.md`
- **So sánh platforms**: `docs/DEPLOYMENT_OPTIONS.md`
- **Quick start guide**: `docs/QUICK_START_DEPLOY.md`

## 🔧 Scripts

- `deploy-railway.ps1` - Chạy để xem hướng dẫn chi tiết

## ⚠️ Lưu Ý Quan Trọng

1. **Database Connection**: Railway tự tạo `MYSQL*` vars, nhưng cần tạo `DBMS_CONNECTION` thủ công
2. **Domain**: Sau khi có domain, cập nhật lại các URLs trong env vars
3. **CORS/WebSocket**: Đã được cấu hình sẵn cho Railway domain

## 🆘 Troubleshooting

- **App không start**: Check logs trong Railway dashboard
- **Database error**: Verify `DBMS_CONNECTION` format đúng
- **CORS error**: Đảm bảo frontend URL đã được thêm vào CORS config
