## API Request Schemas (FE Guide)

Tài liệu này tóm tắt dữ liệu đầu vào cần gửi cho các endpoint chính.  
Quy ước:
- `required` = bắt buộc
- `optional` = có thể bỏ trống
- Với `multipart/form-data`, các trường file gửi dạng file, các trường còn lại gửi dạng text.

### Auth
**POST** `/api/auth/login`  
`LoginRequest`:
- `username` (required)
- `password` (required)

**POST** `/api/auth/refresh`  
`RefreshTokenRequest`:
- `refreshToken` (required)

---

### Booking
**POST** `/api/bookings`  
`CreateBookingRequest`:
- `tourId` (required)
- `tourScheduleId` (required)
- `numParticipants` (required, >= 1)
- `contactName` (required)
- `contactPhone` (required)
- `contactEmail` (required, email)
- `voucherCode` (optional)
- `paymentMethod` (required: `CREDIT_CARD | BANK_TRANSFER | MOMO | VNPAY | CASH`)

**DELETE** `/api/bookings/{id}`  
`CancelBookingRequest` (body optional):
- `reason` (optional)

**POST** `/api/bookings/suggest`  
`SuggestTourRequest`:
- `provinceId` (required)
- `preferredDate` (optional, `yyyy-MM-dd`)
- `numParticipants` (optional)

---

### Payment
**POST** `/api/payments/create`  
`CreatePaymentRequest`:
- `bookingId` (required)
- `paymentMethod` (required: `MOMO | VNPAY | CASH | ...`)

---

### Tour (multipart)
**POST** `/api/tours`  
`CreateTourRequest` (multipart):
- `provinceId` (required)
- `title` (required)
- `slug` (optional)
- `description` (optional)
- `durationHours` (optional)
- `maxParticipants` (optional)
- `price` (optional)
- `artisanId` (optional)
- `thumbnail` (file, optional)
- `images` (files[], optional)

**PUT** `/api/tours/{id}`  
`UpdateTourRequest` (multipart): gửi **chỉ** field cần thay đổi  
- `provinceId`, `title`, `slug`, `description`, `durationHours`, `maxParticipants`, `price`, `artisanId` (optional)
- `thumbnail` (file, optional)
- `images` (files[], optional)

---

### Province (multipart)
**POST** `/api/provinces`  
`CreateProvinceRequest` (multipart):
- `name` (required)
- `slug`, `region`, `latitude`, `longitude`, `description`, `isActive` (optional)
- `thumbnail` (file, optional)

**PUT** `/api/provinces/{id}`  
`UpdateProvinceRequest` (multipart): gửi **chỉ** field cần thay đổi  
- `name`, `slug`, `region`, `latitude`, `longitude`, `description`, `isActive` (optional)
- `thumbnail` (file, optional)

---

### Culture Item (multipart)
**POST** `/api/culture-items`  
`CreateCultureItemRequest`:
- `provinceId` (required)
- `category` (required: `FESTIVAL | FOOD | COSTUME | INSTRUMENT | DANCE | LEGEND | CRAFT`)
- `title` (required)
- `description`, `videoUrl` (optional)
- `thumbnail` (file, optional)
- `images` (files[], optional)

**PUT** `/api/culture-items/{id}`  
`UpdateCultureItemRequest`: gửi **chỉ** field cần thay đổi  
- `provinceId`, `category`, `title`, `description`, `videoUrl` (optional)
- `thumbnail` (file, optional)
- `images` (files[], optional)

---

### Artisan (multipart)
**POST** `/api/artisans`  
`CreateArtisanRequest`:
- `userId` (required)
- `fullName` (required)
- `specialization` (required)
- `bio`, `provinceId`, `workshopAddress` (optional)
- `profileImage` (file, optional)

**PUT** `/api/artisans/{id}`  
`UpdateArtisanRequest`: gửi **chỉ** field cần thay đổi  
- `fullName`, `specialization`, `bio`, `provinceId`, `workshopAddress` (optional)
- `profileImage` (file, optional)

---

### Blog Post (multipart)
**POST** `/api/blog-posts`  
`CreateBlogPostRequest`:
- `title` (required)
- `content` (required)
- `slug`, `provinceId` (optional)
- `featuredImage` (file, optional)

**PUT** `/api/blog-posts/{id}`  
`UpdateBlogPostRequest`: gửi **chỉ** field cần thay đổi  
- `title`, `content`, `slug`, `provinceId` (optional)
- `featuredImage` (file, optional)

---

### Video (multipart)
**POST** `/api/videos`  
`CreateVideoRequest`:
- `title` (required)
- `videoUrl` (required)
- `provinceId`, `cultureItemId` (optional)
- `thumbnail` (file, optional)

**PUT** `/api/videos/{id}`  
`UpdateVideoRequest`: gửi **chỉ** field cần thay đổi  
- `title`, `videoUrl`, `provinceId`, `cultureItemId` (optional)
- `thumbnail` (file, optional)

---

### User Memory (multipart)
**POST** `/api/user-memories`  
`CreateUserMemoryRequest`:
- `title` (required)
- `description`, `provinceId` (optional)
- `images` (files[], optional)
- `audio` (file, optional)
- `video` (file, optional)

**PUT** `/api/user-memories/{id}`  
`UpdateUserMemoryRequest`: gửi **chỉ** field cần thay đổi  
- `title`, `description`, `provinceId` (optional)
- `images` (files[], optional)
- `audio` (file, optional)
- `video` (file, optional)

---

### Review (multipart)
**POST** `/api/reviews`  
`CreateReviewRequest`:
- `bookingId` (required)
- `rating` (required, 1-5)
- `comment` (optional)
- `images` (files[], optional, tối đa 3 ảnh)

---

### Voucher
**POST** `/api/vouchers`  
`CreateVoucherRequest`:
- `code` (required)
- `discountType` (required: `PERCENTAGE | FIXED`)
- `discountValue` (required)
- `minPurchase`, `maxUsage` (optional)
- `validFrom` (required, `yyyy-MM-dd'T'HH:mm:ss`)
- `validUntil` (required, `yyyy-MM-dd'T'HH:mm:ss`)
- `isActive` (optional)

**PUT** `/api/vouchers/{id}`  
`UpdateVoucherRequest`: gửi **chỉ** field cần thay đổi  
- `code`, `discountType`, `discountValue`, `minPurchase`, `maxUsage`, `validFrom`, `validUntil`, `isActive` (optional)

---

### Tour Schedule
**POST** `/api/tour-schedules`  
`CreateTourScheduleRequest`:
- `tourId` (required)
- `tourDate` (required, `yyyy-MM-dd`)
- `startTime` (required, `HH:mm:ss` hoặc `HH:mm`)
- `maxSlots` (required, >= 1)
- `currentPrice` (optional)
- `discountPercent` (optional)
- `status` (optional: `SCHEDULED | CANCELLED | COMPLETED | FULL`)

**PUT** `/api/tour-schedules/{id}`  
`UpdateTourScheduleRequest`: gửi **chỉ** field cần thay đổi  
- `tourId`, `tourDate`, `startTime`, `maxSlots`, `currentPrice`, `discountPercent`, `status` (optional)
