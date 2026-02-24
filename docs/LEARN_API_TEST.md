# Hướng Dẫn Test API Learn cho Frontend

Tài liệu hướng dẫn test các API endpoints của tính năng **Learn** (Học văn hóa) trong hệ thống CoiViet.

## Mục Lục
1. [Chuẩn Bị](#chuẩn-bị)
2. [API Public (không cần auth)](#api-public-không-cần-auth)
3. [API User (cần Bearer token)](#api-user-cần-bearer-token)
4. [API Staff (cần role STAFF/ADMIN)](#api-staff-cần-role-staffadmin)
5. [Thứ Tự Test Gợi Ý](#thứ-tự-test-gợi-ý)
6. [Xử Lý Lỗi](#xử-lý-lỗi)

---

## Chuẩn Bị

### Base URL
- **Local**: `http://localhost:8080`
- **Production**: (Cập nhật theo môi trường deploy)

### Lấy Token (cho API User & Staff)
Đăng nhập qua `POST /api/auth/login` để lấy token:

```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
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

### 1. Danh sách Category

**GET** `/api/learn/public/categories`

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/learn/public/categories"
```

**Response mẫu:**
```json
{
  "success": true,
  "data": [
    { "id": 1, "name": "Tất cả", "slug": "tat-ca", "orderIndex": 0 },
    { "id": 2, "name": "Cồng chiêng", "slug": "cong-chieng", "orderIndex": 1 }
  ]
}
```

---

### 2. Danh sách Module

**GET** `/api/learn/public/modules`  
**GET** `/api/learn/public/modules?categoryId=2`

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/learn/public/modules"
curl -X GET "http://localhost:8080/api/learn/public/modules?categoryId=2"
```

**Response mẫu:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "Không gian Cồng Chiêng",
      "slug": "khong-gian-cong-chieng",
      "thumbnailUrl": "https://...",
      "categoryId": 2,
      "categoryName": "Cồng chiêng",
      "lessonsCount": 6,
      "durationMinutes": 18
    }
  ]
}
```

---

### 3. Chi tiết Module

**GET** `/api/learn/public/modules/{id}`

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/learn/public/modules/1"
```

**Response:** Bao gồm `lessons[]`, `quizPrompt`, `suggestedTours[]`, `quickNotesJson`, `culturalEtiquetteTitle`, `culturalEtiquetteText`.

---

### 4. Chi tiết Bài học

**GET** `/api/learn/public/lessons/{id}`

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/learn/public/lessons/1"
```

**Response mẫu:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Giới thiệu Cồng Chiêng",
    "slug": "gioi-thieu-cong-chieng",
    "imageUrl": "https://...",
    "contentJson": "[{\"type\":\"paragraph\",\"text\":\"...\"}]",
    "vocabularyJson": "[{\"term\":\"...\",\"definition\":\"...\"}]",
    "objectiveText": "Hiểu về cồng chiêng Tây Nguyên",
    "difficulty": "BASIC",
    "estimatedMinutes": 5,
    "videoUrl": "https://youtube.com/...",
    "viewsCount": 100,
    "author": {
      "id": 1,
      "fullName": "Nghệ nhân A",
      "profileImageUrl": "https://..."
    },
    "moduleId": 1,
    "moduleTitle": "Không gian Cồng Chiêng",
    "categoryName": "Cồng chiêng"
  }
}
```

---

### 5. Lấy đề Quiz

**GET** `/api/learn/public/quizzes/{id}`

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/learn/public/quizzes/1"
```

**Response:** Quiz với `questions[]`, mỗi option **không có** `isCorrect` (ẩn đáp án đúng).

---

## API User (cần Bearer token)

**Header bắt buộc:** `Authorization: Bearer {token}`  
**Quyền:** USER, STAFF, hoặc ADMIN

---

### 1. Đánh dấu hoàn thành bài

**POST** `/api/learn/lessons/{id}/complete`

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/learn/lessons/1/complete" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 2. Like / Bỏ like bài

**POST** `/api/learn/lessons/{id}/like` — Like bài  
**DELETE** `/api/learn/lessons/{id}/like` — Bỏ like

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/learn/lessons/1/like" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 3. Lưu / Bỏ lưu bài

**POST** `/api/learn/lessons/{id}/save` — Lưu bài  
**DELETE** `/api/learn/lessons/{id}/save` — Bỏ lưu

---

### 4. Theo dõi / Bỏ theo dõi nghệ nhân

**POST** `/api/learn/artisans/{id}/follow` — Theo dõi  
**DELETE** `/api/learn/artisans/{id}/follow` — Bỏ theo dõi

---

### 5. Nộp Quiz

**POST** `/api/learn/quizzes/{id}/submit`  
**Content-Type:** `application/json`

**Body:**
```json
{
  "answers": {
    "1": 3,
    "2": 4,
    "3": 2,
    "4": 1,
    "5": 1
  },
  "timeTakenSeconds": 204
}
```

- `answers`: Map `questionId` (string) → `optionId` (number)
- `timeTakenSeconds`: Thời gian làm bài (giây)

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/learn/quizzes/1/submit" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"answers":{"1":3,"2":4,"3":2,"4":1,"5":1},"timeTakenSeconds":204}'
```

**Response mẫu:**
```json
{
  "success": true,
  "data": {
    "attemptId": 1,
    "correctCount": 4,
    "totalQuestions": 5,
    "scorePercent": 80,
    "timeTakenSeconds": 204,
    "questionResults": [
      {
        "questionId": 1,
        "selectedOptionId": 3,
        "correctOptionId": 3,
        "isCorrect": true,
        "explanationText": "..."
      }
    ],
    "suggestedTours": [
      { "id": 1, "title": "Tour Cồng Chiêng", "slug": "...", "thumbnailUrl": "...", "price": 500000 }
    ],
    "canClaimVoucher": false
  }
}
```

---

### 6. Thống kê học tập

**GET** `/api/learn/users/me/stats`

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/learn/users/me/stats" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response:** `totalLessonsCompleted`, `averageScore`, `learningStreak`, `featuredCourses[]`.

---

### 7. Khóa đang học

**GET** `/api/learn/users/me/courses`

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/learn/users/me/courses" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 8. Nhận voucher (đạt 100%)

**POST** `/api/learn/achievements/{attemptId}/claim-voucher`

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/learn/achievements/1/claim-voucher" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## API Staff (cần role STAFF/ADMIN)

**Header:** `Authorization: Bearer {token}`  
**Quyền:** Chỉ STAFF hoặc ADMIN

---

### 1. Tạo Module

**POST** `/api/learn/modules`  
**Content-Type:** `multipart/form-data`

**Form fields:**
| Field | Type | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| categoryId | Long | ✓ | ID category |
| title | String | ✓ | Tiêu đề module |
| slug | String | | Slug URL |
| quickNotesJson | String | | JSON array ghi chú nhanh |
| culturalEtiquetteTitle | String | | Tiêu đề văn hóa ứng xử |
| culturalEtiquetteText | String | | Nội dung văn hóa ứng xử |
| provinceId | Long | | ID tỉnh (optional) |
| orderIndex | Integer | | Thứ tự hiển thị |
| tourIds | Long[] | | Danh sách ID tour gợi ý |
| thumbnail | File | | Ảnh thumbnail |

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/learn/modules" \
  -H "Authorization: Bearer STAFF_TOKEN" \
  -F "categoryId=2" \
  -F "title=Không gian Cồng Chiêng" \
  -F "quickNotesJson=[\"Ghi chú 1\",\"Ghi chú 2\"]" \
  -F "culturalEtiquetteTitle=Điều nên lưu ý" \
  -F "culturalEtiquetteText=Hãy giữ thái độ tôn trọng..." \
  -F "thumbnail=@/path/to/image.jpg"
```

---

### 2. Cập nhật Module

**PUT** `/api/learn/modules/{id}`  
**Content-Type:** `multipart/form-data`

Các field giống tạo module, tất cả optional (chỉ gửi field cần cập nhật).

---

### 3. Publish Module

**PUT** `/api/learn/modules/{id}/publish`

---

### 4. Xóa Module

**DELETE** `/api/learn/modules/{id}`

---

### 5. Tạo Lesson

**POST** `/api/learn/lessons`  
**Content-Type:** `multipart/form-data`

**Form fields:**
| Field | Type | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| moduleId | Long | ✓ | ID module |
| title | String | ✓ | Tiêu đề bài học |
| slug | String | | Slug URL |
| artisanId | Long | | ID nghệ nhân (tác giả) |
| contentJson | String | | JSON nội dung bài |
| vocabularyJson | String | | JSON từ vựng |
| objectiveText | String | | Mục tiêu bài học |
| difficulty | String | | BASIC, INTERMEDIATE, ADVANCED |
| estimatedMinutes | Integer | | Thời lượng (phút) |
| videoUrl | String | | URL video |
| orderIndex | Integer | | Thứ tự |
| image | File | | Ảnh bài học |

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/learn/lessons" \
  -H "Authorization: Bearer STAFF_TOKEN" \
  -F "moduleId=1" \
  -F "title=Giới thiệu Cồng Chiêng" \
  -F "contentJson=[{\"type\":\"paragraph\",\"text\":\"Nội dung...\"}]" \
  -F "difficulty=BASIC" \
  -F "estimatedMinutes=5" \
  -F "image=@/path/to/image.jpg"
```

---

### 6. Cập nhật Lesson

**PUT** `/api/learn/lessons/{id}`  
**Content-Type:** `multipart/form-data`

---

### 7. Publish Lesson

**PUT** `/api/learn/lessons/{id}/publish`

---

### 8. Xóa Lesson

**DELETE** `/api/learn/lessons/{id}`

---

### 9. Tạo Quiz

**POST** `/api/learn/quizzes`  
**Content-Type:** `application/x-www-form-urlencoded` hoặc `multipart/form-data`

**Form fields:**
| Field | Type | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| moduleId | Long | ✓ | ID module |
| title | String | ✓ | Tiêu đề quiz |
| timeLimitMinutes | Integer | | Giới hạn thời gian (phút) |
| difficulty | String | | BASIC, INTERMEDIATE, ADVANCED |
| objective | String | | Mục tiêu quiz |
| rulesJson | String | | Quy tắc (phân cách bằng \|) |
| achievementVoucherId | Long | | ID voucher khi đạt 100% |

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/learn/quizzes" \
  -H "Authorization: Bearer STAFF_TOKEN" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "moduleId=1&title=Quiz Cồng Chiêng&timeLimitMinutes=5&difficulty=BASIC"
```

---

### 10. Cập nhật Quiz

**PUT** `/api/learn/quizzes/{id}`  
**Content-Type:** `application/x-www-form-urlencoded`

---

### 11. Thêm câu hỏi vào Quiz

**POST** `/api/learn/quizzes/{quizId}/questions`  
**Content-Type:** `application/json`

**Body:**
```json
{
  "questionText": "Cồng chiêng Tây Nguyên là nhạc cụ truyền thống của dân tộc nào?",
  "hintText": "Gợi ý: vùng Tây Nguyên",
  "explanationText": "Cồng chiêng là nhạc cụ truyền thống đặc trưng của các dân tộc Tây Nguyên.",
  "orderIndex": 1,
  "options": [
    { "label": "A", "optionText": "Dân tộc Kinh", "isCorrect": false },
    { "label": "B", "optionText": "Dân tộc Ê-đê", "isCorrect": true },
    { "label": "C", "optionText": "Dân tộc Tày", "isCorrect": false },
    { "label": "D", "optionText": "Dân tộc Mường", "isCorrect": false }
  ]
}
```

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/learn/quizzes/1/questions" \
  -H "Authorization: Bearer STAFF_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "questionText": "Cồng chiêng Tây Nguyên là nhạc cụ truyền thống của dân tộc nào?",
    "hintText": "Gợi ý: vùng Tây Nguyên",
    "explanationText": "Cồng chiêng là nhạc cụ truyền thống đặc trưng của các dân tộc Tây Nguyên.",
    "orderIndex": 1,
    "options": [
      {"label":"A","optionText":"Dân tộc Kinh","isCorrect":false},
      {"label":"B","optionText":"Dân tộc Ê-đê","isCorrect":true},
      {"label":"C","optionText":"Dân tộc Tày","isCorrect":false},
      {"label":"D","optionText":"Dân tộc Mường","isCorrect":false}
    ]
  }'
```

---

### 12. Xóa Quiz

**DELETE** `/api/learn/quizzes/{id}`

---

## Thứ Tự Test Gợi Ý

1. **Public:** GET categories → GET modules → GET module detail → GET lesson → GET quiz
2. **Auth:** Login → GET stats → POST complete → POST like → POST submit quiz
3. **Staff:** Tạo module → publish → tạo lesson → publish → tạo quiz → thêm câu hỏi → publish quiz

---

## Xử Lý Lỗi

| Status | Mô tả |
|--------|-------|
| 401 | Thiếu hoặc sai token — cần login lại |
| 403 | Không đủ quyền (Staff API cần STAFF/ADMIN) |
| 404 | ID không tồn tại hoặc module/lesson chưa PUBLISHED |

### Lưu ý Submit Quiz
- `answers` phải là object với key là **string** (questionId), value là **number** (optionId)
- Ví dụ: `{"1": 3, "2": 4}` — câu 1 chọn option 3, câu 2 chọn option 4

---

## Tài Liệu Liên Quan

- **Hướng dẫn test API chung:** `docs/HUONG_DAN_FE_TEST_API.md`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
