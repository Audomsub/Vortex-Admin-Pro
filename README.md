<div align="center">

# 🌀 Vortex Admin Pro

<p align="center">
  A powerful, modern, and high-performance Admin Dashboard built for enterprise applications. <br>
  Featuring a robust Spring Boot backend and an elegant React/Vite frontend.
</p>

<!-- Badges -->
<p align="center">
  <img src="https://img.shields.io/badge/version-1.0.0-blue.svg?style=for-the-badge" alt="Version" />
  <img src="https://img.shields.io/badge/build-passing-brightgreen.svg?style=for-the-badge" alt="Build Status" />
  <img src="https://img.shields.io/badge/Frontend-React%20%7C%20Vite%20%7C%20Tailwind-646CFF?style=for-the-badge&logo=react" alt="Frontend Tech" />
  <img src="https://img.shields.io/badge/Backend-Java%20%7C%20Spring%20Boot-6DB33F?style=for-the-badge&logo=spring" alt="Backend Tech" />
  <img src="https://img.shields.io/badge/Database-PostgreSQL-336791?style=for-the-badge&logo=postgresql" alt="Database" />
</p>

</div>

---

## ✨ Features

- **🛡️ Secure Backend**: Enterprise-ready architecture with Spring Boot & OAuth2 integrations (Google, GitHub, Microsoft).
- **⚡ Lightning Fast Frontend**: Built with React and Vite for optimal performance and HMR.
- **🎨 Modern UI/UX**: Clean, responsive, and intuitive design tailored for complex admin workflows.
- **🧩 Modular Design**: Fully separated RESTful API backend and single-page application frontend.
- **🐳 Docker Ready**: Easily spin up the required database and services using `docker-compose`.

---

## 📋 Current Development Status

The backend infrastructure is mostly complete with controllers and database connections (PostgreSQL) ready. However, the frontend is currently in active development.

**Pending API Integrations (Currently using Mock Data):**
- [ ] `ApiKeys`
- [ ] `Reports`
- [ ] `Roles & Permissions`
- [ ] `Webhooks`
- [ ] `Billing`
- [ ] `Organizations`
- [ ] `File Uploads` (Currently generating mock S3 URLs)

---

## 📁 Project Structure

```plaintext
Vortex-Admin-Pro/
├── backend/
│   └── vortex-admin-pro/
│       ├── src/main/java/com/vortexadmin/  # Spring Boot Source Code
│       └── src/main/resources/             # Application configs (application.yaml)
├── frontend/
│   ├── src/                                # React Source Code (Pages, Hooks, Components)
│   ├── package.json
│   └── vite.config.js
├── docker-compose.yml                      # Infrastructure setup
└── README.md                               # Project documentation
```

---

## 🚀 Getting Started

Follow these instructions to get a copy of the project up and running on your local machine.

### Prerequisites

- [Node.js](https://nodejs.org/) (v16+)
- [Java Development Kit (JDK)](https://openjdk.org/) (v17+)
- [Docker](https://www.docker.com/) & Docker Compose
- Maven / Gradle

### 1. Database Setup

Spin up the PostgreSQL database using Docker Compose:

```bash
docker-compose up -d
```

### 2. Start the Backend

```bash
cd backend/vortex-admin-pro
# Run with your IDE or via command line
./mvnw spring-boot:run
```
*The backend will run on `http://localhost:8080`*

### 3. Start the Frontend

```bash
cd frontend
npm install
npm run dev
```
*The frontend will run on `http://localhost:5173`*

---

## 🤝 Contributing

We welcome contributions! Please fork the repository and submit a Pull Request.

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

<br>
<div align="center">
  Made with ❤️ by the <b>Vortex Admin Pro</b> Team.
</div>
