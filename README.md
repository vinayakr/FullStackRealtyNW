# Full Stack Realty NW

Professional real estate website for Vinny Rao — licensed agent and active investor in the Pacific Northwest.

## Features

- **AI Home Advisor** — ChatGPT-like chat interface that guides users through finding their ideal PNW home with streaming, back-and-forth conversation
- **2% Commission Calculator** — Interactive slider showing savings vs. traditional 3% listing commission
- **Articles Section** — SEO-rich real estate guides covering buying, selling, investing, and market insights
- **Responsive Design** — Navy + gold professional design built with Tailwind CSS

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Kotlin 2.0 + Ktor 3.0 (Netty) |
| Database | PostgreSQL + Exposed ORM + HikariCP |
| AI Chat | Anthropic Claude API (streaming SSE) |
| Frontend | Vite + React 18 + TypeScript + Tailwind CSS |
| Reverse Proxy / SSL | Caddy 2 (automatic Let's Encrypt) |
| Infra | Docker Compose |

## Local Development

### Prerequisites
- Java 17 (set via `org.gradle.java.home` in `backend/gradle.properties`)
- Node.js 18+
- Docker Desktop
- Anthropic API key (optional — falls back to contact message)

### 1. Environment setup

```bash
cp .env.example .env
# Add your ANTHROPIC_API_KEY
```

### 2. Start the database

```bash
docker compose up -d postgres
```

If you already have PostgreSQL running locally on port 5432:
```bash
psql -U postgres -c "CREATE ROLE realty_user WITH LOGIN PASSWORD 'realty_password';"
psql -U postgres -c "CREATE DATABASE fullstackrealtynw OWNER realty_user;"
psql -U realty_user -d fullstackrealtynw -f backend/src/main/resources/db/init.sql
```

### 3. Start the Kotlin backend

```bash
cd backend
ANTHROPIC_API_KEY=your_key_here ./gradlew run
# Runs on http://localhost:8080
```

### 4. Start the frontend

```bash
cd frontend
npm install
npm run dev
# Runs on http://localhost:3000 (proxies /api to :8080)
```

## Production Deployment

### Prerequisites on your server
- Docker + Docker Compose
- DNS A records for `fullstackrealtynw.com` and `www.fullstackrealtynw.com` pointing to your server
- Ports 80 and 443 open

### Deploy

```bash
cp .env.example .env   # fill in ANTHROPIC_API_KEY
./scripts/deploy.sh
```

That's it. Caddy automatically obtains and renews a Let's Encrypt certificate on first start — no bootstrap script, no manual steps.

### Subsequent deploys

```bash
./scripts/deploy.sh
```

### Architecture

```
Internet → Caddy (80/443, HTTP/2 + HTTP/3)
             ├── /api/*  → Kotlin backend (internal, SSE-safe)
             ├── /health → Kotlin backend
             └── /*      → frontend/dist (static files + SPA fallback)
```

Caddy handles TLS termination, HTTP→HTTPS redirect, gzip/zstd compression, security headers, and certificate renewal entirely automatically.

## Project Structure

```
FullStackRealtyNW/
├── Caddyfile                             # Reverse proxy + automatic SSL
├── docker-compose.yml                    # postgres, backend, caddy
├── scripts/
│   └── deploy.sh                         # build + docker compose up
├── backend/                              # Kotlin + Ktor API server
│   ├── Dockerfile
│   └── src/main/kotlin/com/fullstackrealtynw/
│       ├── Application.kt
│       ├── plugins/         # HTTP, Databases, Routing
│       ├── routes/          # ArticleRoutes, ChatRoutes
│       ├── services/        # AnthropicService, ArticleService, ChatService
│       └── models/          # Article, Chat
└── frontend/                             # Vite + React + TypeScript
    └── src/
        ├── components/      # Navbar, Hero, About, CommissionSection, ChatInterface, …
        ├── pages/           # Home, ChatPage, ArticlesPage, ArticleDetailPage
        ├── api/client.ts
        └── types/index.ts
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/health` | Health check |
| GET | `/api/articles` | List articles (`?category=Buying`) |
| GET | `/api/articles/:slug` | Single article |
| POST | `/api/chat/sessions` | Create chat session |
| GET | `/api/chat/sessions/:id/messages` | Message history |
| POST | `/api/chat/sessions/:id/messages` | Send message (SSE streaming) |

## Contact

Vinny Rao · vinny@fullstackrealtynw.com
