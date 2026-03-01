# Hướng Dẫn Test API cho Frontend - Từ Đầu Đến Cuối

Tài liệu này hướng dẫn chi tiết cách test tất cả các API endpoints của dự án CoiViet từ đầu đến cuối.

## Mục Lục
1. [Chuẩn Bị Môi Trường](#chuẩn-bị-môi-trường)
2. [Cấu Trúc Response API](#cấu-trúc-response-api)
3. [Authentication & Authorization](#authentication--authorization)
4. [Test Các Endpoint Public](#test-các-endpoint-public)
5. [Test Các Endpoint Cần Authentication](#test-các-endpoint-cần-authentication)
6. [Test Upload File (Multipart)](#test-upload-file-multipart)
7. [Công Cụ Test](#công-cụ-test)
8. [Xử Lý Lỗi](#xử-lý-lỗi)
9. [Best Practices](#best-practices)

---

## Chuẩn Bị Môi Trường

### 1. Base URL
- **Local Development**: `http://localhost:8080`
- **Production**: (Cập nhật theo môi trường deploy)

### 2. Cài Đặt Công Cụ Test
- **Postman** (khuyến nghị): https://www.postman.com/downloads/
- **Thunder Client** (VS Code extension)
- **Insomnia**: https://insomnia.rest/download
- **cURL** (command line)

### 3. Import Collection (Postman)
Tạo collection mới và import các request sau vào Postman để test dễ dàng hơn.

---

## Cấu Trúc Response API

Tất cả API đều trả về format chuẩn:

```json
{
  "success": true,
  "message": "Thông báo thành công",
  "data": { /* dữ liệu trả về */ },
  "timestamp": "2024-01-15T10:30:00"
}
```

**Khi có lỗi:**
```json
{
  "success": false,
  "message": "Thông báo lỗi",
  "error": "Mã lỗi",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Authentication & Authorization

### 1. Đăng Ký Tài Khoản

**POST** `/api/users`

```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123",
  "fullName": "Test User",
  "phone": "0123456789"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "fullName": "Test User"
  }
}
```

### 2. Đăng Nhập

**POST** `/api/auth/login`

```json
{
  "username": "testuser",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_here",
    "user": {
      "id": 1,
      "username": "testuser",
      "email": "test@example.com"
    }
  }
}
```

**Lưu ý:** Lưu `token` để sử dụng cho các request cần authentication.

### 3. Đăng Nhập Google

**POST** `/api/auth/google`

```json
{
  "idToken": "google_id_token_here"
}
```

### 4. Refresh Token

**POST** `/api/auth/refresh`

```json
{
  "refreshToken": "refresh_token_here"
}
```

### 5. Đăng Xuất

**POST** `/api/auth/logout`

**Headers:**
```
Authorization: Bearer {token}
```

### 6. Quên Mật Khẩu

**POST** `/api/auth/forgot-password`

```json
{
  "email": "test@example.com"
}
```

**POST** `/api/auth/verify-otp`

```json
{
  "email": "test@example.com",
  "otp": "123456"
}
```

**POST** `/api/auth/reset-password`

```json
{
  "email": "test@example.com",
  "otp": "123456",
  "newPassword": "newpassword123"
}
```

### 7. Sử Dụng Token

Với mọi request cần authentication, thêm header:

```
Authorization: Bearer {token}
```

---

## Test Các Endpoint Public

Các endpoint này không cần authentication.

### 1. Trang Chủ

**GET** `/api/public/home?limit=10`

**Response:**
```json
{
  "success": true,
  "data": {
    "provinces": [...],
    "featuredTours": [...],
    "featuredBlogs": [...],
    "featuredVideos": [...],
    "featuredArtisans": [...],
    "featuredCultureItems": [...]
  }
}
```

### 2. Tỉnh Thành (Provinces)

**GET** `/api/provinces/public` - Lấy tất cả tỉnh thành

**GET** `/api/provinces/public/{id}` - Lấy tỉnh theo ID

**GET** `/api/provinces/public/slug/{slug}` - Lấy tỉnh theo slug

### 3. Tour

**GET** `/api/tours/public` - Lấy tất cả tour

**GET** `/api/tours/public/{id}` - Lấy tour theo ID

**GET** `/api/tours/public/province/{provinceId}` - Lấy tour theo tỉnh

**GET** `/api/tours/public/artisan/{artisanId}` - Lấy tour theo nghệ nhân

### 4. Văn Hóa (Culture Items)

**GET** `/api/culture-items/public` - Lấy tất cả văn hóa

**GET** `/api/culture-items/public/{id}` - Lấy văn hóa theo ID

**GET** `/api/culture-items/public/category/{category}` - Lấy theo category
- Categories: `FESTIVAL`, `FOOD`, `COSTUME`, `INSTRUMENT`, `DANCE`, `LEGEND`, `CRAFT`

**GET** `/api/culture-items/public/province/{provinceId}` - Lấy theo tỉnh

**GET** `/api/culture-items/public/province/{provinceId}/category/{category}` - Lấy theo tỉnh và category

### 5. Blog Posts

**GET** `/api/blog-posts/public` - Lấy tất cả blog đã publish

**GET** `/api/blog-posts/public/{id}` - Lấy blog theo ID

**GET** `/api/blog-posts/public/slug/{slug}` - Lấy blog theo slug

### 6. Videos

**GET** `/api/videos/public` - Lấy tất cả video đã publish

**GET** `/api/videos/public/{id}` - Lấy video theo ID

**GET** `/api/videos/public/province/{provinceId}` - Lấy video theo tỉnh

### 7. Nghệ Nhân (Artisans)

**GET** `/api/artisans/public` - Lấy tất cả nghệ nhân

**GET** `/api/artisans/public/{id}/detail` - Lấy chi tiết nghệ nhân (cho trang artisan: quick info, gallery, narrative, tours/culture liên quan)

**GET** `/api/artisans/public/{id}` - Lấy nghệ nhân theo ID (entity thô)

**GET** `/api/artisans/public/province/{provinceId}` - Lấy nghệ nhân theo tỉnh

### 8. Ký Ức (User Memories)

**GET** `/api/user-memories/public` - Lấy tất cả ký ức đã publish

**GET** `/api/user-memories/public/{id}` - Lấy ký ức theo ID

**GET** `/api/user-memories/public/province/{provinceId}` - Lấy ký ức theo tỉnh

### 9. Voucher

**GET** `/api/vouchers/public/validate/{code}` - Kiểm tra voucher hợp lệ

**Response:**
```json
{
  "success": true,
  "data": {
    "valid": true,
    "discountType": "PERCENTAGE",
    "discountValue": 10,
    "message": "Voucher hợp lệ"
  }
}
```

---

## Test Các Endpoint Cần Authentication

### 1. Booking (Đặt Tour)

**POST** `/api/bookings` - Tạo booking mới

**Headers:**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Body:**
```json
{
  "tourId": 1,
  "tourScheduleId": 1,
  "numParticipants": 2,
  "contactName": "Nguyễn Văn A",
  "contactPhone": "0123456789",
  "contactEmail": "test@example.com",
  "voucherCode": "DISCOUNT10",
  "paymentMethod": "CREDIT_CARD"
}
```

**Payment Methods:** `CREDIT_CARD`, `BANK_TRANSFER`, `MOMO`, `VNPAY`, `CASH`

**GET** `/api/bookings` - Lấy danh sách booking của user hiện tại

**GET** `/api/bookings/{id}` - Lấy chi tiết booking

**DELETE** `/api/bookings/{id}` - Hủy booking

**Body (optional):**
```json
{
  "reason": "Lý do hủy"
}
```

**POST** `/api/bookings/suggest` - Gợi ý tour (AI)

```json
{
  "provinceId": 1,
  "preferredDate": "2024-02-15",
  "numParticipants": 2
}
```

**GET** `/api/bookings/check-availability?tourScheduleId=1&numParticipants=2` - Kiểm tra khả dụng

**GET** `/api/bookings/{id}/cancellation-fee` - Tính phí hủy tour

### 2. Payment (Thanh Toán)

**POST** `/api/payments/create`

```json
{
  "bookingId": 1,
  "paymentMethod": "MOMO"
}
```

**GET** `/api/payments/{id}` - Lấy thông tin payment

### 3. Review (Đánh Giá)

**POST** `/api/reviews` - Tạo review (multipart - xem phần Upload File)

**Body (Form Data):**
- `bookingId`: 1
- `rating`: 5 (1-5)
- `comment`: "Tour rất hay!"
- `images`: [file1, file2, file3] (tối đa 3 ảnh)

**GET** `/api/reviews/tour/{tourId}` - Lấy review theo tour

**GET** `/api/reviews/my-reviews` - Lấy review của user hiện tại

**GET** `/api/reviews/{id}` - Lấy review theo ID

**DELETE** `/api/reviews/{id}` - Xóa review

### 4. User Memory (Ký Ức Cá Nhân)

**GET** `/api/user-memories/my-memories` - Lấy ký ức của user hiện tại

**POST** `/api/user-memories` - Tạo ký ức mới (multipart)

**PUT** `/api/user-memories/{id}` - Cập nhật ký ức (multipart)

**PUT** `/api/user-memories/{id}/publish` - Publish ký ức

**DELETE** `/api/user-memories/{id}` - Xóa ký ức

### 5. User Profile

**GET** `/api/users/{id}` - Lấy thông tin user

**PUT** `/api/users/{id}` - Cập nhật thông tin user

```json
{
  "fullName": "Tên mới",
  "phone": "0987654321",
  "email": "newemail@example.com"
}
```

**POST** `/api/users/change-password` - Đổi mật khẩu

```json
{
  "oldPassword": "oldpass123",
  "newPassword": "newpass123"
}
```

### 6. Notification (Thông Báo)

**GET** `/api/notifications` - Lấy danh sách thông báo

**GET** `/api/notifications/unread/count` - Đếm thông báo chưa đọc

**PUT** `/api/notifications/{id}/read` - Đánh dấu đã đọc

**PUT** `/api/notifications/read-all` - Đánh dấu tất cả đã đọc

**DELETE** `/api/notifications/{id}` - Xóa thông báo

### 7. Chat

**GET** `/api/chat/conversations` - Lấy danh sách cuộc trò chuyện

**GET** `/api/chat/conversations/{conversationId}/messages` - Lấy tin nhắn

---

## Test Upload File (Multipart)

Các endpoint này yêu cầu gửi `multipart/form-data` thay vì `application/json`.

### 1. Tour

**POST** `/api/tours`

**Headers:**
```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Body (Form Data):**
- `provinceId`: 1
- `title`: "Tour Hà Nội"
- `slug`: "tour-ha-noi" (optional)
- `description`: "Mô tả tour" (optional)
- `durationHours`: 4 (optional)
- `maxParticipants`: 20 (optional)
- `price`: 500000 (optional)
- `artisanId`: 1 (optional)
- `thumbnail`: [file] (optional)
- `images`: [file1, file2, ...] (optional, multiple)

**PUT** `/api/tours/{id}` - Cập nhật (chỉ gửi field cần thay đổi)

### 2. Province

**POST** `/api/provinces`

**Body (Form Data):**
- `name`: "Hà Nội" (required)
- `slug`: "ha-noi" (optional)
- `region`: "Miền Bắc" (optional)
- `latitude`: 21.0285 (optional)
- `longitude`: 105.8542 (optional)
- `description`: "Mô tả" (optional)
- `isActive`: true (optional)
- `thumbnail`: [file] (optional)

### 3. Culture Item

**POST** `/api/culture-items`

**Body (Form Data):**
- `provinceId`: 1 (required)
- `category`: "FESTIVAL" (required)
- `title`: "Lễ hội" (required)
- `description`: "Mô tả" (optional)
- `videoUrl`: "https://youtube.com/..." (optional)
- `thumbnail`: [file] (optional)
- `images`: [file1, file2, ...] (optional, multiple)

### 4. Artisan

**PUT** `/api/artisans/me` (role ARTISAN)  
Nghệ nhân cập nhật hồ sơ của chính mình (ID lấy từ token). Tham số giống POST, chỉ gửi field cần đổi.

**POST** `/api/artisans`

**Body (Form Data):**
- `userId`: 1 (required)
- `fullName`: "Nguyễn Văn A" (required)
- `specialization`: "Làm gốm" (required)
- `bio`: "Tiểu sử" (optional)
- `provinceId`: 1 (optional)
- `workshopAddress`: "Địa chỉ" (optional)
- `ethnicity`: "Mường" (optional)
- `dateOfBirth`: "1959-01-15" (optional, yyyy-MM-dd)
- `heroSubtitle`: "Mô tả hero" (optional)
- `narrativeContent`: [{"title":"...","content":"...","imageUrl":"..."}] (optional, JSON string)
- `profileImage`: [file] (optional)
- `panoramaImage`: [file] (optional)
- `images`: [file1, file2, ...] (optional, multiple)

### 5. Blog Post

**POST** `/api/blog-posts`

**Body (Form Data):**
- `title`: "Tiêu đề" (required)
- `content`: "Nội dung" (required)
- `slug`: "slug-blog" (optional)
- `provinceId`: 1 (optional)
- `featuredImage`: [file] (optional)

### 6. Video

**POST** `/api/videos`

**Body (Form Data):**
- `title`: "Video title" (required)
- `videoUrl`: "https://youtube.com/..." (required)
- `provinceId`: 1 (optional)
- `cultureItemId`: 1 (optional)
- `thumbnail`: [file] (optional)

### 7. User Memory

**POST** `/api/user-memories`

**Body (Form Data):**
- `title`: "Ký ức" (required)
- `description`: "Mô tả" (optional)
- `provinceId`: 1 (optional)
- `images`: [file1, file2, ...] (optional, multiple)
- `audio`: [file] (optional)
- `video`: [file] (optional)

### 8. Review

**POST** `/api/reviews`

**Body (Form Data):**
- `bookingId`: 1 (required)
- `rating`: 5 (required, 1-5)
- `comment`: "Bình luận" (optional)
- `images`: [file1, file2, file3] (optional, tối đa 3 ảnh)

### 9. Upload Avatar

**PUT** `/api/upload/user/avatar`

**Body (Form Data):**
- `file`: [file]

---

## Công Cụ Test

### 1. Postman

#### Tạo Collection
1. Tạo Collection mới: "CoiViet API"
2. Tạo Environment: "Local" với variable `baseUrl = http://localhost:8080`
3. Tạo variable `token` để lưu JWT token

#### Setup Pre-request Script (cho collection)
```javascript
// Tự động thêm token nếu có
if (pm.environment.get("token")) {
    pm.request.headers.add({
        key: "Authorization",
        value: "Bearer " + pm.environment.get("token")
    });
}
```

#### Test Script (cho login request)
```javascript
// Lưu token sau khi login thành công
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    if (jsonData.data && jsonData.data.token) {
        pm.environment.set("token", jsonData.data.token);
        pm.environment.set("userId", jsonData.data.user.id);
    }
}
```

### 2. cURL Examples

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

#### Get Tours (Public)
```bash
curl -X GET http://localhost:8080/api/tours/public
```

#### Create Booking (với token)
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "tourId": 1,
    "tourScheduleId": 1,
    "numParticipants": 2,
    "contactName": "Test User",
    "contactPhone": "0123456789",
    "contactEmail": "test@example.com",
    "paymentMethod": "CASH"
  }'
```

#### Upload File
```bash
curl -X POST http://localhost:8080/api/tours \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -F "provinceId=1" \
  -F "title=Test Tour" \
  -F "thumbnail=@/path/to/image.jpg" \
  -F "images=@/path/to/image1.jpg" \
  -F "images=@/path/to/image2.jpg"
```

### 3. JavaScript/Fetch Examples

#### Login
```javascript
const login = async (username, password) => {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ username, password }),
  });
  
  const data = await response.json();
  if (data.success) {
    localStorage.setItem('token', data.data.token);
    return data.data;
  }
  throw new Error(data.message);
};
```

#### Get Tours
```javascript
const getTours = async () => {
  const response = await fetch('http://localhost:8080/api/tours/public');
  const data = await response.json();
  return data.data;
};
```

#### Create Booking
```javascript
const createBooking = async (bookingData) => {
  const token = localStorage.getItem('token');
  const response = await fetch('http://localhost:8080/api/bookings', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify(bookingData),
  });
  
  const data = await response.json();
  if (!data.success) {
    throw new Error(data.message);
  }
  return data.data;
};
```

#### Upload File
```javascript
const uploadTour = async (tourData, thumbnail, images) => {
  const token = localStorage.getItem('token');
  const formData = new FormData();
  
  // Thêm các field text
  Object.keys(tourData).forEach(key => {
    formData.append(key, tourData[key]);
  });
  
  // Thêm file
  if (thumbnail) {
    formData.append('thumbnail', thumbnail);
  }
  
  if (images && images.length > 0) {
    images.forEach(image => {
      formData.append('images', image);
    });
  }
  
  const response = await fetch('http://localhost:8080/api/tours', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      // KHÔNG set Content-Type, browser sẽ tự set với boundary
    },
    body: formData,
  });
  
  const data = await response.json();
  return data.data;
};
```

### 4. Axios Examples

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
});

// Thêm token vào mọi request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Xử lý response
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      // Token hết hạn, redirect về login
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Sử dụng
const getTours = () => api.get('/tours/public');
const createBooking = (data) => api.post('/bookings', data);
const uploadTour = (formData) => api.post('/tours', formData, {
  headers: { 'Content-Type': 'multipart/form-data' },
});
```

---

## Xử Lý Lỗi

### Các Mã Lỗi Thường Gặp

| Status Code | Mô Tả | Cách Xử Lý |
|------------|-------|-----------|
| 200 | Thành công | Xử lý data bình thường |
| 400 | Bad Request | Kiểm tra format request body |
| 401 | Unauthorized | Token không hợp lệ hoặc hết hạn, cần login lại |
| 403 | Forbidden | Không có quyền truy cập |
| 404 | Not Found | Resource không tồn tại |
| 500 | Internal Server Error | Lỗi server, thử lại sau |

### Ví Dụ Xử Lý Lỗi

```javascript
const handleApiCall = async (apiFunction) => {
  try {
    const data = await apiFunction();
    return { success: true, data };
  } catch (error) {
    if (error.response) {
      // Server trả về lỗi
      const { status, data } = error.response;
      
      switch (status) {
        case 401:
          // Token hết hạn
          localStorage.removeItem('token');
          window.location.href = '/login';
          break;
        case 403:
          alert('Bạn không có quyền thực hiện hành động này');
          break;
        case 404:
          alert('Không tìm thấy dữ liệu');
          break;
        case 500:
          alert('Lỗi server, vui lòng thử lại sau');
          break;
        default:
          alert(data.message || 'Có lỗi xảy ra');
      }
    } else {
      // Lỗi network hoặc lỗi khác
      alert('Không thể kết nối đến server');
    }
    
    return { success: false, error };
  }
};
```

---

## Best Practices

### 1. Quản Lý Token
- Lưu token vào `localStorage` hoặc `sessionStorage`
- Tự động refresh token trước khi hết hạn
- Xóa token khi logout
- Kiểm tra token trước mỗi request cần auth

### 2. Error Handling
- Luôn kiểm tra `response.success` trước khi dùng `data`
- Hiển thị message lỗi rõ ràng cho user
- Log lỗi để debug (development only)

### 3. Loading States
- Hiển thị loading indicator khi đang gọi API
- Disable button/form khi đang submit
- Sử dụng skeleton loading cho danh sách

### 4. File Upload
- Validate file size trước khi upload (max 10MB)
- Validate file type (chỉ cho phép image/video)
- Hiển thị progress bar khi upload
- Preview file trước khi upload

### 5. Caching
- Cache dữ liệu public (provinces, tours) để giảm số lần gọi API
- Invalidate cache khi có thay đổi

### 6. Pagination
- Implement pagination cho danh sách dài
- Sử dụng infinite scroll hoặc "Load More" button

### 7. Testing Checklist

#### Authentication Flow
- [ ] Đăng ký tài khoản mới
- [ ] Đăng nhập với username/password
- [ ] Đăng nhập với Google
- [ ] Refresh token
- [ ] Đăng xuất
- [ ] Quên mật khẩu (forgot → verify OTP → reset)

#### Public Endpoints
- [ ] Lấy dữ liệu trang chủ
- [ ] Lấy danh sách tỉnh thành
- [ ] Lấy danh sách tour
- [ ] Lấy tour theo tỉnh
- [ ] Lấy văn hóa theo category
- [ ] Lấy blog posts
- [ ] Lấy videos
- [ ] Lấy nghệ nhân
- [ ] Validate voucher

#### Authenticated Endpoints
- [ ] Tạo booking
- [ ] Lấy danh sách booking
- [ ] Hủy booking
- [ ] Tạo review
- [ ] Tạo user memory
- [ ] Cập nhật profile
- [ ] Đổi mật khẩu
- [ ] Lấy thông báo

#### File Upload
- [ ] Upload tour với thumbnail và images
- [ ] Upload province thumbnail
- [ ] Upload culture item với images
- [ ] Upload user memory với images/audio/video
- [ ] Upload review images
- [ ] Upload avatar

#### Error Cases
- [ ] Test với token không hợp lệ
- [ ] Test với token hết hạn
- [ ] Test với thiếu required fields
- [ ] Test với file quá lớn
- [ ] Test với file type không hợp lệ

---

## Tài Liệu Tham Khảo

- **API Request Schemas**: Xem file `docs/api-requests.md` để biết chi tiết request body
- **Learn API Test**: Xem file `docs/LEARN_API_TEST.md` để test API tính năng Learn (Học văn hóa)
- **Swagger UI**: Truy cập `http://localhost:8080/swagger-ui.html` để xem API documentation
- **Postman Collection**: Import collection từ file (nếu có)

---

## Hỗ Trợ

Nếu gặp vấn đề khi test API:
1. Kiểm tra server đã chạy chưa
2. Kiểm tra base URL đúng chưa
3. Kiểm tra token còn hợp lệ không
4. Kiểm tra format request body
5. Xem log server để debug
6. Liên hệ backend team để được hỗ trợ

---

**Chúc bạn test API thành công! 🚀**
