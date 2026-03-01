# Hướng Dẫn Test Edit Artisan & Ánh Xạ Field Cho Trang FE

Tài liệu này hướng dẫn cách test API chỉnh sửa nghệ nhân (Edit Artisan) và giải thích từng field tương ứng với các vùng trên design trang artisan.

---

## Mục Lục

1. [Chuẩn Bị](#chuẩn-bị)
2. [API Edit Artisan](#api-edit-artisan)
3. [Ánh Xạ Field → Design Trang Artisan](#ánh-xạ-field--design-trang-artisan)
4. [Ví Dụ Test Cụ Thể](#ví-dụ-test-cụ-thể)
5. [Lưu Ý Khi Gửi Request](#lưu-ý-khi-gửi-request)

---

## Chuẩn Bị

### 1. Authentication

Có **2 cách** cập nhật nghệ nhân:

| Endpoint | Role | Mô tả |
|----------|------|-------|
| **PUT** `/api/artisans/me` | ARTISAN | Nghệ nhân cập nhật **hồ sơ của chính mình** (dựa vào token, không cần truyền ID) |
| **PUT** `/api/artisans/{id}` | STAFF, ADMIN | Staff/Admin cập nhật bất kỳ nghệ nhân nào theo ID |

**Bước 1:** Đăng nhập để lấy token

- Nghệ nhân tự sửa: đăng nhập bằng tài khoản có role **ARTISAN**
- Staff/Admin sửa: đăng nhập bằng tài khoản **STAFF** hoặc **ADMIN**

```
POST /api/auth/login
Content-Type: application/json

{
  "username": "artisan_username",
  "password": "password"
}
```

**Bước 2:** Dùng token trong header

```
Authorization: Bearer {accessToken}
```

### 2. Lấy ID nghệ nhân cần sửa

```
GET /api/artisans/public
```

Hoặc xem chi tiết một nghệ nhân:

```
GET /api/artisans/public/{id}/detail
```

---

## API Edit Artisan

### Option 1: Nghệ nhân tự cập nhật (không cần ID)

**PUT** `/api/artisans/me`

- **Role:** ARTISAN
- **ID:** Lấy từ token (userId → artisan)
- **Content-Type:** `multipart/form-data`

### Option 2: Staff/Admin cập nhật theo ID

**PUT** `/api/artisans/{id}`

- **Role:** STAFF, ADMIN
- **Content-Type:** `multipart/form-data`

**Headers:**
```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Quy tắc:** Chỉ gửi các field cần **thay đổi**. Field không gửi sẽ giữ nguyên giá trị cũ.

| Tham số | Kiểu | Bắt buộc | Mô tả |
|---------|------|-----------|-------|
| `fullName` | string | Không | Họ tên nghệ nhân |
| `specialization` | string | Không | Chuyên môn |
| `bio` | string | Không | Tiểu sử / giới thiệu |
| `provinceId` | number | Không | ID tỉnh thành |
| `workshopAddress` | string | Không | Địa chỉ xưởng |
| `ethnicity` | string | Không | Dân tộc |
| `dateOfBirth` | string | Không | Ngày sinh (yyyy-MM-dd) |
| `heroSubtitle` | string | Không | Mô tả ngắn cho hero |
| `narrativeContent` | string (JSON) | Không | Các block câu chuyện |
| `profileImage` | file | Không | Ảnh đại diện |
| `panoramaImage` | file | Không | Ảnh panorama full-width |
| `images` | file[] | Không | Danh sách ảnh gallery |

---

## Ánh Xạ Field → Design Trang Artisan

Dưới đây là bản đồ chi tiết từng vùng trên design trang artisan và field API tương ứng.

### 1. Hero Section (Phần đầu trang, nền tối)

| Vị trí trên design | Field API | Giải thích |
|--------------------|-----------|------------|
| **Tiêu đề lớn** (vd: "NGƯỜI GIỮ HÒN CÔNG CHIÊNG GIA LAI") | `specialization` | Chuyên môn / vai trò của nghệ nhân. FE có thể dùng làm tiêu đề hero hoặc kết hợp với `fullName`. |
| **Mô tả ngắn** (đoạn văn trắng dưới tiêu đề) | `heroSubtitle` | Mô tả ngắn gọn, 1–2 câu cho hero. Ví dụ: "Nghệ nhân Ro Mah H'Blao đã dành cả đời gìn giữ văn hóa cồng chiêng..." |
| **Ảnh lớn bên phải** (nghệ nhân với cồng chiêng) | `profileImageUrl` | Ảnh đại diện chính. Gửi file qua `profileImage` khi create/update. |
| **Nút "Khám phá ngay"** | — | FE tự xử lý (link, scroll, v.v.). Không có field backend. |

---

### 2. Tên Nghệ Nhân (Chữ đỏ lớn)

| Vị trí trên design | Field API | Giải thích |
|--------------------|-----------|------------|
| **Tên nghệ nhân** (vd: "NGHỆ NHÂN RO MAH H'BLAO") | `fullName` | Họ tên đầy đủ của nghệ nhân. |

---

### 3. Quick Info (3 ô thông tin nhanh)

| Ô trên design | Field API | Giải thích |
|---------------|-----------|------------|
| **Dân tộc** (vd: "Mường") | `ethnicity` | Dân tộc của nghệ nhân. Ví dụ: Mường, Jrai, Êđê, Kinh... |
| **Tuổi** (vd: "65") | `dateOfBirth` | Ngày sinh (yyyy-MM-dd). Backend tự tính tuổi từ `dateOfBirth`. FE nhận `age` (Integer) trong response `ArtisanDetailResponse`. |
| **Nơi sinh sống** (vd: "Gia Lai") | `provinceId` | ID tỉnh thành. Backend trả `location` (tên tỉnh) trong `ArtisanDetailResponse`. |

---

### 4. Đoạn Giới Thiệu (Introduction)

| Vị trí trên design | Field API | Giải thích |
|--------------------|-----------|------------|
| **Đoạn văn giới thiệu** (vd: "Ro Mah H'Blao là một trong những nghệ nhân...") | `bio` | Tiểu sử / giới thiệu chi tiết về nghệ nhân. Có thể nhiều đoạn, hỗ trợ xuống dòng. |

---

### 5. Narrative Blocks (Các block câu chuyện)

Mỗi block gồm: **tiêu đề** + **nội dung** + **ảnh nhỏ** (có khung kiểu tem thư).

| Thành phần trong block | Field API | Giải thích |
|------------------------|-----------|------------|
| **Tiêu đề block** (vd: "Từ tiếng chiêng trong ký ức") | `narrativeContent[].title` | Tiêu đề của từng block câu chuyện. |
| **Nội dung block** | `narrativeContent[].content` | Nội dung văn bản của block. |
| **Ảnh nhỏ** (khung tem) | `narrativeContent[].imageUrl` | URL ảnh kèm block. Có thể null nếu block không có ảnh. |

**Format `narrativeContent` (JSON string):**

```json
[
  {
    "title": "Từ tiếng chiêng trong ký ức",
    "content": "Nội dung block 1...",
    "imageUrl": "https://res.cloudinary.com/.../image.jpg"
  },
  {
    "title": "Gìn giữ và lan toả di sản",
    "content": "Nội dung block 2...",
    "imageUrl": null
  }
]
```

**Lưu ý:** Khi gửi qua form-data, `narrativeContent` phải là **string** (escape JSON nếu cần).

---

### 6. Gallery Ảnh Nhỏ (Ảnh trong narrative / gallery chung)

| Vị trí trên design | Field API | Giải thích |
|--------------------|-----------|------------|
| **Các ảnh nhỏ** (nhiều ảnh, có thể trong narrative hoặc gallery riêng) | `images` | Danh sách file ảnh. Backend lưu URL, trả `images` (List<String>) trong `ArtisanDetailResponse`. FE có thể dùng cho gallery chung hoặc ảnh kèm narrative. |

---

### 7. Ảnh Panorama Full-Width

| Vị trí trên design | Field API | Giải thích |
|--------------------|-----------|------------|
| **Ảnh rộng toàn trang** (vd: nhóm người chơi cồng chiêng lúc hoàng hôn) | `panoramaImageUrl` | Ảnh panorama. Gửi file qua `panoramaImage` khi create/update. |

---

### 8. Kết Nối Văn Hóa (Related content)

| Thành phần | Field API | Giải thích |
|------------|-----------|------------|
| **Card "Lễ hội Cồng chiêng tại Gia Lai"** | `relatedCultureItems` | Tự động lấy từ culture items cùng tỉnh (province). Không chỉnh qua edit artisan. |
| **Card "Tour trải nghiệm..."** | `relatedTours` | Tự động lấy từ tours do nghệ nhân này hướng dẫn. Không chỉnh qua edit artisan. |
| **"Xem thêm nghệ nhân khác"** | `otherArtisans` | Tự động lấy nghệ nhân cùng tỉnh. Không chỉnh qua edit artisan. |

**Lưu ý:** Phần Kết nối văn hóa được backend aggregate từ dữ liệu liên quan. FE chỉ cần hiển thị, không có field riêng khi edit artisan.

---

### 9. Các Field Không Hiển Thị Trực Trên Design

| Field | Giải thích |
|-------|------------|
| `workshopAddress` | Địa chỉ xưởng / nơi làm việc. Có thể dùng cho trang chi tiết hoặc thông tin liên hệ. |
| `userId` | User account liên kết với nghệ nhân. Chỉ dùng khi tạo mới. |

---

## Ví Dụ Test Cụ Thể

### Ví dụ 1: Nghệ nhân tự sửa (PUT /api/artisans/me)

**PUT** `/api/artisans/me`

Dùng token của tài khoản ARTISAN. Không cần truyền ID.

**Body (Form Data):**

| Key | Value |
|-----|-------|
| fullName | NGHỆ NHÂN RO MAH H'BLAO |
| ethnicity | Mường |
| dateOfBirth | 1959-03-15 |
| heroSubtitle | Mô tả hero... |

---

### Ví dụ 2: Staff/Admin sửa theo ID (chỉ text)

**PUT** `/api/artisans/1`

**Body (Form Data):**

| Key | Value |
|-----|-------|
| fullName | NGHỆ NHÂN RO MAH H'BLAO |
| specialization | Người giữ hồn cồng chiêng Gia Lai |
| ethnicity | Mường |
| dateOfBirth | 1959-03-15 |
| provinceId | 5 |
| heroSubtitle | Nghệ nhân Ro Mah H'Blao đã dành cả đời gìn giữ và lan toả văn hóa cồng chiêng Tây Nguyên. |
| bio | Ro Mah H'Blao là một trong những nghệ nhân tiêu biểu của Gia Lai. Ông sinh ra và lớn lên trong không gian văn hóa cồng chiêng... |
| narrativeContent | [{"title":"Từ tiếng chiêng trong ký ức","content":"Từ nhỏ, tôi đã được nghe tiếng chiêng vang vọng...","imageUrl":null},{"title":"Gìn giữ và lan toả di sản","content":"Hiện nay tôi đang truyền dạy cho thế hệ trẻ...","imageUrl":null}] |

---

### Ví dụ 3: Sửa kèm upload ảnh mới

**PUT** `/api/artisans/me` hoặc **PUT** `/api/artisans/1`

**Body (Form Data):**

| Key | Type | Value |
|-----|------|-------|
| fullName | text | NGHỆ NHÂN RO MAH H'BLAO |
| profileImage | file | [chọn file ảnh .jpg/.png] |
| panoramaImage | file | [chọn file ảnh panorama] |
| images | file | [chọn nhiều file ảnh] |

**Lưu ý:** Khi gửi `images` mới, toàn bộ ảnh gallery cũ sẽ bị thay thế.

---

### Ví dụ 4: Chỉ thay ảnh profile

**PUT** `/api/artisans/me` hoặc **PUT** `/api/artisans/1`

**Body (Form Data):**

| Key | Type | Value |
|-----|------|-------|
| profileImage | file | [chọn file ảnh] |

Ảnh profile cũ sẽ bị xóa trên Cloudinary, ảnh mới được upload và lưu.

---

## Lưu Ý Khi Gửi Request

### 1. Multipart Form-Data

- Các field text: gửi dạng `application/x-www-form-urlencoded` hoặc `multipart/form-data` (key-value).
- Các field file: gửi dạng `multipart/form-data` với `Content-Disposition: form-data; name="profileImage"; filename="...""`.

### 2. JSON trong narrativeContent

Khi gửi `narrativeContent` qua form-data, cần escape dấu ngoặc kép nếu dùng raw string. Hoặc gửi từ JavaScript:

```javascript
formData.append('narrativeContent', JSON.stringify([
  { title: "Tiêu đề 1", content: "Nội dung 1", imageUrl: null },
  { title: "Tiêu đề 2", content: "Nội dung 2", imageUrl: "https://..." }
]));
```

### 3. Chỉ gửi field cần đổi

- Không gửi field = giữ nguyên giá trị cũ.
- Gửi `null` hoặc chuỗi rỗng có thể được xử lý khác nhau tùy backend; nên kiểm tra hành vi thực tế.

### 4. Xem kết quả sau khi edit

```
GET /api/artisans/public/{id}/detail
```

Response trả về đầy đủ dữ liệu đã format cho FE render trang artisan.

---

## Tóm Tắt Bảng Ánh Xạ Nhanh

**API:** `PUT /api/artisans/me` (ARTISAN) hoặc `PUT /api/artisans/{id}` (STAFF/ADMIN)

| Vùng design | Field khi Edit | Ghi chú |
|-------------|----------------|---------|
| Hero – tiêu đề | specialization | Có thể kết hợp fullName |
| Hero – mô tả ngắn | heroSubtitle | |
| Hero – ảnh lớn | profileImage (file) | |
| Tên nghệ nhân | fullName | |
| Ô Dân tộc | ethnicity | |
| Ô Tuổi | dateOfBirth | Backend tính age |
| Ô Nơi sinh sống | provinceId | Backend trả location |
| Đoạn giới thiệu | bio | |
| Block câu chuyện | narrativeContent (JSON) | title, content, imageUrl |
| Gallery ảnh nhỏ | images (files) | |
| Ảnh panorama | panoramaImage (file) | |
| Kết nối văn hóa | — | Tự động từ backend |
