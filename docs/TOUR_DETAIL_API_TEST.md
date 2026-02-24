# Hướng Dẫn Test API Tour Detail & Culture Items

Tài liệu hướng dẫn test các API endpoints liên quan đến **Tour Detail**, **Province** (thông tin nhanh), và **Tour Culture Items** (địa điểm nổi bật, lễ hội, ẩm thực) trong hệ thống CoiViet.

## Mục Lục

1. [Chuẩn Bị](#chuẩn-bị)
2. [API Public (không cần auth)](#api-public-không-cần-auth)
3. [API Staff (cần role STAFF/ADMIN)](#api-staff-cần-role-staffadmin)
4. [Thứ Tự Test Gợi Ý](#thứ-tự-test-gợi-ý)
5. [Xử Lý Lỗi](#xử-lý-lỗi)

---

## Chuẩn Bị

### Base URL

- **Local**: `http://localhost:8080`
- **Production**: (Cập nhật theo môi trường deploy)

### Lấy Token (cho API Staff)

Đăng nhập qua `POST /api/auth/login` với tài khoản có role STAFF hoặc ADMIN:

```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"staff_user","password":"password123"}'
```

Lưu `data.token` và thêm header cho các request cần auth:

```
Authorization: Bearer {token}
```

### Cấu Trúc Response

Tất cả API trả về format chuẩn:

```json
{
  "success": true,
  "message": "Thông báo",
  "data": { /* dữ liệu */ },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## API Public (không cần auth)

### 1. Lấy Tour theo ID

**GET** `/api/tours/public/{id}`

**cURL:**

```bash
curl -X GET "http://localhost:8080/api/tours/public/1"
```

**Response mẫu:**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Măng Đen 2 ngày 1 đêm",
    "slug": "mang-den-2-ngay-1-dem",
    "description": "...",
    "durationHours": 48,
    "maxParticipants": 10,
    "price": 1500000,
    "thumbnailUrl": "https://...",
    "images": "url1,url2",
    "province": {
      "id": 1,
      "name": "Kon Tum",
      "slug": "kon-tum",
      "region": "Tây Nguyên",
      "latitude": 14.35,
      "longitude": 108.02,
      "description": "...",
      "thumbnailUrl": "https://...",
      "bestSeason": "Tháng 10 - Tháng 3 (mùa khô)",
      "transportation": "Xe máy, xe khách từ Pleiku",
      "culturalTips": "[\"Trang phục lịch sự\", \"Tôn trọng phong tục địa phương\"]"
    },
    "artisan": { "id": 1, "fullName": "..." },
    "averageRating": 4.5,
    "totalBookings": 10,
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:00:00"
  }
}
```

---

### 2. Lấy Tour Detail (kèm Culture Items)

**GET** `/api/tours/public/{id}/detail`

Trả về tour và danh sách culture items (địa điểm nổi bật, lễ hội, ẩm thực) trong một response. Nếu tour chưa gắn items → fallback lấy theo province.

**cURL:**

```bash
curl -X GET "http://localhost:8080/api/tours/public/1/detail"
```

**Response mẫu:**

```json
{
  "success": true,
  "data": {
    "tour": {
      "id": 1,
      "title": "Măng Đen 2 ngày 1 đêm",
      "province": { "id": 1, "name": "Kon Tum", "bestSeason": "...", "transportation": "...", "culturalTips": "..." },
      "..."
    },
    "cultureItems": [
      {
        "id": 1,
        "title": "Thác Pa Sỹ",
        "category": "CRAFT",
        "description": "...",
        "thumbnailUrl": "https://...",
        "videoUrl": null
      },
      {
        "id": 2,
        "title": "Lễ hội cầu mùa",
        "category": "FESTIVAL",
        "..."
      }
    ]
  }
}
```

---

### 3. Lấy Culture Items của Tour

**GET** `/api/tours/public/{id}/culture-items`  
**GET** `/api/tours/public/{id}/culture-items?category=FOOD`

Lấy địa điểm nổi bật, lễ hội, ẩm thực gắn với tour. Có thể filter theo category.

**Categories:** `FESTIVAL`, `FOOD`, `COSTUME`, `INSTRUMENT`, `DANCE`, `LEGEND`, `CRAFT`

**cURL:**

```bash
# Tất cả culture items
curl -X GET "http://localhost:8080/api/tours/public/1/culture-items"

# Chỉ ẩm thực
curl -X GET "http://localhost:8080/api/tours/public/1/culture-items?category=FOOD"

# Chỉ lễ hội
curl -X GET "http://localhost:8080/api/tours/public/1/culture-items?category=FESTIVAL"
```

**Response mẫu:**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "Thác Pa Sỹ",
      "category": "CRAFT",
      "description": "...",
      "thumbnailUrl": "https://...",
      "images": "...",
      "videoUrl": null,
      "province": { "id": 1, "name": "Kon Tum" }
    }
  ]
}
```

---

### 4. Lấy Địa điểm nổi bật (Highlights)

**GET** `/api/tours/public/{id}/highlights`

Alias cho `culture-items` không filter. Fallback theo province nếu tour chưa gắn items.

**cURL:**

```bash
curl -X GET "http://localhost:8080/api/tours/public/1/highlights"
```

---

### 5. Lấy Province (có bestSeason, transportation, culturalTips)

**GET** `/api/provinces/public/{id}`

**cURL:**

```bash
curl -X GET "http://localhost:8080/api/provinces/public/1"
```

**Response mẫu:**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Kon Tum",
    "slug": "kon-tum",
    "region": "Tây Nguyên",
    "latitude": 14.35,
    "longitude": 108.02,
    "description": "...",
    "thumbnailUrl": "https://...",
    "bestSeason": "Tháng 10 - Tháng 3 (mùa khô, thời tiết mát mẻ)",
    "transportation": "Xe máy, xe khách từ Pleiku",
    "culturalTips": "[\"Trang phục lịch sự\", \"Tôn trọng phong tục địa phương\", \"Xin phép khi chụp ảnh\"]",
    "isActive": true
  }
}
```

---

## API Staff (cần role STAFF/ADMIN)

### 1. Tạo Province (có bestSeason, transportation, culturalTips)

**POST** `/api/provinces`  
**Content-Type:** `multipart/form-data`

**Tham số:**

| Tham số         | Bắt buộc | Mô tả                                      |
|-----------------|----------|--------------------------------------------|
| name            | Có       | Tên tỉnh thành                             |
| slug            | Không    | Slug (unique)                               |
| region          | Không    | Vùng miền                                  |
| latitude        | Không    | Vĩ độ                                      |
| longitude       | Không    | Kinh độ                                    |
| description     | Không    | Mô tả                                      |
| bestSeason      | Không    | Thời điểm đẹp nhất (e.g. Tháng 10 - Tháng 3) |
| transportation  | Không    | Cách di chuyển (e.g. Xe máy, xe khách từ Pleiku) |
| culturalTips    | Không    | Lưu ý ứng xử văn hoá (JSON array hoặc text) |
| isActive        | Không    | Trạng thái (mặc định: true)                |
| thumbnail       | Không    | File ảnh thumbnail                         |

**cURL:**

```bash
curl -X POST "http://localhost:8080/api/provinces" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "name=Kon Tum" \
  -F "region=Tây Nguyên" \
  -F "bestSeason=Tháng 10 - Tháng 3 (mùa khô, thời tiết mát mẻ)" \
  -F "transportation=Xe máy, xe khách từ Pleiku" \
  -F "culturalTips=[\"Trang phục lịch sự\", \"Tôn trọng phong tục địa phương\"]"
```

---

### 2. Cập nhật Province

**PUT** `/api/provinces/{id}`  
**Content-Type:** `multipart/form-data`

**cURL:**

```bash
curl -X PUT "http://localhost:8080/api/provinces/1" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "bestSeason=Tháng 11 - Tháng 4" \
  -F "transportation=Xe máy, ô tô, xe khách" \
  -F "culturalTips=[\"Trang phục lịch sự\", \"Xin phép khi chụp ảnh\", \"Không chạm đồ thờ cúng\"]"
```

---

### 3. Tạo Tour (có cultureItemIds)

**POST** `/api/tours`  
**Content-Type:** `multipart/form-data`

**Tham số:**

| Tham số        | Bắt buộc | Mô tả                                                       |
|----------------|----------|-------------------------------------------------------------|
| provinceId     | Có       | ID tỉnh thành                                              |
| title          | Có       | Tiêu đề tour                                                |
| slug           | Không    | Slug                                                        |
| description    | Không    | Mô tả                                                       |
| durationHours  | Không    | Số giờ tour                                                 |
| maxParticipants| Không    | Số người tối đa                                             |
| price          | Không    | Giá tour                                                    |
| artisanId      | Không    | ID nghệ nhân                                                |
| cultureItemIds | Không    | Danh sách ID culture items (nhiều param: cultureItemIds=1&cultureItemIds=2) |
| thumbnail      | Không    | File ảnh thumbnail                                          |
| images         | Không    | Danh sách ảnh (nhiều file)                                  |

**cURL:**

```bash
curl -X POST "http://localhost:8080/api/tours" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "provinceId=1" \
  -F "title=Măng Đen 2 ngày 1 đêm" \
  -F "description=Khám phá Măng Đen" \
  -F "cultureItemIds=1" \
  -F "cultureItemIds=2" \
  -F "cultureItemIds=3"
```

---

### 4. Cập nhật Tour (có cultureItemIds)

**PUT** `/api/tours/{id}`  
**Content-Type:** `multipart/form-data`

Truyền `cultureItemIds` để thay thế toàn bộ culture items. Truyền rỗng để xóa hết.

**cURL:**

```bash
# Cập nhật culture items
curl -X PUT "http://localhost:8080/api/tours/1" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "cultureItemIds=1" \
  -F "cultureItemIds=2" \
  -F "cultureItemIds=5"

# Xóa hết culture items (không truyền cultureItemIds hoặc truyền rỗng)
curl -X PUT "http://localhost:8080/api/tours/1" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "cultureItemIds="
```

---

### 5. Gắn Culture Items vào Tour

**POST** `/api/tours/{id}/culture-items`

Thay thế toàn bộ culture items của tour. Truyền rỗng để xóa hết.

**cURL:**

```bash
# Gắn 3 culture items (thứ tự = thứ tự hiển thị)
curl -X POST "http://localhost:8080/api/tours/1/culture-items" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "cultureItemIds=1" \
  -F "cultureItemIds=2" \
  -F "cultureItemIds=3"

# Xóa hết
curl -X POST "http://localhost:8080/api/tours/1/culture-items" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 6. Bỏ Culture Item khỏi Tour

**DELETE** `/api/tours/{id}/culture-items/{cultureItemId}`

**cURL:**

```bash
curl -X DELETE "http://localhost:8080/api/tours/1/culture-items/3" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Thứ Tự Test Gợi Ý

1. **Chuẩn bị dữ liệu**
   - Tạo/cập nhật Province với bestSeason, transportation, culturalTips
   - Lấy danh sách CultureItem: `GET /api/culture-items/public/province/1`

2. **Test Public API**
   - `GET /api/tours/public/1` — kiểm tra province có bestSeason, transportation, culturalTips
   - `GET /api/tours/public/1/detail` — kiểm tra tour + cultureItems
   - `GET /api/tours/public/1/culture-items` — kiểm tra fallback theo province (khi tour chưa gắn items)
   - `GET /api/tours/public/1/culture-items?category=FOOD` — filter theo category

3. **Test Staff API**
   - `POST /api/tours` với cultureItemIds — tạo tour có gắn items
   - `GET /api/tours/public/1/culture-items` — kiểm tra trả về đúng items đã gắn
   - `POST /api/tours/1/culture-items` — thay thế items
   - `DELETE /api/tours/1/culture-items/3` — bỏ 1 item
   - `PUT /api/tours/1` với cultureItemIds rỗng — xóa hết items

4. **Test Fallback**
   - Tạo tour không gắn cultureItemIds
   - Gọi `GET /api/tours/public/1/culture-items` — phải trả về items theo province
   - Gắn items cho tour
   - Gọi lại — phải trả về items đã gắn (không còn fallback)

---

## Xử Lý Lỗi

| Mã | Message                    | Nguyên nhân                          |
|----|----------------------------|--------------------------------------|
| 404 | Tour không tồn tại        | ID tour sai hoặc đã xóa              |
| 404 | Tỉnh thành không tồn tại  | ID province sai                       |
| 404 | CultureItem không tồn tại | ID culture item sai khi gắn vào tour |
| 401 | Unauthorized               | Thiếu hoặc token không hợp lệ        |
| 403 | Forbidden                  | Tài khoản không có role STAFF/ADMIN  |

---

## Ghi Chú cho Frontend

- **Province.bestSeason, transportation, culturalTips**: Dùng cho section "Thông tin nhanh" trên TourDetail. Nếu null → ẩn hoặc hiện placeholder.
- **Tour culture items**: Dùng cho sections "Địa điểm nổi bật", "Lễ hội - Phong tục", "Ẩm thực địa phương". FE filter theo `category`:
  - Địa điểm nổi bật: CRAFT, INSTRUMENT, COSTUME
  - Lễ hội: FESTIVAL, DANCE, LEGEND
  - Ẩm thực: FOOD
- **Fallback**: Khi tour chưa gắn items, API trả về CultureItem theo province. FE không cần xử lý riêng.
- **Video**: Dùng `GET /api/videos/public/province/{provinceId}` — không nằm trong Tour API.
