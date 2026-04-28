# Tool-119 — Companies Act 2013 Compliance Tool

An AI-powered web application that helps companies track and manage
their compliance obligations under the Companies Act 2013.

---

## Architecture
┌─────────────────────────────────────────────┐
│           Browser (React Frontend)          │
│         http://localhost:80                 │
└──────────────────┬──────────────────────────┘
│ HTTP/REST
┌──────────────────▼──────────────────────────┐
│       Java Spring Boot Backend              │
│       http://localhost:8080                 │
│   JWT Auth │ Redis Cache │ Swagger UI       │
└────┬───────────────────────────┬────────────┘
│                           │
┌────▼────────┐          ┌───────▼────────────┐
│ PostgreSQL  │          │   Redis Cache      │
│   Port 5432 │          │   Port 6379        │
└─────────────┘          └────────────────────┘
│
┌──────────────────────────────────▼──────────┐
│       Python Flask AI Service               │
│       http://localhost:5000                 │
│   /describe │ /recommend │ /generate-report │
└─────────────────────────────────────────────┘
---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2.5 |
| Database | PostgreSQL 15 |
| Cache | Redis 7 |
| Security | Spring Security + JWT |
| Migrations | Flyway |
| AI Service | Python 3.11, Flask, Groq LLaMA-3.3-70b |
| Frontend | React 18 + Vite, Tailwind CSS |
| Containerization | Docker + Docker Compose |
| API Docs | Swagger UI (SpringDoc OpenAPI) |
| Testing | JUnit 5, Mockito, JaCoCo (80% coverage) |

---

## Prerequisites

Make sure these are installed before running:

- Java 17 (Eclipse Temurin recommended)
- Maven 3.9+
- Docker Desktop
- Git

---

## Setup Steps

### 1. Clone the repository

```bash
git clone https://github.com/tecsxpert/companies-act-2013-compliance-tool
cd companies-act-2013-compliance-tool
```

### 2. Create your .env file

```bash
cp .env.example .env
```

Then open `.env` and fill in your values (see table below).

### 3. Start all services

```bash
docker-compose up --build
```

This will:
- Start PostgreSQL on port 5432
- Start Redis on port 6379
- Run Flyway migrations automatically
- Seed 30 demo records on first startup
- Start Spring Boot backend on port 8080
- Start Flask AI service on port 5000
- Start React frontend on port 80

### 4. Access the application

| Service | URL |
|---------|-----|
| Frontend | http://localhost |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| AI Service Health | http://localhost:5000/health |

### 5. Demo login credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@company.com | admin123 |
| Manager | manager@company.com | manager123 |
| User | user@company.com | user123 |

---

## .env Reference Table

| Variable | Description | Example |
|----------|-------------|---------|
| DB_HOST | PostgreSQL host | postgres |
| DB_PORT | PostgreSQL port | 5432 |
| DB_NAME | Database name | compliancedb |
| DB_USERNAME | Database user | postgres |
| DB_PASSWORD | Database password | postgres123 |
| REDIS_HOST | Redis host | redis |
| REDIS_PORT | Redis port | 6379 |
| REDIS_PASSWORD | Redis password (optional) | |
| JWT_SECRET | JWT signing secret (min 32 chars) | your_secret_here |
| JWT_EXPIRATION_MS | Access token TTL in ms | 86400000 |
| JWT_REFRESH_MS | Refresh token TTL in ms | 604800000 |
| MAIL_HOST | SMTP host | smtp.gmail.com |
| MAIL_PORT | SMTP port | 587 |
| MAIL_USERNAME | Sender email | your@gmail.com |
| MAIL_PASSWORD | App password | yourapppassword |
| AI_SERVICE_URL | Flask AI service URL | http://ai-service:5000 |
| GROQ_API_KEY | Groq API key | your_groq_key |

---

## Running Tests

```bash
cd backend
mvn test
```

To generate coverage report:

```bash
mvn test jacoco:report
start target/site/jacoco/index.html
```

Current coverage: **80%** | Tests: **138 passing**

---

## API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | /api/auth/register | Public | Register new user |
| POST | /api/auth/login | Public | Login and get JWT |
| POST | /api/auth/refresh | Public | Refresh access token |
| GET | /api/compliance/all | USER+ | Get all records (paginated) |
| GET | /api/compliance/{id} | USER+ | Get record by ID |
| POST | /api/compliance/create | MANAGER+ | Create new record |
| PUT | /api/compliance/{id} | MANAGER+ | Update record |
| DELETE | /api/compliance/{id} | ADMIN | Soft delete record |
| GET | /api/compliance/search | USER+ | Search records |
| GET | /api/compliance/stats | MANAGER+ | Get statistics |
| GET | /api/compliance/overdue | USER+ | Get overdue records |
| GET | /api/compliance/due-soon | USER+ | Get records due soon |

Full API documentation: http://localhost:8080/swagger-ui.html

---

## Project Structure
companies-act-2013-compliance-tool/
├── backend/                    # Spring Boot application
│   ├── src/main/java/com/internship/tool/
│   │   ├── controller/         # REST endpoints
│   │   ├── service/            # Business logic
│   │   ├── repository/         # DB queries
│   │   ├── entity/             # JPA models + DTOs
│   │   ├── config/             # Security, Redis, JWT
│   │   └── exception/          # Custom exceptions
│   ├── src/main/resources/
│   │   ├── db/migration/       # Flyway SQL files
│   │   └── application.yaml    # Configuration
│   └── Dockerfile
├── ai-service/                 # Flask AI microservice
├── frontend/                   # React + Vite frontend
├── docker-compose.yml
├── .env.example
└── README.md

---

## Team

| Role | Responsibility |
|------|---------------|
| Java Developer 1 | Spring Boot, JWT, Docker, Tests, README |
| Java Developer 2 | Flyway, React Frontend, Email, Demo Video |
| AI Developer 1 | Flask setup, /describe, /recommend endpoints |
| AI Developer 2 | Groq client, /generate-report, Security review |

---

## Demo Day

**Friday 9 May 2026** — 6 minute live presentation

Run fresh demo environment:
```bash
docker-compose down -v
docker-compose up --build
```

This resets the database and seeds 30 fresh demo records.