# LinguaMastery API

[English](#english) | [繁體中文](#繁體中文)

---

## 繁體中文

### 專案簡介

LinguaMastery 後端 API，基於 Spring Boot 開發，提供語言學習平台所需的使用者驗證、單字書管理與學習記錄等功能。

### 技術棧

- **框架**：Spring Boot 3.3.5
- **語言**：Java 21
- **資料庫**：PostgreSQL
- **ORM**：Spring Data JPA / Hibernate
- **驗證**：JWT（JSON Web Token）
- **Email**：Resend HTTP API
- **建置工具**：Gradle
- **部署**：Render（後端）、Vercel（前端）

### 主要功能

- 使用者註冊 / 登入（JWT 驗證）
- Email 驗證（註冊後需驗證才能登入）
- 忘記密碼 / 重設密碼
- Rate Limiting（登入 5 次/分鐘、註冊 3 次/10 分鐘，防止暴力攻擊）
- 密碼強度驗證（至少 8 碼，須含英文字母與數字）
- 單字書 CRUD（含重新命名與語言切換）
- 單字 CRUD
- CSV 批次匯入單字（含重複檢查、部分成功）
- 單字批次刪除
- 閃卡測驗與學習統計
- 間隔重複複習系統（SRS / SM-2 演算法）
- 每日學習 Streak（連續天數 + 今日練習數）
- 選擇題測驗（四選一，Fisher-Yates 隨機選項）

### 本地啟動

#### 前置條件

- Java 21+
- PostgreSQL（建立資料庫 `lingua_mastery`）
- IntelliJ IDEA（建議）

#### 設定環境

1. 在 `src/main/resources/` 建立 `application-local.yml`（此檔案已在 `.gitignore`，不會被提交）：

```yaml
spring:
  datasource:
    password: 你的資料庫密碼

resend:
  api-key: 你的_Resend_API_Key  # 從 resend.com 取得

jwt:
  secret: 你的JWT密鑰（至少32字元）

app:
  mail:
    from: "LinguaMastery <onboarding@resend.dev>"
```

2. IntelliJ Run Configuration → **Active profiles** 填入 `local`

#### 啟動

```bash
./gradlew bootRun
```

伺服器預設運行於 `http://localhost:8080`

### API 端點

| 方法   | 路徑 | 說明 |
|--------|------|------|
| POST   | `/api/auth/register` | 註冊 |
| POST   | `/api/auth/login` | 登入 |
| GET    | `/api/auth/verify` | Email 驗證 |
| POST   | `/api/auth/resend-verification` | 重寄驗證信 |
| POST   | `/api/auth/forgot-password` | 申請重設密碼 |
| POST   | `/api/auth/reset-password` | 重設密碼 |
| GET    | `/api/books` | 取得單字書列表 |
| POST   | `/api/books` | 建立單字書 |
| PUT    | `/api/books/{id}` | 更新單字書名稱 / 語言 |
| DELETE | `/api/books/{id}` | 刪除單字書 |
| GET    | `/api/words/{bookId}` | 取得單字列表 |
| POST   | `/api/words/{bookId}` | 新增單字 |
| PUT    | `/api/words/{wordId}` | 更新單字 |
| DELETE | `/api/words/{wordId}` | 刪除單字 |
| GET    | `/api/study/{bookId}` | 取得本次閃卡題目 |
| POST   | `/api/study/result` | 提交閃卡答題結果 |
| GET    | `/api/stats` | 取得學習統計 |
| GET    | `/api/stats/streak` | 取得連續天數與今日練習數 |
| GET    | `/api/quiz/{bookId}` | 取得選擇題題目（四選一） |
| GET    | `/api/review/stats` | 取得各書今日複習數量 |
| GET    | `/api/review/{bookId}` | 取得本次 SRS 複習單字 |
| POST   | `/api/review/result` | 提交 SRS 複習結果（更新排程） |
| POST   | `/api/books/{bookId}/words/import` | CSV 批次匯入單字 |
| DELETE | `/api/words/batch` | 批次刪除單字 |

---

## English

### Overview

Backend API for LinguaMastery, a gamified language learning platform. Built with Spring Boot, it provides user authentication, vocabulary book management, and learning statistics.

### Tech Stack

- **Framework**: Spring Boot 3.3.5
- **Language**: Java 21
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA / Hibernate
- **Auth**: JWT (JSON Web Token)
- **Email**: Resend HTTP API
- **Build**: Gradle
- **Deploy**: Render (backend), Vercel (frontend)

### Features

- User registration / login with JWT
- Email verification (required before login)
- Forgot password / password reset
- Rate limiting (login: 5/min, register: 3/10min per IP)
- Password strength validation (min 8 chars, letters + numbers required)
- Vocabulary book CRUD (rename & language switch)
- Word CRUD
- CSV batch import (with duplicate check & partial success)
- Batch word deletion
- Flashcard study and learning statistics
- Spaced Repetition System (SRS / SM-2 algorithm)
- Daily learning streak (consecutive days + today's count)
- Multiple choice quiz (4 options, Fisher-Yates randomization)

### Getting Started

#### Prerequisites

- Java 21+
- PostgreSQL (create database `lingua_mastery`)
- IntelliJ IDEA (recommended)

#### Configuration

1. Create `application-local.yml` under `src/main/resources/` (already in `.gitignore`, never committed):

```yaml
spring:
  datasource:
    password: your_db_password

resend:
  api-key: your_resend_api_key  # Get it from resend.com

jwt:
  secret: your-jwt-secret-at-least-32-characters

app:
  mail:
    from: "LinguaMastery <onboarding@resend.dev>"
```

2. In IntelliJ Run Configuration, set **Active profiles** to `local`

#### Run

```bash
./gradlew bootRun
```

Server runs at `http://localhost:8080`

---

## 更新日誌 / Changelog

### v0.8.0 (2026-05-19)
- 後端部署至 Render（https://lingua-mastery-api.onrender.com）
- 前端部署至 Vercel（https://lingua-mastery-web.vercel.app）
- 改用 Resend HTTP API 寄信，取代 Gmail SMTP（解決雲端平台封鎖 SMTP 問題）
- Android APK 發布至 GitHub Releases，可直接下載安裝

### v0.7.0 (2026-05-19)
- 新增單字熟練度分級（0 未學習 → 1 學習中 → 2 已熟悉 → 3 已精通）
- 新增 `user_word_status` 資料表，記錄每位使用者對每個單字的熟練度與連續答對次數
- WordResponse 新增 `proficiencyLevel` 欄位
- StudyService / ReviewService 答題後自動更新熟練度
- 修正熟練度升級 bug：避免同一次答題從 level 1 跳到 level 3
- 修正並發 INSERT 衝突：REQUIRES_NEW 確保 retry 在新 transaction 執行

### v0.6.0 (2026-05-19)
- 新增選擇題測驗（`GET /api/quiz/{bookId}`）
- Fisher-Yates 隨機錯誤選項，不足時從其他書補足
- 追蹤 correctIndex 避免重複翻譯時出錯，改用 ThreadLocalRandom

### v0.5.0 (2026-05-19)
- 新增每日學習 Streak 功能（`GET /api/stats/streak`）
- 新增 `daily_records` 資料表，記錄每日學習數量
- StudyService / ReviewService 提交結果後自動記錄當日活動

### v0.4.1 (2026-05-16)
- 修正刪除單字本時外鍵約束錯誤（cascade 依序刪除 study_logs → word_reviews → words）

### v0.4.0 (2026-05-16)
- 新增 CSV 批次匯入單字（`POST /api/books/{bookId}/words/import`）
- 新增單字批次刪除（`DELETE /api/words/batch`）
- 新增重複單字檢查（單筆新增與 CSV 匯入皆適用）

### v0.3.0 (2026-05-15)
- 新增 SRS 間隔重複複習系統（SM-2 演算法）
- 新增單字書編輯功能（重新命名、切換語言）

### v0.2.0
- 新增 Email 驗證、忘記密碼、重設密碼功能

### v0.1.0
- 初始版本：使用者驗證、單字書 CRUD、閃卡測驗、學習統計

---

### v0.8.0 (2026-05-19)
- Deployed backend to Render (https://lingua-mastery-api.onrender.com)
- Deployed frontend to Vercel (https://lingua-mastery-web.vercel.app)
- Replaced Gmail SMTP with Resend HTTP API (resolves cloud platform SMTP blocking)
- Published Android APK to GitHub Releases for direct download

### v0.7.0 (2026-05-19)
- Added word proficiency levels (0 not learned → 1 learning → 2 familiar → 3 mastered)
- Added `user_word_status` table to track per-user proficiency and correct answer streaks
- Added `proficiencyLevel` field to WordResponse
- StudyService / ReviewService now update proficiency after each answer
- Fixed proficiency level-skip bug (prevented level 1 → level 3 in a single answer)
- Fixed concurrent INSERT race condition using REQUIRES_NEW transaction on retry

### v0.6.0 (2026-05-19)
- Added multiple choice quiz (`GET /api/quiz/{bookId}`)
- Fisher-Yates randomization for wrong options with cross-book fallback
- Fixed correctIndex tracking to handle duplicate translations, switched to ThreadLocalRandom

### v0.5.0 (2026-05-19)
- Added daily learning streak (`GET /api/stats/streak`)
- Added `daily_records` table to track daily study counts
- StudyService / ReviewService now record daily activity after each submission

### v0.4.1 (2026-05-16)
- Fixed foreign key constraint error when deleting vocabulary books (cascade: study_logs → word_reviews → words)

### v0.4.0 (2026-05-16)
- Added CSV batch word import (`POST /api/books/{bookId}/words/import`)
- Added batch word deletion (`DELETE /api/words/batch`)
- Added duplicate word check (single add & CSV import)

### v0.3.0 (2026-05-15)
- Added SRS spaced repetition system (SM-2 algorithm)
- Added vocabulary book edit (rename & language switch)

### v0.2.0
- Added email verification, forgot password, password reset

### v0.1.0
- Initial release: auth, vocabulary book CRUD, flashcard study, learning statistics
