#!/usr/bin/env bash
# scripts/deploy.sh
#
# Builds frontend + backend and (re)starts all Docker services.
# Run this for every deployment after the first SSL bootstrap.
#
# Usage:
#   ./scripts/deploy.sh              # uses .env.prod
#   ./scripts/deploy.sh --env .env   # override env file

set -euo pipefail

BOLD='\033[1m'; GREEN='\033[0;32m'; RESET='\033[0m'
ENV_FILE=".env.prod"

# Allow overriding the env file: ./scripts/deploy.sh --env .env.staging
while [[ $# -gt 0 ]]; do
    case "$1" in
        --env) ENV_FILE="$2"; shift 2 ;;
        *) echo "Unknown argument: $1"; exit 1 ;;
    esac
done

if [[ ! -f "$ENV_FILE" ]]; then
    echo "Error: env file '$ENV_FILE' not found." >&2
    exit 1
fi

echo -e "${BOLD}=== Full Stack Realty NW — Deploy (${ENV_FILE}) ===${RESET}"

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
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file "$ENV_FILE" up -d --build

echo ""
echo -e "${GREEN}${BOLD}✓ Deployment complete${RESET}"
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file "$ENV_FILE" ps
