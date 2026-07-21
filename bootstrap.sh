#!/bin/bash
set -e

SKIP_CONNECTIVITY=false
NON_INTERACTIVE=false

while [[ "$#" -gt 0 ]]; do
    case $1 in
        -s|--skip-connectivity) SKIP_CONNECTIVITY=true ;;
        -y|--non-interactive) NON_INTERACTIVE=true ;;
        *) echo "Unknown parameter passed: $1"; exit 1 ;;
    esac
    shift
done

echo "Starting bootstrapping process..."

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DATA_DIR="${ROOT_DIR}/data"
TEMPLATE_FILE="${ROOT_DIR}/dummy_template.xlsx"
ENV_FILE="${ROOT_DIR}/.env"
VALID_TEMPLATE_SOURCE="${ROOT_DIR}/core/src/main/resources/properties/CRF_Design_Template_v3.10.xls"

if [ ! -d "${DATA_DIR}" ]; then
    echo "Creating missing data directory at ${DATA_DIR}..."
    mkdir -p "${DATA_DIR}"
fi

prompt() {
    local var_name=$1
    local prompt_text=$2
    local default_val=$3
    
    if [ "$NON_INTERACTIVE" = true ]; then
        if [ -z "$default_val" ] && [ -z "${!var_name}" ]; then
            echo "Error: Required parameter $var_name is missing and no default exists in non-interactive mode."
            exit 1
        fi
        eval "$var_name=\"\${!var_name:-\$default_val}\""
    else
        local current_val="${!var_name:-$default_val}"
        read -p "$prompt_text [$current_val]: " input_val
        eval "$var_name=\"\${input_val:-\$current_val}\""
    fi
}

# Environment Generation & Detection
if [ -f "${ENV_FILE}" ]; then
    if [ "$NON_INTERACTIVE" = true ]; then
        echo "Found existing .env file. Merging automatically in non-interactive mode."
        cp "${ENV_FILE}" "${ENV_FILE}.bak"
        set -a
        source "${ENV_FILE}"
        set +a
    else
        read -p "Existing .env file found. Do you want to (m)erge with new choices, or (o)verwrite completely? [m/o]: " env_action
        env_action=${env_action:-m}
        if [[ "$env_action" =~ ^[Mm]$ ]]; then
            cp "${ENV_FILE}" "${ENV_FILE}.bak"
            set -a
            source "${ENV_FILE}"
            set +a
            echo "Loaded existing configuration for merging."
        else
            cp "${ENV_FILE}" "${ENV_FILE}.bak"
            echo "Existing configuration backed up, but will be overwritten."
            unset DB_HOST DB_PORT DB_USER DB_PASS DB DB_TYPE LDAP_ENABLED LDAP_HOST HOST_FILE_PATH FILE_PATH SEED_CLINICAL_DATA HOST_CLINICAL_TEMPLATE_PATH CLINICAL_TEMPLATE_PATH
        fi
    fi
fi

echo "Configuring environment..."
prompt DB_HOST "Database Host" "db"
prompt DB_PORT "Database Port" "5432"
prompt DB_USER "Database User" "clinica"
prompt DB_PASS "Database Password" "clinica"
prompt DB "Database Name" "clinica"
prompt DB_TYPE "Database Type" "postgres"

prompt LDAP_ENABLED "Enable LDAP? (true/false)" "false"
if [ "$LDAP_ENABLED" = "true" ]; then
    prompt LDAP_HOST "LDAP Host" "ldap://localhost:389"
fi

prompt HOST_FILE_PATH "Host File Path" "${DATA_DIR}"
prompt FILE_PATH "Container File Path" "/opt/clinica/data/"

prompt SEED_CLINICAL_DATA "Seed Clinical Data? (true/false)" "false"

if [ "$SEED_CLINICAL_DATA" = "true" ]; then
    prompt HOST_CLINICAL_TEMPLATE_PATH "Host Clinical Template Path" "${TEMPLATE_FILE}"
    prompt CLINICAL_TEMPLATE_PATH "Container Clinical Template Path" "/opt/clinica/template.xlsx"
else
    HOST_CLINICAL_TEMPLATE_PATH="${TEMPLATE_FILE}"
    CLINICAL_TEMPLATE_PATH=""
fi

# Verify Connectivity
check_connection() {
    local host=$1
    local port=$2
    local service=$3
    if [ "$SKIP_CONNECTIVITY" = false ]; then
        if [ "$host" = "db" ] || [ "$host" = "localhost" ] || [ "$host" = "127.0.0.1" ] || [ -z "$host" ]; then return 0; fi
        
        echo "Verifying connection to $service at $host:$port..."
        if timeout 5 bash -c "</dev/tcp/$host/$port" 2>/dev/null; then
            echo "Connection to $service successful."
        else
            echo "Error: Failed to connect to $service at $host:$port. Use -s to skip connectivity checks."
            exit 1
        fi
    fi
}

check_connection "$DB_HOST" "$DB_PORT" "Database"

if [ "$LDAP_ENABLED" = "true" ]; then
    ldap_hostname=$(echo "$LDAP_HOST" | sed -E 's|^[a-z]+://||' | cut -d: -f1)
    ldap_port=$(echo "$LDAP_HOST" | sed -E 's|^[a-z]+://||' | cut -d: -f2)
    if [ "$ldap_hostname" = "$ldap_port" ] || [ -z "$ldap_port" ]; then ldap_port=389; fi
    check_connection "$ldap_hostname" "$ldap_port" "LDAP"
fi

# Write .env file
cat <<EOF > "${ENV_FILE}.tmp"
DB_HOST=$DB_HOST
DB_PORT=$DB_PORT
DB_USER=$DB_USER
DB_PASS=$DB_PASS
DB=$DB
DB_TYPE=$DB_TYPE
LDAP_ENABLED=$LDAP_ENABLED
EOF

if [ "$LDAP_ENABLED" = "true" ]; then
    echo "LDAP_HOST=$LDAP_HOST" >> "${ENV_FILE}.tmp"
fi

cat <<EOF >> "${ENV_FILE}.tmp"
HOST_FILE_PATH=$HOST_FILE_PATH
FILE_PATH=$FILE_PATH
SEED_CLINICAL_DATA=$SEED_CLINICAL_DATA
HOST_CLINICAL_TEMPLATE_PATH=$HOST_CLINICAL_TEMPLATE_PATH
CLINICAL_TEMPLATE_PATH=$CLINICAL_TEMPLATE_PATH
EOF

mv "${ENV_FILE}.tmp" "${ENV_FILE}"
echo ".env generated successfully."

# Template Spreadsheet Logic
if [ "$SEED_CLINICAL_DATA" = "false" ]; then
    echo "Data seeding is disabled. Deploying a valid placeholder template to ${HOST_CLINICAL_TEMPLATE_PATH}..."
    cp "${VALID_TEMPLATE_SOURCE}" "${HOST_CLINICAL_TEMPLATE_PATH}"
else
    if [ ! -f "${HOST_CLINICAL_TEMPLATE_PATH}" ]; then
        echo "Creating missing template file at ${HOST_CLINICAL_TEMPLATE_PATH}..."
        touch "${HOST_CLINICAL_TEMPLATE_PATH}"
    fi
fi

# Trigger Container Orchestration
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
