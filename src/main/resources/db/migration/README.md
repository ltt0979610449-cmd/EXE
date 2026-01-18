# Flyway Database Migrations

## Tổng quan

Thư mục này chứa các Flyway migration scripts để quản lý database schema.

## Trạng thái hiện tại

Hiện tại, ứng dụng sử dụng Hibernate `ddl-auto: update` để tự động tạo/cập nhật schema.

## Kích hoạt Flyway (Optional)

Để chuyển sang sử dụng Flyway migrations:

### Bước 1: Tạo migration scripts từ schema hiện tại

1. Export schema hiện tại từ database:
```bash
mysqldump -u coiviet_user -p coivietdb --no-data --routines > schema.sql
```

2. Tạo migration file đầu tiên:
   - File: `V1__Initial_schema.sql`
   - Copy nội dung từ schema.sql đã export

### Bước 2: Enable Flyway trong application-prod.yaml

Thay đổi trong `application-prod.yaml`:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # hoặc 'none' - không tự động tạo schema
  flyway:
    enabled: true
    baseline-on-migrate: true  # Tạo baseline nếu database đã có schema
    locations: classpath:db/migration
```

### Bước 3: Tạo migration cho các thay đổi mới

Khi có thay đổi schema, tạo migration file mới:
- Format: `V<version>__<description>.sql`
- Ví dụ: `V2__Add_user_table.sql`, `V3__Add_indexes.sql`

### Bước 4: Test migrations

1. Test locally với database test
2. Verify migrations chạy đúng
3. Deploy lên Render

## Lưu ý

- **Không nên** enable Flyway nếu database đã có data và đang dùng `ddl-auto: update`
- **Nên** enable Flyway từ đầu dự án hoặc khi migrate sang production
- Flyway sẽ tự động chạy migrations khi app khởi động
- Migrations phải là **idempotent** (có thể chạy nhiều lần mà không lỗi)

## Migration File Naming Convention

- Format: `V<version>__<description>.sql`
- Version: Số nguyên tăng dần (1, 2, 3, ...)
- Description: Mô tả ngắn gọn migration (dùng underscore)
- Ví dụ:
  - `V1__Initial_schema.sql`
  - `V2__Add_user_table.sql`
  - `V3__Add_email_index.sql`

## Undo Migrations

Flyway không hỗ trợ undo migrations tự động. Cần tạo migration mới để revert changes nếu cần.

## Resources

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Flyway Best Practices](https://flywaydb.org/documentation/learn-more/best-practices)
