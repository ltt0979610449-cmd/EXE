# Các Nền Tảng Deploy Miễn Phí cho Spring Boot

## 1. Railway.app ⭐ (Khuyên dùng)

### Ưu điểm:
- $5 credit miễn phí mỗi tháng (đủ cho app nhỏ)
- Tự động build và deploy từ GitHub
- Hỗ trợ MySQL database tích hợp
- Dễ setup, UI đẹp
- Hỗ trợ Docker và buildpacks

### Cách deploy:
1. Đăng ký tại https://railway.app
2. Tạo project mới → Deploy from GitHub repo
3. Add MySQL service
4. Set environment variables
5. Deploy tự động

### File cần thiết:
- `Dockerfile` (đã có)
- `railway.json` (optional, để config)

---

## 2. Render.com

### Ưu điểm:
- Free tier thực sự (không cần credit card)
- Tự động deploy từ Git
- Hỗ trợ PostgreSQL (MySQL cần external)
- SSL tự động

### Nhược điểm:
- App free tier sleep sau 15 phút không dùng
- Khởi động lại chậm (~30s)

### Cách deploy:
1. Đăng ký tại https://render.com
2. New → Web Service
3. Connect GitHub repo
4. Build Command: `mvn clean package -DskipTests`
5. Start Command: `java -jar target/*.jar`
6. Add PostgreSQL database (free tier)

### File cần thiết:
- `render.yaml` (để config)

---

## 3. Google Cloud Run

### Ưu điểm:
- Free tier: 2 triệu requests/tháng
- Pay-per-use (chỉ trả khi có traffic)
- Scale về 0 tự động
- Hỗ trợ Docker tốt
- Global CDN

### Nhược điểm:
- Cần thẻ tín dụng (nhưng có free tier)
- Database cần external (Cloud SQL hoặc external)

### Cách deploy:
```bash
# Install gcloud CLI
# Build và push image
gcloud builds submit --tag gcr.io/PROJECT_ID/coiviet
# Deploy
gcloud run deploy coiviet --image gcr.io/PROJECT_ID/coiviet --platform managed
```

### File cần thiết:
- `Dockerfile` (đã có)
- `cloudbuild.yaml` (optional)

---

## 4. Oracle Cloud Infrastructure (OCI) Always Free

### Ưu điểm:
- **Vĩnh viễn miễn phí**: 2 VMs, 200GB storage
- Full control như VPS
- Không giới hạn thời gian

### Nhược điểm:
- Cần setup thủ công
- Phức tạp hơn
- Cần kiến thức về Linux/server

### Cách deploy:
1. Đăng ký Oracle Cloud (cần thẻ tín dụng)
2. Tạo VM instance (Always Free tier)
3. SSH vào server
4. Install Java 21, MySQL
5. Deploy app như VPS thông thường

---

## 5. AWS Lambda (Serverless)

### Ưu điểm:
- Free tier: 1 triệu requests/tháng
- Scale tự động
- Pay-per-use

### Nhược điểm:
- Cần refactor code cho serverless
- Cold start có thể chậm
- Database connection pooling phức tạp

### Không khuyên dùng cho Spring Boot truyền thống

---

## So Sánh Nhanh

| Platform | Free Tier | Database | Dễ dùng | Sleep | Khuyên dùng |
|----------|-----------|----------|---------|-------|-------------|
| **Fly.io** | ✅ Có | External | ⭐⭐⭐⭐ | ❌ | ✅ |
| **Railway** | $5 credit | ✅ MySQL | ⭐⭐⭐⭐⭐ | ❌ | ✅✅ |
| **Render** | ✅ Có | PostgreSQL | ⭐⭐⭐⭐ | ✅ (15min) | ✅ |
| **Cloud Run** | 2M requests | External | ⭐⭐⭐ | ❌ | ✅ |
| **OCI** | Vĩnh viễn | Tự setup | ⭐⭐ | ❌ | ⚠️ |

---

## Khuyến Nghị

### Cho dự án nhỏ/portfolio:
1. **Railway.app** - Dễ nhất, có MySQL tích hợp
2. **Render.com** - Free tier tốt, chấp nhận sleep

### Cho production:
1. **Fly.io** (hiện tại) - Tốt nhất
2. **Google Cloud Run** - Scale tốt, pay-per-use
3. **Railway.app** - Nếu budget cho phép

### Cho học tập:
1. **OCI Always Free** - VPS miễn phí vĩnh viễn
2. **Render.com** - Free tier không cần thẻ

---

## Lưu Ý Chung

1. **Database**: Hầu hết platform free tier không có MySQL, cần dùng:
   - External MySQL (PlanetScale, Aiven free tier)
   - PostgreSQL (Render, Railway)
   - Cloud SQL (Google Cloud - có free tier)

2. **Environment Variables**: Tất cả platform đều hỗ trợ set env vars

3. **Build**: Cần đảm bảo `Dockerfile` hoặc build command đúng

4. **Port**: Spring Boot cần listen trên port từ env var (thường là `PORT`)
