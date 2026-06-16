<div align="center">

# 🌀 Vortex Admin Pro

<p align="center">
  <b>A powerful, modern, and high-performance Admin Dashboard built for enterprise applications.</b> <br>
  Featuring a robust Spring Boot backend and an elegant React/Vite frontend.
</p>

<!-- Badges -->
<p align="center">
  <img src="https://img.shields.io/badge/version-1.0.0-blue.svg?style=for-the-badge" alt="Version" />
  <img src="https://img.shields.io/badge/build-passing-brightgreen.svg?style=for-the-badge" alt="Build Status" />
  <img src="https://img.shields.io/badge/Frontend-React%20%7C%20Vite%20%7C%20Tailwind-646CFF?style=for-the-badge&logo=react" alt="Frontend Tech" />
  <img src="https://img.shields.io/badge/Backend-Java%20%7C%20Spring%20Boot-6DB33F?style=for-the-badge&logo=spring" alt="Backend Tech" />
  <img src="https://img.shields.io/badge/Database-PostgreSQL-336791?style=for-the-badge&logo=postgresql" alt="Database" />
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge" alt="License" />
</p>

<br/>
<a href="#about-the-project"><strong>Explore the docs »</strong></a>
<br />
<br />
<a href="#features">View Features</a> ·
<a href="#getting-started">Getting Started</a> ·
<a href="#roadmap-development-status">Roadmap</a>
</div>

---

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li><a href="#about-the-project">About The Project</a></li>
    <li><a href="#tech-stack">Tech Stack</a></li>
    <li><a href="#features">Features</a></li>
    <li><a href="#project-structure">Project Structure</a></li>
    <li><a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#roadmap-development-status">Roadmap & Development Status</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
  </ol>
</details>

---

## 📖 About The Project

**Vortex Admin Pro** is designed to be the ultimate starting point for any complex enterprise administration panel. It fully separates the frontend and backend architectures, allowing for decoupled scaling and development. The UI is crafted with deep attention to detail, utilizing rich aesthetics, dark mode by default, and responsive design, while the backend leverages the raw performance and security of Spring Boot.

---

## 🛠 Tech Stack

### Frontend
* **Core:** React.js, Vite
* **Styling:** TailwindCSS, Vanilla CSS (Custom Themes)
* **State Management:** React Context / Custom Hooks

### Backend
* **Core:** Java 17+, Spring Boot 3
* **Security:** Spring Security, OAuth2 (Google, GitHub, Microsoft)
* **Database:** PostgreSQL, Spring Data JPA

### DevOps & Infrastructure
* **Containerization:** Docker & Docker Compose
* **Build Tools:** Maven / Gradle, npm

---

## ✨ Features

- **🛡️ Secure Backend & Authentication**: Enterprise-ready architecture with JWT and OAuth2 social logins.
- **⚡ Lightning Fast Frontend**: HMR and instant builds via Vite.
- **🎨 Modern & Premium UI/UX**: Clean, responsive layout tailored for complex admin workflows, with rich micro-animations.
- **🧩 Modular Design**: Fully separated RESTful API backend and single-page application frontend.
- **🐳 Docker Ready**: Instantly spin up PostgreSQL and related services using `docker-compose`.
- **📊 Real-time Ready**: Architecture structured to support real-time webhooks and notifications.

---

## 📁 Project Structure

```plaintext
Vortex-Admin-Pro/
├── backend/
│   └── vortex-admin-pro/
│       ├── src/main/java/com/vortexadmin/  # Spring Boot Source Code
│       ├── src/main/resources/             # Application configs (e.g., application.yaml)
│       └── generate_mock_sql.py            # Mock data generation script
├── frontend/
│   ├── src/                                # React Source Code (Pages, Hooks, Components)
│   ├── package.json                        # Node dependencies
│   └── vite.config.js                      # Vite configuration
├── docker-compose.yml                      # Infrastructure setup for PostgreSQL
└── README.md                               # Project documentation
```

---

## 🚀 Getting Started

Follow these instructions to get a local copy of the project up and running.

### Prerequisites

Ensure you have the following installed on your local machine:
- [Node.js](https://nodejs.org/) (v16+)
- [Java Development Kit (JDK)](https://openjdk.org/) (v17+)
- [Docker](https://www.docker.com/) & Docker Compose
- Maven (or use the provided `./mvnw` wrapper)

### Installation

**1. Database Setup**
Spin up the PostgreSQL database in the background using Docker Compose:
```bash
docker-compose up -d
```

**2. Start the Backend (Spring Boot)**
```bash
cd backend/vortex-admin-pro
./mvnw spring-boot:run
```
*The backend server will start on `http://localhost:8080`*

**3. Start the Frontend (Vite + React)**
```bash
cd frontend
npm install
npm run dev
```
*The frontend development server will start on `http://localhost:5173`*

---

## 🗺️ Roadmap & Development Status

The foundational backend infrastructure (Controllers, Database Connections, Security) is set up. The frontend is currently in active development. 

> **Note:** Several features are currently utilizing **Mock Data** and are pending full API integration.

### 🚧 Pending API Integrations:
- [ ] **ApiKeys**: Management of API access tokens.
- [ ] **Reports**: Real-time analytics and data reporting.
- [ ] **Roles & Permissions**: Granular access control for users.
- [ ] **Webhooks**: Event-driven webhook triggering.
- [ ] **Billing**: Currently using a `MOCK` payment provider. Needs Stripe/PromptPay integration.
- [ ] **Organizations**: Multi-tenant organization structure.
- [ ] **File Uploads**: UI generates mock S3 URLs instead of actual file uploads.
- [ ] **Global Search**: Backend currently returns dummy data, and UI has mock fallbacks.

---

## 🤝 Contributing

We welcome contributions! Your efforts make the open-source community an amazing place to learn, inspire, and create.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

<br>
<div align="center">
  Made with ❤️ by the <b>Vortex Admin Pro</b> Team.
</div>
