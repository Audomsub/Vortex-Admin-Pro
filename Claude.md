# CLAUDE.md

## Project Overview

Project Name: Vortex Admin Pro

Vortex Admin Pro is a modern full-stack SaaS Admin Dashboard Starter Kit built with Spring Boot and React Vite.

The project focuses on:

* Enterprise-ready architecture
* JWT Authentication
* Role-Based Access Control (RBAC)
* User Management
* Dashboard Analytics
* Audit Logging
* Notification System
* PostgreSQL Database
* REST API Architecture
* Responsive UI
* Dark Mode Support

---

## Tech Stack

### Backend

* Java 17
* Spring Boot 3.5.x
* Spring Security
* Spring Data JPA
* PostgreSQL
* Lombok
* Validation
* MapStruct
* JWT (JJWT)
* SpringDoc OpenAPI (Swagger)

### Frontend

* React 18
* Vite
* TailwindCSS
* React Router DOM
* Axios
* Zustand
* Recharts
* Lucide React

### Database

* PostgreSQL

---

## Backend Architecture

Use clean layered architecture:

Controller
↓
Service
↓
Repository
↓
Database

Never access Repository directly from Controller.

All business logic must be inside Service layer.

---

## Package Structure

com.vortexadmin

* config
* controller
* dto
* entity
* exception
* mapper
* repository
* security
* service
* service.impl
* util

---

## Entity Rules

Use:

* @Entity
* @Table
* Lombok annotations

Every entity should include:

* id
* createdAt
* updatedAt

Relationships must use JPA annotations.

Use:

* @ManyToOne
* @OneToMany
* @ManyToMany

Avoid bidirectional relationships unless necessary.

---

## DTO Rules

Never expose Entity directly to API.

Always create:

* Request DTO
* Response DTO

Examples:

* LoginRequest
* RegisterRequest
* UserResponse
* DashboardStatsResponse

---

## API Response Standard

Every API should return:

{
"success": true,
"message": "Operation successful",
"data": {}
}

Error format:

{
"success": false,
"message": "Validation failed",
"errors": []
}

---

## Authentication

Use JWT Authentication.

Required endpoints:

POST /api/auth/login
POST /api/auth/register
POST /api/auth/refresh
POST /api/auth/logout

Use:

* Access Token
* Refresh Token

Password encryption:

BCryptPasswordEncoder

---

## RBAC

Roles:

* SUPER_ADMIN
* ADMIN
* MANAGER
* USER

Permissions:

* user.create

* user.read

* user.update

* user.delete

* role.create

* role.read

* role.update

* role.delete

* dashboard.view

* settings.manage

Authorization must be enforced on both:

* Backend API
* Frontend Route

---

## Database Naming Rules

Tables:

snake_case

Examples:

users
roles
permissions
audit_logs
refresh_tokens

Columns:

snake_case

Examples:

created_at
updated_at
first_name
last_name

---

## Frontend Structure

src

* assets
* components
* hooks
* layouts
* pages
* router
* services
* store
* utils

---

## React Rules

Use:

* Functional Components
* React Hooks
* Zustand

Do not use Redux.

Use Axios for all API calls.

Never hardcode URLs.

Store API URL in:

.env

Example:

VITE_API_URL=http://localhost:8080/api

---

## UI Guidelines

Theme:

Modern SaaS

Inspired by:

* Stripe
* Vercel
* Linear

Requirements:

* Dark Mode
* Responsive Design
* Mobile Friendly
* Sidebar Navigation
* Top Navbar
* Dashboard Cards
* Charts
* Loading States
* Error States

---

## Dashboard Features

Statistics:

* Total Users
* Active Users
* Revenue
* Growth Rate

Charts:

* User Growth
* Revenue Trend
* Role Distribution

Widgets:

* Recent Activity
* Notifications

---

## Coding Standards

Backend:

* Constructor Injection
* No Field Injection
* Use Service Interfaces
* Use DTO Mapping

Frontend:

* Reusable Components
* Custom Hooks
* Centralized API Services

---

## Git Commit Convention

feat: new feature
fix: bug fix
refactor: code refactor
docs: documentation
style: ui update
test: tests
chore: maintenance

Examples:

feat: add jwt authentication
feat: add user management module
fix: resolve login token issue

---

## Claude Instructions

When generating code:

1. Follow existing project structure.
2. Write production-ready code.
3. Include validation.
4. Include error handling.
5. Use clean architecture.
6. Use TypeScript-ready patterns when possible.
7. Do not generate placeholder code unless requested.
8. Prefer scalability and maintainability.
9. Follow Spring Boot best practices.
10. Follow React best practices.

Always generate complete files when asked.
Always explain file locations before generating code.
