#!/usr/bin/env bash
# scripts/deploy.sh
#
# Builds frontend + backend and (re)starts all Docker services.
# Run this for every deployment after the first SSL bootstrap.
#
# Usage:
#   ./scripts/deploy.sh

set -euo pipefail

BOLD='\033[1m'; GREEN='\033[0;32m'; RESET='\033[0m'

echo -e "${BOLD}=== Full Stack Realty NW — Deploy ===${RESET}"

# ── Build frontend ─────────────────────────────────────────────────────────────
echo ""
echo -e "${BOLD}[1/3] Building frontend...${RESET}"
cd frontend
npm ci --silent
npm run build
cd ..
echo -e "${GREEN}✓ Frontend built → frontend/dist${RESET}"

# ── Build backend ──────────────────────────────────────────────────────────────
echo ""
echo -e "${BOLD}[2/3] Building backend...${RESET}"
cd backend
./gradlew shadowJar --quiet
cd ..
echo -e "${GREEN}✓ Backend jar built${RESET}"

# ── Start / update services ────────────────────────────────────────────────────
echo ""
echo -e "${BOLD}[3/3] Starting services...${RESET}"
docker compose up -d --build

echo ""
echo -e "${GREEN}${BOLD}✓ Deployment complete${RESET}"
docker compose ps
