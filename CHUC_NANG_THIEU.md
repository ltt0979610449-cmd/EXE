# BÁO CÁO CÁC CHỨC NĂNG CÒN THIẾU

## 🔴 QUAN TRỌNG - CẦN BỔ SUNG NGAY

### 1. **Quên mật khẩu / Đặt lại mật khẩu (Forgot Password / Reset Password)**
- **Mô tả**: Người dùng quên mật khẩu cần có cách để đặt lại
- **Thiếu**: 
  - Endpoint `/api/auth/forgot-password` để gửi email reset password
  - Endpoint `/api/auth/reset-password` để đặt lại mật khẩu với token
  - Model/Entity để lưu reset token và thời gian hết hạn
- **Tác động**: Người dùng không thể tự khôi phục tài khoản

### 2. **Quản lý trạng thái User (Block/Unblock User)**
- **Mô tả**: Admin cần có khả năng block/unblock user
- **Thiếu**: 
  - Endpoint trong `AdminUserController` để thay đổi status (ACTIVE/INACTIVE/BANNED)
  - Hiện tại chỉ có enum `Status.BANNED` nhưng không có API để sử dụng
- **Tác động**: Admin không thể quản lý user hiệu quả

### 3. **Tìm tours theo Artisan**
- **Mô tả**: Cần có endpoint để lấy danh sách tours của một artisan
- **Thiếu**:
  - Method `findByArtisanId` trong `TourRepository`
  - Method trong `TourService` và `TourServiceImpl`
  - Endpoint `/api/tours/public/artisan/{artisanId}` trong `TourController`
- **Tác động**: Không thể xem các tours của một artisan cụ thể

### 4. **Thống kê cho Artisan**
- **Mô tả**: Artisan cần có dashboard để xem thống kê tours, bookings, doanh thu
- **Thiếu**:
  - Controller `ArtisanDashboardController` hoặc endpoint trong `ArtisanController`
  - Service để tính toán thống kê (tours, bookings, revenue, ratings)
- **Tác động**: Artisan không thể theo dõi hiệu suất công việc

### 5. **Đổi mật khẩu riêng (Change Password)**
- **Mô tả**: User cần endpoint riêng để đổi mật khẩu
- **Thiếu**:
  - Endpoint `/api/users/change-password` trong `UserController`
  - Validation để kiểm tra mật khẩu cũ
- **Tác động**: Hiện tại phải dùng update user, không tiện lợi

---

## 🟡 QUAN TRỌNG - NÊN BỔ SUNG

### 6. **Validation và Authorization đầy đủ**
- **Vấn đề**: 
  - `VoucherController` có comment `// TODO: Add admin/staff check` (dòng 60)
  - Một số endpoint thiếu kiểm tra quyền truy cập
- **Cần bổ sung**:
  - Thêm `@PreAuthorize` hoặc kiểm tra role trong các endpoint quan trọng
  - Đảm bảo chỉ admin/staff mới có thể tạo/sửa/xóa vouchers, tours, etc.

### 7. **Soft Delete**
- **Mô tả**: Hiện tại đang dùng hard delete, nên chuyển sang soft delete
- **Thiếu**:
  - Thêm field `deletedAt` vào các entity quan trọng (User, Tour, Booking, etc.)
  - Filter các record đã xóa trong queries
  - Endpoint để restore các record đã xóa
- **Tác động**: Mất dữ liệu vĩnh viễn khi xóa

### 8. **Audit Log / Activity Log**
- **Mô tả**: Ghi lại các thay đổi quan trọng (tạo/sửa/xóa tours, bookings, users)
- **Thiếu**:
  - Entity `AuditLog` để lưu các hoạt động
  - Service để ghi log tự động
  - Endpoint để admin xem logs
- **Tác động**: Khó theo dõi và debug các vấn đề

### 9. **Export Data (Excel/CSV)**
- **Mô tả**: Admin cần export dữ liệu để phân tích
- **Thiếu**:
  - Endpoint để export bookings, users, tours ra Excel/CSV
  - Service để format dữ liệu
- **Tác động**: Khó phân tích dữ liệu thủ công

### 10. **Tìm kiếm và Lọc nâng cao**
- **Mô tả**: Cần tìm kiếm tours, users, bookings với nhiều tiêu chí
- **Thiếu**:
  - Search tours theo tên, mô tả, giá, artisan
  - Filter bookings theo status, date range, user
  - Pagination cho các danh sách lớn
- **Tác động**: Khó tìm kiếm khi dữ liệu lớn

---

## 🟢 TÍNH NĂNG BỔ SUNG - TÙY CHỌN

### 11. **Email Verification**
- **Mô tả**: Xác thực email khi đăng ký
- **Thiếu**: 
  - Gửi email xác thực
  - Endpoint để verify email với token
  - Field `emailVerified` trong User

### 12. **Two-Factor Authentication (2FA)**
- **Mô tả**: Bảo mật tài khoản với 2FA
- **Thiếu**: 
  - Tích hợp OTP (SMS hoặc Email)
  - Endpoint để enable/disable 2FA

### 13. **Social Login (ngoài Google)**
- **Mô tả**: Đăng nhập bằng Facebook, Apple, etc.
- **Thiếu**: 
  - Tích hợp các OAuth provider khác

### 14. **Push Notifications**
- **Mô tả**: Gửi push notification cho mobile app
- **Thiếu**: 
  - Tích hợp FCM hoặc APNS
  - Service để gửi push notifications

### 15. **File Upload Validation**
- **Mô tả**: Kiểm tra kích thước, định dạng file upload
- **Thiếu**: 
  - Validation cho file size, file type
  - Virus scanning (nếu cần)

### 16. **Rate Limiting**
- **Mô tả**: Giới hạn số request để tránh abuse
- **Thiếu**: 
  - Tích hợp Spring Boot Rate Limiting
  - Giới hạn theo IP hoặc user

### 17. **Caching**
- **Mô tả**: Cache dữ liệu thường dùng để tăng performance
- **Thiếu**: 
  - Cache cho tours, provinces, artisans
  - Redis integration

### 18. **API Versioning**
- **Mô tả**: Quản lý version của API
- **Thiếu**: 
  - Version trong URL (`/api/v1/...`)
  - Deprecation strategy

---

## 📋 TÓM TẮT THEO ĐỘ ƯU TIÊN

### **Ưu tiên cao (Làm ngay)**
1. ✅ Quên mật khẩu / Đặt lại mật khẩu
2. ✅ Quản lý trạng thái User (Block/Unblock)
3. ✅ Tìm tours theo Artisan
4. ✅ Thống kê cho Artisan
5. ✅ Đổi mật khẩu riêng

### **Ưu tiên trung bình (Làm sau)**
6. Validation và Authorization đầy đủ
7. Soft Delete
8. Audit Log
9. Export Data
10. Tìm kiếm và Lọc nâng cao

### **Ưu tiên thấp (Tùy chọn)**
11-18. Các tính năng bổ sung khác

---

## 📝 GHI CHÚ

- Dự án đã có cấu trúc tốt với đầy đủ các entity, service, controller cơ bản
- Các chức năng CRUD cơ bản đã được implement
- Payment gateway (MoMo, VNPay) đã được tích hợp
- WebSocket chat đã được implement
- Notification system đã có
- Admin dashboard đã có thống kê cơ bản

**Tổng kết**: Dự án đã có nền tảng tốt, nhưng cần bổ sung các chức năng quản lý và bảo mật quan trọng để hoàn thiện hơn.
