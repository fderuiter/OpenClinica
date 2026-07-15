#!/bin/bash
set -e

echo "Starting bootstrapping process..."

# Get absolute paths
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DATA_DIR="${ROOT_DIR}/data"
TEMPLATE_FILE="${ROOT_DIR}/dummy_template.xlsx"
ENV_FILE="${ROOT_DIR}/.env"

# 1. Autogeneration of Folders & Placeholder Files
if [ ! -d "${DATA_DIR}" ]; then
    echo "Creating missing data directory at ${DATA_DIR}..."
    mkdir -p "${DATA_DIR}"
fi

if [ ! -f "${TEMPLATE_FILE}" ]; then
    echo "Creating missing template file at ${TEMPLATE_FILE}..."
    touch "${TEMPLATE_FILE}"
fi

# 2. Environment Generation & Detection
if [ -f "${ENV_FILE}" ]; then
    echo ".env file already exists. Skipping environment generation."
else
    echo ".env file is missing. Generating default .env file..."
    cat <<EOF > "${ENV_FILE}"
DB_HOST=db
DB_PORT=5432
DB_USER=clinica
DB_PASS=clinica
DB=clinica
DB_TYPE=postgres
LDAP_ENABLED=false
HOST_FILE_PATH=${DATA_DIR}
FILE_PATH=/opt/clinica/data/
SEED_CLINICAL_DATA=false
HOST_CLINICAL_TEMPLATE_PATH=${TEMPLATE_FILE}
CLINICAL_TEMPLATE_PATH=
EOF
    echo ".env generated successfully."
fi

# 3. Trigger Container Orchestration
if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
    COMPOSE_CMD="docker compose"
elif command -v docker-compose >/dev/null 2>&1; then
    COMPOSE_CMD="docker-compose"
else
    echo "Error: Neither 'docker compose' nor 'docker-compose' command was found."
    exit 1
fi

echo "Starting containerized stack using '${COMPOSE_CMD}'..."
${COMPOSE_CMD} up -d --build

echo "Bootstrapping complete!"
