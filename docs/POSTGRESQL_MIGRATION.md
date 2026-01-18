# Hướng dẫn Migrate từ MySQL sang PostgreSQL

## ✅ Đã hoàn thành

Các thay đổi sau đã được thực hiện tự động:

1. ✅ **pom.xml**: Thay đổi dependency từ `mysql-connector-j` sang `postgresql`
2. ✅ **application.yaml**: Cập nhật dialect từ `MySQLDialect` sang `PostgreSQLDialect`
3. ✅ **application-prod.yaml**: Cập nhật dialect và JDBC URL format
4. ✅ **render.yaml**: Cập nhật cấu hình database connection cho PostgreSQL

## 🔍 Kiểm tra cần thiết

### 1. Entity Classes

PostgreSQL và MySQL có một số khác biệt về:
- **AUTO_INCREMENT**: MySQL dùng `AUTO_INCREMENT`, PostgreSQL dùng `SERIAL` hoặc `IDENTITY`
- **String types**: MySQL có `VARCHAR`, PostgreSQL cũng có nhưng có thêm `TEXT`
- **Boolean**: MySQL dùng `TINYINT(1)`, PostgreSQL dùng `BOOLEAN`
- **Date/Time**: Cả hai đều hỗ trợ `TIMESTAMP`, `DATE`, `TIME`

**Good news**: Hibernate sẽ tự động xử lý các khác biệt này khi dùng `@GeneratedValue(strategy = GenerationType.IDENTITY)`.

### 2. Native Queries

Nếu có native SQL queries trong code, cần kiểm tra:

**MySQL-specific functions cần thay đổi:**
- `DATE_FORMAT()` → PostgreSQL: `TO_CHAR()`
- `NOW()` → PostgreSQL: `NOW()` (giống nhau)
- `IFNULL()` → PostgreSQL: `COALESCE()`
- `CONCAT()` → PostgreSQL: `||` hoặc `CONCAT()`
- `LIMIT x OFFSET y` → PostgreSQL: `LIMIT x OFFSET y` (giống nhau)

**Ví dụ:**
```sql
-- MySQL
SELECT DATE_FORMAT(created_at, '%Y-%m-%d') FROM users;

-- PostgreSQL
SELECT TO_CHAR(created_at, 'YYYY-MM-DD') FROM users;
```

### 3. Data Types

**String Length:**
- MySQL: `VARCHAR(255)` - giới hạn length
- PostgreSQL: `VARCHAR(255)` hoặc `TEXT` - không giới hạn thực sự

**Text Fields:**
- MySQL: `TEXT`, `MEDIUMTEXT`, `LONGTEXT`
- PostgreSQL: `TEXT` (không phân biệt size)

**JSON:**
- MySQL: `JSON` type
- PostgreSQL: `JSON` hoặc `JSONB` (recommended, faster)

## 📝 Các bước tiếp theo

### Bước 1: Test Locally với PostgreSQL

1. **Cài đặt PostgreSQL local:**
   ```bash
   # Windows (chocolatey)
   choco install postgresql
   
   # Hoặc download từ: https://www.postgresql.org/download/
   ```

2. **Tạo database:**
   ```sql
   CREATE DATABASE coivietdb;
   CREATE USER coiviet_user WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE coivietdb TO coiviet_user;
   ```

3. **Cập nhật `.env` hoặc environment variables:**
   ```
   DBMS_CONNECTION=jdbc:postgresql://localhost:5432/coivietdb
   DBMS_USERNAME=coiviet_user
   DBMS_PASSWORD=your_password
   ```

4. **Test application:**
   ```bash
   mvn clean package
   java -jar target/coiviet-0.0.1-SNAPSHOT.jar --spring.profiles.active=default
   ```

5. **Kiểm tra:**
   - App khởi động thành công
   - Database schema được tạo tự động (nếu dùng `ddl-auto: update`)
   - Test các API endpoints
   - Verify data được lưu đúng

### Bước 2: Migrate Data (nếu có data hiện tại)

Nếu đã có data trong MySQL, cần migrate:

1. **Export data từ MySQL:**
   ```bash
   mysqldump -u coiviet_user -p coivietdb > mysql_export.sql
   ```

2. **Convert SQL syntax (nếu cần):**
   - Sử dụng tool như `pgloader` hoặc manual conversion
   - Hoặc export dưới dạng CSV và import vào PostgreSQL

3. **Import vào PostgreSQL:**
   ```bash
   psql -U coiviet_user -d coivietdb -f converted_export.sql
   ```

**Tool recommendation: `pgloader`:**
```bash
# Install pgloader
# Windows: choco install pgloader
# Linux/Mac: brew install pgloader

# Migrate
pgloader mysql://user:password@localhost/coivietdb postgresql://user:password@localhost/coivietdb
```

### Bước 3: Deploy lên Render

Sau khi test thành công local:

1. **Commit changes:**
   ```bash
   git add .
   git commit -m "Migrate from MySQL to PostgreSQL"
   git push origin main
   ```

2. **Tạo PostgreSQL database trên Render:**
   - Vào Render Dashboard
   - New + → PostgreSQL
   - Name: `coiviet-db`
   - Region: Singapore
   - Plan: Free hoặc Starter

3. **Link database với Web Service:**
   - Trong Web Service settings
   - Link với `coiviet-db`
   - Render sẽ tự động inject `DATABASE_*` variables

4. **Set environment variables:**
   - `SPRING_PROFILES_ACTIVE=prod`
   - `DBMS_CONNECTION` sẽ được tự động format từ Render database
   - Các biến khác như JWT, Cloudinary, etc.

5. **Deploy:**
   - Render sẽ tự động build và deploy
   - Kiểm tra logs để đảm bảo không có lỗi

## ⚠️ Lưu ý quan trọng

### 1. Case Sensitivity

- **MySQL**: Table và column names không phân biệt hoa thường (mặc định)
- **PostgreSQL**: Table và column names **phân biệt hoa thường**

**Giải pháp:**
- Hibernate sẽ tự động xử lý nếu dùng `@Table(name = "users")` (lowercase)
- Nếu có uppercase trong table names, cần quote: `"Users"` → PostgreSQL sẽ tìm `Users` (case-sensitive)

### 2. Reserved Words

PostgreSQL có một số reserved words khác MySQL:
- `user` → Cần quote: `"user"` hoặc đổi tên
- `order` → Cần quote: `"order"`

**Giải pháp:** Hibernate sẽ tự động quote nếu cần.

### 3. Auto Increment

- **MySQL**: `AUTO_INCREMENT`
- **PostgreSQL**: `SERIAL` hoặc `IDENTITY` (PostgreSQL 10+)

**Good news:** `@GeneratedValue(strategy = GenerationType.IDENTITY)` hoạt động với cả hai.

### 4. Boolean Type

- **MySQL**: `TINYINT(1)` hoặc `BOOLEAN`
- **PostgreSQL**: `BOOLEAN`

**Good news:** Hibernate map `boolean` Java type đúng cho cả hai.

### 5. Date/Time Functions

Một số functions khác nhau:
- `NOW()` - giống nhau
- `CURDATE()` (MySQL) → `CURRENT_DATE` (PostgreSQL)
- `CURTIME()` (MySQL) → `CURRENT_TIME` (PostgreSQL)

## 🔧 Troubleshooting

### Lỗi: "relation does not exist"

**Nguyên nhân:** Table chưa được tạo hoặc tên table sai (case-sensitive)

**Giải pháp:**
- Kiểm tra `ddl-auto: update` đã enable chưa
- Kiểm tra table names trong entities (nên dùng lowercase)
- Xem logs để tìm table name chính xác

### Lỗi: "column does not exist"

**Nguyên nhân:** Column name case-sensitive hoặc chưa được tạo

**Giải pháp:**
- Kiểm tra column names trong entities
- Verify schema đã được tạo đúng

### Lỗi: "syntax error"

**Nguyên nhân:** Native SQL query dùng MySQL-specific syntax

**Giải pháp:**
- Tìm và thay thế MySQL-specific functions
- Sử dụng Hibernate/JPA queries thay vì native SQL khi có thể

### Lỗi: Connection timeout

**Nguyên nhân:** Database chưa sẵn sàng hoặc connection string sai

**Giải pháp:**
- Verify connection string format: `jdbc:postgresql://host:port/database?sslmode=require`
- Kiểm tra database service đã running chưa
- Verify username/password đúng

## 📚 Resources

- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Hibernate PostgreSQL Dialect](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#database-dialect)
- [pgloader - MySQL to PostgreSQL Migration](https://pgloader.readthedocs.io/)
- [PostgreSQL vs MySQL Differences](https://www.postgresql.org/docs/current/datatype.html)

## ✅ Checklist

- [ ] Đã thay đổi dependency trong `pom.xml`
- [ ] Đã cập nhật `application.yaml` và `application-prod.yaml`
- [ ] Đã test locally với PostgreSQL
- [ ] Đã kiểm tra các native queries (nếu có)
- [ ] Đã migrate data (nếu có data hiện tại)
- [ ] Đã tạo PostgreSQL database trên Render
- [ ] Đã link database với Web Service
- [ ] Đã set environment variables
- [ ] Đã deploy và test trên Render
- [ ] Đã verify tất cả API endpoints hoạt động đúng

## 🎉 Hoàn thành

Sau khi hoàn thành tất cả các bước trên, ứng dụng đã sẵn sàng chạy trên PostgreSQL!
