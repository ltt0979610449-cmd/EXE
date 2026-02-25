# Learn API - Form Test & Luồng Test

Tài liệu tổng hợp **form data** và **luồng test** cho tất cả API Learn trong một file.

---

## 1. Chuẩn Bị

### Biến môi trường

| Biến | Mô tả | Ví dụ |
|------|-------|-------|
| `BASE_URL` | URL API | `http://localhost:8080` hoặc `https://exe-1-k8ma.onrender.com` |
| `STAFF_TOKEN` | JWT token (STAFF/ADMIN) | Lấy từ `POST /api/auth/login` |
| `USER_TOKEN` | JWT token (USER/STAFF/ADMIN) | Lấy từ `POST /api/auth/login` |
| `MODULE_ID` | ID module vừa tạo | Ghi lại sau bước Create Module |

### Login lấy token

```bash
curl -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"staff_user","password":"password123"}'
```

Lưu `data.token` vào `STAFF_TOKEN` và `USER_TOKEN`.

---

## 2. Form Data - Tất Cả API Cần Form

### 2.1. Create Module

| Field | Type | Bắt buộc | Giá trị mẫu |
|-------|------|----------|-------------|
| categoryId | Long | ✓ | 2 |
| title | String | ✓ | Không gian Cồng Chiêng Tây Nguyên |
| slug | String | | (để trống, auto generate) |
| quickNotesJson | String | | `["Cồng chiêng là nhạc cụ thiêng của người Tây Nguyên","UNESCO công nhận năm 2005","Sử dụng trong lễ hội, nghi lễ, sinh hoạt cộng đồng"]` |
| culturalEtiquetteTitle | String | | Lưu ý khi tham quan |
| culturalEtiquetteText | String | | Không chạm vào cồng chiêng khi chưa được phép. Giữ thái độ tôn trọng. |
| provinceId | Long | | (để trống) |
| orderIndex | Integer | | 1 |
| tourIds | Long[] | | (để trống) |
| thumbnail | File | | (để trống) |

**Content-Type:** `multipart/form-data`

---

### 2.2. Create Lesson (3 bài)

**Lưu ý:** Bắt buộc gửi `Content-Type: multipart/form-data`. Không dùng `application/x-www-form-urlencoded`.

#### Lesson 1: Giới thiệu Cồng Chiêng

| Field | Type | Bắt buộc | Giá trị mẫu |
|-------|------|----------|-------------|
| moduleId | Long | ✓ | `{MODULE_ID}` |
| title | String | ✓ | Giới thiệu Cồng Chiêng |
| slug | String | | gioi-thieu-cong-chieng |
| artisanId | Long | | (để trống) |
| contentJson | String | | `[{"type":"paragraph","text":"Cồng chiêng là nhạc cụ thiêng liêng của các dân tộc Tây Nguyên."}]` |
| vocabularyJson | String | | (để trống) |
| objectiveText | String | | Hiểu về nguồn gốc và ý nghĩa cồng chiêng |
| difficulty | String | | BASIC |
| estimatedMinutes | Integer | | 5 |
| videoUrl | String | | (để trống) |
| orderIndex | Integer | | 1 |
| image | File | | (để trống) |

#### Lesson 2: Vai trò trong đời sống

| Field | Type | Bắt buộc | Giá trị mẫu |
|-------|------|----------|-------------|
| moduleId | Long | ✓ | `{MODULE_ID}` |
| title | String | ✓ | Vai trò trong đời sống |
| slug | String | | vai-tro-trong-doi-song |
| orderIndex | Integer | | 2 |
| difficulty | String | | BASIC |
| estimatedMinutes | Integer | | 5 |

#### Lesson 3: Nghệ nhân Cồng Chiêng

| Field | Type | Bắt buộc | Giá trị mẫu |
|-------|------|----------|-------------|
| moduleId | Long | ✓ | `{MODULE_ID}` |
| title | String | ✓ | Nghệ nhân Cồng Chiêng |
| slug | String | | nghe-nhan-cong-chieng |
| orderIndex | Integer | | 3 |
| difficulty | String | | BASIC |
| estimatedMinutes | Integer | | 5 |

---

### 2.3. Create Quiz

| Field | Type | Bắt buộc | Giá trị mẫu |
|-------|------|----------|-------------|
| moduleId | Long | ✓ | `{MODULE_ID}` |
| title | String | ✓ | Quiz Cồng Chiêng Tây Nguyên |
| timeLimitMinutes | Integer | | 5 |
| difficulty | String | | BASIC |
| objective | String | | Kiểm tra hiểu biết về cồng chiêng |
| rulesJson | String | | `Quy tắc 1\|Quy tắc 2` (phân cách bằng \|) |
| achievementVoucherId | Long | | (để trống) |

**Content-Type:** `application/x-www-form-urlencoded` hoặc `multipart/form-data`

---

### 2.4. Add Quiz Question

**Content-Type:** `application/json`

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

---

## 3. Luồng Test Đầy Đủ

### Phase 1: Public (không cần auth)

| # | Method | Path | Mô tả | Expected |
|---|--------|------|-------|----------|
| 1 | GET | `/api/learn/public/categories` | List Categories | 200, data[] |
| 2 | GET | `/api/learn/public/modules` | List Modules (trước seed) | 200, data[] (có thể rỗng) |
| 3 | GET | `/api/learn/public/modules?categoryId=2` | Modules by Category | 200, data[] |

---

### Phase 2: Staff - Tạo nội dung

| # | Method | Path | Mô tả | Expected |
|---|--------|------|-------|----------|
| 4 | POST | `/api/learn/modules` | Create Module | 200, data.id → lưu vào MODULE_ID |
| 5 | POST | `/api/learn/lessons` | Create Lesson 1 | 200, data (LearnLessonResponse) |
| 6 | POST | `/api/learn/lessons` | Create Lesson 2 | 200, data |
| 7 | POST | `/api/learn/lessons` | Create Lesson 3 | 200, data |
| 8 | POST | `/api/learn/quizzes` | Create Quiz | 200, data (QuizResponse) |
| 9 | POST | `/api/learn/quizzes/{quizId}/questions` | Add Quiz Question | 200, data |
| 10 | PUT | `/api/learn/lessons/{id}/publish` | Publish từng lesson | 200 (x3 lần) |
| 11 | PUT | `/api/learn/quizzes/{id}/publish` | Publish Quiz | 200 |
| 12 | PUT | `/api/learn/modules/{id}/publish` | Publish Module | 200 |

---

### Phase 3: Public - Kiểm tra sau publish

| # | Method | Path | Mô tả | Expected |
|---|--------|------|-------|----------|
| 13 | GET | `/api/learn/public/modules` | List Modules (sau seed) | 200, data[] có module mới |
| 14 | GET | `/api/learn/public/modules/{id}` | Module Detail | 200, data có lessons[], quizPrompt, suggestedTours[] |

---

### Phase 4: User (cần Bearer token)

| # | Method | Path | Mô tả | Expected |
|---|--------|------|-------|----------|
| 15 | GET | `/api/learn/users/me/stats` | Stats | 200, data.totalLessonsCompleted, featuredCourses[] |
| 16 | GET | `/api/learn/users/me/courses` | My Courses | 200, data[] |

---

## 4. cURL - Luồng Test Hoàn Chỉnh

### Bước 0: Login

```bash
BASE_URL="https://exe-1-k8ma.onrender.com"
# hoặc: BASE_URL="http://localhost:8080"

RESP=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"staff_user","password":"password123"}')

STAFF_TOKEN=$(echo $RESP | jq -r '.data.token')
echo "Token: $STAFF_TOKEN"
```

---

### Bước 1: Public - List categories & modules

```bash
curl -X GET "${BASE_URL}/api/learn/public/categories"
curl -X GET "${BASE_URL}/api/learn/public/modules"
curl -X GET "${BASE_URL}/api/learn/public/modules?categoryId=2"
```

---

### Bước 2: Staff - Create Module

```bash
curl -X POST "${BASE_URL}/api/learn/modules" \
  -H "Authorization: Bearer ${STAFF_TOKEN}" \
  -F "categoryId=2" \
  -F "title=Không gian Cồng Chiêng Tây Nguyên" \
  -F "quickNotesJson=[\"Cồng chiêng là nhạc cụ thiêng của người Tây Nguyên\",\"UNESCO công nhận năm 2005\",\"Sử dụng trong lễ hội, nghi lễ, sinh hoạt cộng đồng\"]" \
  -F "culturalEtiquetteTitle=Lưu ý khi tham quan" \
  -F "culturalEtiquetteText=Không chạm vào cồng chiêng khi chưa được phép. Giữ thái độ tôn trọng." \
  -F "orderIndex=1"
```

**Lưu `data.id` từ response → gán vào MODULE_ID (ví dụ: 6)**

---

### Bước 3: Staff - Create Lessons (multipart/form-data)

```bash
MODULE_ID=6  # Thay bằng ID thực tế từ bước 2

# Lesson 1
curl -X POST "${BASE_URL}/api/learn/lessons" \
  -H "Authorization: Bearer ${STAFF_TOKEN}" \
  -F "moduleId=${MODULE_ID}" \
  -F "title=Giới thiệu Cồng Chiêng" \
  -F "contentJson=[{\"type\":\"paragraph\",\"text\":\"Cồng chiêng là nhạc cụ thiêng liêng của các dân tộc Tây Nguyên.\"}]" \
  -F "difficulty=BASIC" \
  -F "estimatedMinutes=5" \
  -F "orderIndex=1"

# Lesson 2
curl -X POST "${BASE_URL}/api/learn/lessons" \
  -H "Authorization: Bearer ${STAFF_TOKEN}" \
  -F "moduleId=${MODULE_ID}" \
  -F "title=Vai trò trong đời sống" \
  -F "difficulty=BASIC" \
  -F "estimatedMinutes=5" \
  -F "orderIndex=2"

# Lesson 3
curl -X POST "${BASE_URL}/api/learn/lessons" \
  -H "Authorization: Bearer ${STAFF_TOKEN}" \
  -F "moduleId=${MODULE_ID}" \
  -F "title=Nghệ nhân Cồng Chiêng" \
  -F "difficulty=BASIC" \
  -F "estimatedMinutes=5" \
  -F "orderIndex=3"
```

---

### Bước 4: Staff - Create Quiz

```bash
curl -X POST "${BASE_URL}/api/learn/quizzes" \
  -H "Authorization: Bearer ${STAFF_TOKEN}" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "moduleId=${MODULE_ID}&title=Quiz Cồng Chiêng Tây Nguyên&timeLimitMinutes=5&difficulty=BASIC"
```

**Lưu `data.id` từ response → gán vào QUIZ_ID**

---

### Bước 5: Staff - Add Quiz Question & Publish

```bash
QUIZ_ID=1  # Thay bằng ID thực tế từ bước 4

curl -X POST "${BASE_URL}/api/learn/quizzes/${QUIZ_ID}/questions" \
  -H "Authorization: Bearer ${STAFF_TOKEN}" \
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

Publish lessons và quiz (lấy ID từ response các bước trước):

```bash
LESSON_ID_1=7  # Thay bằng ID thực tế
LESSON_ID_2=8
LESSON_ID_3=9

curl -X PUT "${BASE_URL}/api/learn/lessons/${LESSON_ID_1}/publish" -H "Authorization: Bearer ${STAFF_TOKEN}"
curl -X PUT "${BASE_URL}/api/learn/lessons/${LESSON_ID_2}/publish" -H "Authorization: Bearer ${STAFF_TOKEN}"
curl -X PUT "${BASE_URL}/api/learn/lessons/${LESSON_ID_3}/publish" -H "Authorization: Bearer ${STAFF_TOKEN}"
curl -X PUT "${BASE_URL}/api/learn/quizzes/${QUIZ_ID}/publish" -H "Authorization: Bearer ${STAFF_TOKEN}"
```

---

### Bước 6: Staff - Publish Module

```bash
curl -X PUT "${BASE_URL}/api/learn/modules/${MODULE_ID}/publish" \
  -H "Authorization: Bearer ${STAFF_TOKEN}"
```

---

### Bước 7: Public - Module Detail

```bash
curl -X GET "${BASE_URL}/api/learn/public/modules/${MODULE_ID}"
```

---

### Bước 8: User - Stats & Courses

```bash
curl -X GET "${BASE_URL}/api/learn/users/me/stats" \
  -H "Authorization: Bearer ${STAFF_TOKEN}"

curl -X GET "${BASE_URL}/api/learn/users/me/courses" \
  -H "Authorization: Bearer ${STAFF_TOKEN}"
```

---

## 5. Checklist Kết Quả Mong Đợi

| Endpoint | Status | Kiểm tra |
|----------|--------|----------|
| GET /api/learn/public/categories | 200 | data[] có 6 categories |
| GET /api/learn/public/modules | 200 | data[] (rỗng hoặc có modules) |
| POST /api/learn/modules | 200 | data.id, data.status=DRAFT |
| POST /api/learn/lessons | 200 | data.id, data.title, data.moduleId |
| POST /api/learn/quizzes | 200 | data.id, data.title, data.moduleId |
| PUT /api/learn/modules/{id}/publish | 200 | data.status=PUBLISHED |
| GET /api/learn/public/modules/{id} | 200 | data.lessons[], data.quizPrompt, data.suggestedTours |
| GET /api/learn/users/me/stats | 200 | data.featuredCourses[] |
| GET /api/learn/users/me/courses | 200 | data[] |

---

## 6. Lưu Ý Quan Trọng

1. **Create Lesson bắt buộc multipart/form-data**  
   Không dùng `application/x-www-form-urlencoded`. FE phải gửi `Content-Type: multipart/form-data` với boundary.

2. **Create Quiz**  
   Có thể dùng `application/x-www-form-urlencoded` hoặc `multipart/form-data`.

3. **Thứ tự publish**  
   Publish lessons và quiz trước, sau đó mới publish module. Module published mới hiển thị ở public.

4. **rulesJson**  
   Phân cách các quy tắc bằng ký tự `|` (pipe).

5. **contentJson, vocabularyJson**  
   Gửi dạng JSON string (escape dấu ngoặc kép nếu cần).
