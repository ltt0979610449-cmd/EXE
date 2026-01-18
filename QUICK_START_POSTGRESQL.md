# Quick Start - Test PostgreSQL Local

## 🚀 Cách nhanh nhất

### Option 1: Sử dụng Script Tự động (Recommended)

```powershell
cd c:\CN8\EXE2\coiviet\coiviet
.\scripts\setup-postgresql-local.ps1
```

Script sẽ tự động:
- ✅ Kiểm tra PostgreSQL
- ✅ Tạo database và user
- ✅ Tạo file `.env.example`
- ✅ Test connection

Sau đó:
1. Copy `.env.example` thành `.env` và điền các giá trị còn thiếu
2. Chạy: `mvn clean package`
3. Chạy: `java -jar target/coiviet-0.0.1-SNAPSHOT.jar`

### Option 2: Sử dụng Docker (Nếu đã có Docker)

```powershell
# Chạy PostgreSQL container
docker run --name postgres-coiviet `
  -e POSTGRES_PASSWORD=postgres `
  -e POSTGRES_DB=coivietdb `
  -p 5432:5432 `
  -d postgres:16

# Tạo user
docker exec -it postgres-coiviet psql -U postgres -c "CREATE USER coiviet_user WITH PASSWORD 'coiviet_password';"
docker exec -it postgres-coiviet psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE coivietdb TO coiviet_user;"
docker exec -it postgres-coiviet psql -U postgres -d coivietdb -c "GRANT ALL ON SCHEMA public TO coiviet_user;"
```

Tạo file `.env`:
```env
DBMS_CONNECTION=jdbc:postgresql://localhost:5432/coivietdb
DBMS_USERNAME=coiviet_user
DBMS_PASSWORD=coiviet_password
SPRING_PROFILES_ACTIVE=default
# ... các biến khác (xem docs/POSTGRESQL_LOCAL_TEST.md)
```

## 📋 Checklist

- [ ] PostgreSQL đã được cài đặt hoặc Docker đang chạy
- [ ] Database và user đã được tạo
- [ ] File `.env` đã được tạo với đúng cấu hình
- [ ] Test connection thành công
- [ ] Application build và chạy thành công

## 🔍 Test Connection

```powershell
.\scripts\test-postgresql-connection.ps1
```

## 📚 Chi tiết

Xem hướng dẫn đầy đủ trong: `docs/POSTGRESQL_LOCAL_TEST.md`

## ⚠️ Troubleshooting

**PostgreSQL chưa được cài đặt?**
```powershell
choco install postgresql -y
```

**Lỗi connection?**
- Kiểm tra PostgreSQL service đang chạy
- Verify username/password trong `.env`
- Xem `docs/POSTGRESQL_LOCAL_TEST.md` phần Troubleshooting
