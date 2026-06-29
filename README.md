<div align="center">

<br/>

<img src="https://img.shields.io/badge/🌀-Vortex%20Admin%20Pro-6366F1?style=for-the-badge&labelColor=0F0F0F&color=6366F1" height="40" alt="Vortex Admin Pro" />

<h3>Enterprise-Grade SaaS Admin Dashboard Starter Kit</h3>

<p>A production-ready, full-stack admin platform built with Spring Boot 3 and React 18.<br/>Ship your next SaaS product with authentication, RBAC, analytics, and more — out of the box.</p>

<br/>

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![React](https://img.shields.io/badge/React-18-61DAFB?style=flat-square&logo=react&logoColor=black)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-5-646CFF?style=flat-square&logo=vite&logoColor=white)](https://vitejs.dev/)
[![TailwindCSS](https://img.shields.io/badge/TailwindCSS-3-06B6D4?style=flat-square&logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](./LICENSE)

<br/>

</div>

---

## 📖 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Environment Variables](#-environment-variables)
- [API Reference](#-api-reference)
- [RBAC — Roles & Permissions](#-rbac--roles--permissions)
- [Modules](#-modules)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🌀 Overview

**Vortex Admin Pro** is a comprehensive, enterprise-ready SaaS Admin Dashboard Starter Kit. It provides everything you need to bootstrap a production-grade admin panel — from authentication and role-based access control to real-time analytics, file management, and AI-powered audit log insights.

Built on a clean **Controller → Service → Repository** layered architecture, every feature is production-ready and extensible.

> Inspired by the best: **Stripe**, **Vercel**, and **Linear** — modern, minimal, and fast.

---

## ✨ Features

### 🔐 Authentication & Security
- **JWT Access + Refresh Token** flow with 7-day sliding expiry
- **Google OAuth2** login (redirect flow + ID token exchange)
- **Two-Factor Authentication (TOTP)** with backup codes
- **Password Policy** enforcement (minimum strength, expiry, history)
- **Account Lockout** after 5 failed attempts (15-minute cooldown)
- **Forgot / Reset Password** via email link
- **Active Session Management** — view and revoke sessions per device
- **BCrypt** password hashing

### 👥 User Management
- Full **CRUD** for users with soft delete
- **Bulk Actions** — activate, suspend, delete, change role
- **CSV Import** with RFC-4180 compliant parser and email validation
- **Export** users to CSV / Excel
- **User Activity Timeline** — per-user audit events and login sessions
- **Geo Login Map** — login sessions plotted by country
- **Avatar** upload and profile management

### 🛡️ Role-Based Access Control (RBAC)
- 4 built-in roles: `SUPER_ADMIN`, `ADMIN`, `MANAGER`, `USER`
- Granular **permission system** — 20+ individual permissions
- Dynamic role/permission assignment via UI
- Enforced on both **backend** (`@PreAuthorize`) and **frontend** (route guards)

### 📊 Dashboard & Analytics
- Real-time KPI cards: Total Users, Active Users, Total Teams, Events, Tasks
- **User Growth** chart (last 6 months)
- **Task Activity** chart (last 7 days)
- **Login Activity** trend (last 6 months)
- **User Distribution** pie chart
- **System Health** monitoring (CPU, memory, disk, JVM)
- Recent activity feed & latest users widget

### 📁 File Manager
- Upload, rename, download, and delete files
- Permission-aware — users see only their own files; admins see all
- File size limit: **10 MB** per file

### 📅 Calendar & Events
- Monthly calendar view with day-based event slots
- Create, edit, and delete events
- **Multi-attendee selection** (search or dropdown)
- Events scoped to current user — shows events you created or attend

### ✅ Task Management
- Kanban board with **drag-and-drop** (`@hello-pangea/dnd`)
- Columns: **To Do**, **In Progress**, **Done**
- Priority levels: Low, Medium, High
- Assign tasks to users and teams
- **Task Comments** per task

### 🏢 Organizations
- Multi-organization workspace support
- Invite members via token-based invitations
- Switch active workspace
- Organization-level billing and plan management

### 👥 Teams
- Create and manage teams / departments
- Assign members and tasks to teams

### 🔔 Notifications
- In-app notification center
- Mark as read / mark all as read
- Unread badge counter in the sidebar

### 📈 Reports & Exports
- KPI trend cards with real DB comparisons (vs previous period)
- Revenue and user growth area/bar charts
- Export **Users**, **Audit Logs**, **Login Activity**, **Organizations**, **Billing** to CSV / Excel / PDF

### 💳 Billing & Subscriptions
- Subscription plan management (Free, Pro, Enterprise)
- Invoice history
- Storage and member usage tracking
- Per-organization billing

### 🔑 API Key Management
- Generate, view, copy, and revoke API keys
- Key masking UI with show/hide toggle

### 🪝 Webhooks
- Register and manage webhook endpoints
- Subscribe to system events
- Send test payloads
- Delivery log viewer

### 📧 Email Builder
- Visual HTML email template editor (split code/preview view)
- Live iframe preview
- Save templates to database (Welcome, Reset Password, Invoice)

### 🤖 AI Insights (Gemini)
- Analyze audit logs with **Google Gemini AI**
- Language-aware responses based on user locale (EN / TH / ZH)

### 📋 Audit Logs
- Full action history: `CREATE`, `UPDATE`, `DELETE`, `LOGIN`
- Search and filter by user, action, entity, IP address
- Export logs
- AI-powered analysis

### 🌐 System Health
- Live backend health check (Spring Actuator)
- JVM memory, CPU load, disk usage
- Service status indicators

### 🎛️ System Settings
- Platform name, support email, timezone
- Maintenance mode toggle
- Session timeout & password expiration
- SMTP configuration
- Branding (logo URL, primary/secondary color)
- Localization (default language, date format, currency)

### 🌍 Internationalization (i18n)
- Full UI translation: **English**, **Thai (ภาษาไทย)**, **Chinese Simplified (中文)**
- Per-user language preference (saved to database)
- AI responses adapt to user locale

### 🌙 UI & UX
- **Dark Mode** support (system preference + manual toggle)
- **Fully Responsive** — mobile, tablet, desktop
- Animated page transitions
- Toast notification system (success, error, info)
- Command Palette
- Loading and empty states throughout

---

## 🧰 Tech Stack

### Backend

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Runtime |
| Spring Boot | 3.5.x | Application framework |
| Spring Security | 6.x | Auth & authorization |
| Spring Data JPA | 3.x | Database ORM |
| Spring OAuth2 Client | 6.x | Google OAuth2 |
| JJWT | 0.12.x | JWT generation & validation |
| Lombok | latest | Boilerplate reduction |
| MapStruct | 1.5.x | DTO mapping |
| SpringDoc OpenAPI | 2.x | Swagger UI at `/swagger-ui.html` |
| Jakarta Validation | 3.x | Request validation |
| PostgreSQL Driver | 42.x | JDBC driver |
| Spring Mail | 3.x | Email sending (SMTP) |
| Gemini API | REST | AI audit log analysis |

### Frontend

| Technology | Version | Purpose |
|---|---|---|
| React | 18 | UI framework |
| Vite | 5 | Build tool & dev server |
| TailwindCSS | 3 | Styling |
| React Router DOM | 6 | Client-side routing |
| Axios | 1.x | HTTP client |
| Zustand | 4.x | State management |
| Recharts | 2.x | Charts & graphs |
| react-i18next | 14.x | Internationalization |
| @hello-pangea/dnd | latest | Drag-and-drop Kanban |
| @react-oauth/google | latest | Google OAuth2 |
| jwt-decode | 4.x | JWT client decoding |
| Lucide React | latest | Icon library |

### Infrastructure

| Technology | Purpose |
|---|---|
| PostgreSQL 16 | Primary database |
| Supabase | Managed PostgreSQL (default config) |
| Docker & Compose | Containerized deployment |

---

## 🏗️ Architecture

```
┌──────────────────────────────────────────────────┐
│                   Frontend (React)                │
│  Pages → Services (Axios) → REST API              │
│  Zustand (global state) + react-i18next (i18n)   │
└───────────────────┬──────────────────────────────┘
                    │ HTTP/REST
                    ▼
┌──────────────────────────────────────────────────┐
│              Backend (Spring Boot)                │
│                                                  │
│  Controller Layer  (REST endpoints, validation)  │
│        ↓                                         │
│  Service Layer     (business logic, auth, RBAC)  │
│        ↓                                         │
│  Repository Layer  (Spring Data JPA queries)     │
│        ↓                                         │
│  PostgreSQL        (via Supabase / Docker)       │
└──────────────────────────────────────────────────┘
```

**Backend Package Structure:**

```
com.vortexadmin
├── config          # Security config, CORS, OAuth2, Actuator
├── controller      # REST controllers (26 controllers)
├── dto
│   ├── request     # Input DTOs (validated with Jakarta)
│   └── response    # Output DTOs (no entity exposure)
├── entity          # JPA entities
├── exception       # ApiException, GlobalExceptionHandler
├── mapper          # MapStruct mappers
├── repository      # Spring Data JPA repositories
├── security        # JWT filter, UserDetailsImpl, OAuth2 service
├── service         # Service interfaces
├── service/impl    # Service implementations
└── util            # SecurityUtils, helpers
```

---

## 📁 Project Structure

```
Vortex-Admin-Pro/
├── backend/
│   └── vortex-admin-pro/
│       ├── src/main/java/com/vortexadmin/
│       │   ├── config/
│       │   ├── controller/         # 26 REST controllers
│       │   ├── dto/
│       │   │   ├── request/        # LoginRequest, UserCreateRequest, ...
│       │   │   └── response/       # UserProfileResponse, ApiResponse<T>, ...
│       │   ├── entity/             # User, Role, Task, Event, File, ...
│       │   ├── exception/          # ApiException, GlobalExceptionHandler
│       │   ├── repository/         # JPA repositories
│       │   ├── security/           # JWT, OAuth2, UserDetails
│       │   ├── service/            # Interfaces
│       │   └── service/impl/       # Business logic implementations
│       └── src/main/resources/
│           └── application.yaml    # App configuration (env-variable driven)
│
├── frontend/
│   ├── public/
│   └── src/
│       ├── api/
│       │   └── axios.js            # Axios instance with JWT interceptors
│       ├── components/
│       │   ├── layout/             # Layout, Sidebar, Navbar
│       │   ├── modals/             # UserModal, ImportModal, ...
│       │   └── ui/                 # Toast, ModalPortal, ...
│       ├── context/
│       │   └── AuthContext.jsx     # Auth state, login, logout, updateUser
│       ├── hooks/
│       │   └── useAuth.js          # Hook for auth context
│       ├── locales/
│       │   ├── en/translation.json
│       │   ├── th/translation.json
│       │   └── zh/translation.json
│       ├── pages/                  # 26 page components
│       ├── router/                 # React Router config + route guards
│       ├── services/               # API service functions
│       └── store/                  # Zustand stores
│
├── docker-compose.yml
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version |
|---|---|
| Java Development Kit (JDK) | 17+ |
| Node.js | 18+ |
| Maven | 3.8+ |
| Docker & Docker Compose | Latest |

---

### Option A — Local Development (Recommended)

#### 1. Clone the repository

```bash
git clone https://github.com/Audomsub/Vortex-Admin-Pro.git
cd Vortex-Admin-Pro
```

#### 2. Configure environment variables

Copy and fill in the backend environment:

```bash
# Backend — set these as environment variables or in your IDE run config
DB_URL=jdbc:postgresql://<host>:<port>/postgres
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
JPA_DDL_AUTO=update
FRONTEND_URL=http://localhost:5173
MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USERNAME=your@email.com
MAIL_PASSWORD=your_smtp_password
MAIL_FROM=no-reply@yourdomain.com
```

Create a `.env` file in the `frontend/` directory:

```bash
VITE_API_URL=http://localhost:8080/api
```

#### 3. Start the Backend

```bash
cd backend/vortex-admin-pro
./mvnw spring-boot:run
```

> Backend runs on **http://localhost:8080**  
> Swagger UI available at **http://localhost:8080/swagger-ui.html**

#### 4. Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

> Frontend runs on **http://localhost:5173**

---

### Option B — Docker Compose

```bash
# Build and start all services
docker-compose up --build -d

# Stop all services
docker-compose down
```

| Service | URL |
|---|---|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |

---

### Default Credentials

After the first startup, the **DataSeeder** automatically creates:

| Role | Username | Password |
|---|---|---|
| `SUPER_ADMIN` | `superadmin` | `Admin@12345` |
| `ADMIN` | `admin` | `Admin@12345` |

> Change these credentials immediately in a production environment.

---

## ⚙️ Environment Variables

### Backend (`application.yaml` / system env)

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | Supabase URL | PostgreSQL JDBC connection string |
| `DB_USERNAME` | — | Database username |
| `DB_PASSWORD` | — | Database password |
| `JPA_DDL_AUTO` | `update` | Hibernate DDL mode (`update`, `validate`, `none`) |
| `FRONTEND_URL` | `http://localhost:5173` | CORS allowed origin |
| `MAIL_HOST` | — | SMTP server hostname |
| `MAIL_PORT` | `587` | SMTP port |
| `MAIL_USERNAME` | — | SMTP auth username |
| `MAIL_PASSWORD` | — | SMTP auth password |
| `MAIL_FROM` | `no-reply@vortexadmin.com` | Sender address for system emails |
| `REPORT_EMAIL` | — | Email address to receive exported reports |

### Frontend (`.env`)

| Variable | Default | Description |
|---|---|---|
| `VITE_API_URL` | `http://localhost:8080/api` | Backend API base URL |

---

## 📡 API Reference

All endpoints are prefixed with `/api`. Every response follows a standard wrapper:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {}
}
```

Error responses:

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": ["field: error message"]
}
```

### Auth

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/login` | Public | Login with username + password |
| `POST` | `/api/auth/register` | Public | Register a new account |
| `POST` | `/api/auth/refresh` | Public | Refresh access token |
| `POST` | `/api/auth/logout` | JWT | Invalidate refresh token |
| `POST` | `/api/auth/google` | Public | Exchange Google ID token |
| `POST` | `/api/auth/forgot-password` | Public | Send password reset email |
| `POST` | `/api/auth/reset-password` | Public | Reset password via token |

### Users

| Method | Endpoint | Permission | Description |
|---|---|---|---|
| `GET` | `/api/users` | `user.read` | List all users |
| `POST` | `/api/users` | `user.create` | Create user |
| `PUT` | `/api/users/{id}` | `user.update` | Update user |
| `DELETE` | `/api/users/{id}` | `user.delete` | Soft-delete user |
| `GET` | `/api/users/me` | JWT | Get own profile |
| `PUT` | `/api/users/me` | JWT | Update own profile |
| `POST` | `/api/users/me/avatar` | JWT | Upload avatar |
| `POST` | `/api/users/bulk-action` | `user.update` | Bulk suspend/delete/role |
| `POST` | `/api/users/import` | `user.create` | Import users from CSV |
| `GET` | `/api/users/export` | `user.read` | Export users CSV/Excel |
| `GET` | `/api/users/{id}/activity` | `user.read` | User activity timeline |
| `GET` | `/api/users/geo-stats` | `user.read` | Login geo distribution |
| `GET` | `/api/users/search` | `user.read` | Search users by keyword |

### Tasks

| Method | Endpoint | Permission | Description |
|---|---|---|---|
| `GET` | `/api/tasks` | `task.read` | List tasks |
| `POST` | `/api/tasks` | `task.create` | Create task |
| `PUT` | `/api/tasks/{id}` | `task.update` | Update task |
| `DELETE` | `/api/tasks/{id}` | `task.delete` | Delete task |
| `GET` | `/api/tasks/assignee/{userId}` | `task.read` | Tasks by assignee |
| `GET` | `/api/tasks/team/{teamId}` | `task.read` | Tasks by team |

### Other Endpoints

| Group | Base Path | Controllers |
|---|---|---|
| Roles | `/api/roles` | CRUD roles + permissions |
| Teams | `/api/teams` | CRUD teams + members |
| Calendar | `/api/events` | CRUD events + attendees |
| Files | `/api/files` | Upload, download, rename, delete |
| Notifications | `/api/notifications` | List, mark read |
| Audit Logs | `/api/audit-logs` | List, search, export |
| Reports | `/api/reports` | Stats, KPIs, export |
| Billing | `/api/billing` | Plans, subscriptions, invoices |
| Organizations | `/api/organizations` | CRUD orgs, members, invitations |
| API Keys | `/api/api-keys` | Generate, list, revoke |
| Webhooks | `/api/webhooks` | CRUD endpoints, test, logs |
| Email Templates | `/api/email-templates` | Get, save templates |
| AI | `/api/ai/analyze` | Gemini AI audit analysis |
| Sessions | `/api/sessions` | List, revoke sessions |
| 2FA | `/api/2fa` | Enable, disable, verify TOTP |
| System Settings | `/api/settings` | Get, update platform settings |
| Dashboard | `/api/dashboard` | Stats, charts, health |
| Support Tickets | `/api/tickets` | CRUD tickets + comments |
| Search | `/api/search` | Global search |

> Full interactive documentation available at `/swagger-ui.html` when the backend is running.

---

## 🛡️ RBAC — Roles & Permissions

### Built-in Roles

| Role | Description |
|---|---|
| `SUPER_ADMIN` | Full access to all features. Cannot be restricted. |
| `ADMIN` | Full user and system management |
| `MANAGER` | Team, task, and report management |
| `USER` | Basic access — view own data, manage own tasks |

### Permission List

| Category | Permissions |
|---|---|
| Users | `user.read` `user.create` `user.update` `user.delete` |
| Roles | `role.read` `role.create` `role.update` `role.delete` |
| Tasks | `task.read` `task.create` `task.update` `task.delete` `task.read.own` `task.update.own` `task.delete.own` `task.read.team` |
| Files | `file.read` `file.upload` `file.delete` `file.read.all` `file.delete.all` |
| Dashboard | `dashboard.view` |
| Settings | `settings.manage` |

---

## 🗂️ Modules

| Module | Page | API Controller |
|---|---|---|
| Dashboard | `Home.jsx` | `DashboardController` |
| User Management | `Users.jsx` | `UserController` |
| Roles & Permissions | `Roles.jsx` | `RoleController` |
| Teams | `Teams.jsx` | `TeamController` |
| Task Board | `Tasks.jsx` | `TaskController` |
| Calendar | `Calendar.jsx` | `EventController` |
| File Manager | `Files.jsx` | `FileController` |
| Notifications | `Notifications.jsx` | `NotificationController` |
| Reports | `Reports.jsx` | `ReportController` |
| Billing | `Billing.jsx` | `BillingController` |
| Organizations | `Organizations.jsx` | `OrganizationController` |
| Support Tickets | `Tickets.jsx` | `TicketController` |
| API Keys | `ApiKeys.jsx` | `ApiKeyController` |
| Webhooks | `Webhooks.jsx` | `WebhookController` |
| Email Builder | `EmailBuilder.jsx` | `EmailTemplateController` |
| Audit Logs | `AuditLogs.jsx` | `AuditLogController` |
| System Health | `SystemHealth.jsx` | Spring Actuator |
| System Settings | `Settings.jsx` | `SystemSettingController` |
| Profile | `Profile.jsx` | `UserController` |
| 2FA Setup | `Profile.jsx` | `TwoFactorController` |
| Active Sessions | `Profile.jsx` | `SessionController` |
| AI Insights | `AuditLogs.jsx` | `AiController` |

---

## 🤝 Contributing

Contributions, issues and feature requests are welcome!

1. Fork the repository
2. Create a feature branch: `git checkout -b feat/your-feature`
3. Commit your changes: `git commit -m "feat: add your feature"`
4. Push to the branch: `git push origin feat/your-feature`
5. Open a Pull Request

### Commit Convention

```
feat:     New feature
fix:      Bug fix
refactor: Code refactor
style:    UI/UX update
docs:     Documentation update
test:     Tests
chore:    Maintenance / tooling
```

---

## 📄 License

Distributed under the **MIT License**. See [`LICENSE`](./LICENSE) for more information.

---

<div align="center">

Built with care by **Audomsub** &nbsp;·&nbsp; Powered by Spring Boot & React

<br/>

[![GitHub](https://img.shields.io/badge/GitHub-Audomsub-181717?style=flat-square&logo=github)](https://github.com/Audomsub/Vortex-Admin-Pro)

</div>
