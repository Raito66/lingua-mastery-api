# LinguaMastery API

[English](#english) | [繁體中文](#繁體中文)

---

## 繁體中文

### 專案簡介

LinguaMastery 後端 API，基於 Spring Boot 開發，提供語言學習平台所需的使用者驗證、單字書管理與學習記錄等功能。

### 技術棧

- **框架**：Spring Boot 3.3.5
- **語言**：Java 17
- **資料庫**：PostgreSQL
- **ORM**：Spring Data JPA / Hibernate
- **驗證**：JWT（JSON Web Token）
- **Email**：Spring Mail + Gmail SMTP
- **建置工具**：Gradle

### 主要功能

- 使用者註冊 / 登入（JWT 驗證）
- Email 驗證（註冊後需驗證才能登入）
- 忘記密碼 / 重設密碼
- 單字書 CRUD（含重新命名與語言切換）
- 單字 CRUD
- 閃卡測驗與學習統計
- 間隔重複複習系統（SRS / SM-2 演算法）

### 本地啟動

#### 前置條件

- Java 17+
- PostgreSQL（建立資料庫 `lingua_mastery`）
- IntelliJ IDEA（建議）

#### 設定環境

1. 複製 `application-local.yml.example` 並命名為 `application-local.yml`：

```yaml
spring:
  datasource:
    password: 你的資料庫密碼
  mail:
    username: 你的Gmail帳號
    password: 你的Gmail應用程式密碼

jwt:
  secret: 你的JWT密鑰（至少32字元）

app:
  mail:
    from: "LinguaMastery <你的Gmail帳號>"
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
| GET    | `/api/review/stats` | 取得各書今日複習數量 |
| GET    | `/api/review/{bookId}` | 取得本次 SRS 複習單字 |
| POST   | `/api/review/result` | 提交 SRS 複習結果（更新排程） |

---

## English

### Overview

Backend API for LinguaMastery, a gamified language learning platform. Built with Spring Boot, it provides user authentication, vocabulary book management, and learning statistics.

### Tech Stack

- **Framework**: Spring Boot 3.3.5
- **Language**: Java 17
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA / Hibernate
- **Auth**: JWT (JSON Web Token)
- **Email**: Spring Mail + Gmail SMTP
- **Build**: Gradle

### Features

- User registration / login with JWT
- Email verification (required before login)
- Forgot password / password reset
- Vocabulary book CRUD (rename & language switch)
- Word CRUD
- Flashcard study and learning statistics
- Spaced Repetition System (SRS / SM-2 algorithm)

### Getting Started

#### Prerequisites

- Java 17+
- PostgreSQL (create database `lingua_mastery`)
- IntelliJ IDEA (recommended)

#### Configuration

1. Create `application-local.yml` under `src/main/resources/`:

```yaml
spring:
  datasource:
    password: your_db_password
  mail:
    username: your_gmail@gmail.com
    password: your_gmail_app_password

jwt:
  secret: your-jwt-secret-at-least-32-characters

app:
  mail:
    from: "LinguaMastery <your_gmail@gmail.com>"
```

2. In IntelliJ Run Configuration, set **Active profiles** to `local`

#### Run

```bash
./gradlew bootRun
```

Server runs at `http://localhost:8080`

---

## 更新日誌 / Changelog

### v0.3.0 (2026-05-15)
- 新增 SRS 間隔重複複習系統（SM-2 演算法）
- 新增單字書編輯功能（重新命名、切換語言）

### v0.2.0
- 新增 Email 驗證、忘記密碼、重設密碼功能

### v0.1.0
- 初始版本：使用者驗證、單字書 CRUD、閃卡測驗、學習統計

---

### v0.3.0 (2026-05-15)
- Added SRS spaced repetition system (SM-2 algorithm)
- Added vocabulary book edit (rename & language switch)

### v0.2.0
- Added email verification, forgot password, password reset

### v0.1.0
- Initial release: auth, vocabulary book CRUD, flashcard study, learning statistics
