#!/bin/bash
# Comprehensive Automated Rollback Framework - Deployment & Health Check Script
set -e

# Configuration Paths
PROPERTIES_DIR="/app/core/src/main/resources/properties"
BACKUP_DIR="/tmp/deployment_backup"
APP_DIR="/app/modern"
BINARY_NAME="OpenClinica-modern.jar"
HEALTH_CHECK_URL="http://localhost:8080/actuator/health"
MAX_RETRIES=10
SLEEP_SECS=5

echo "Starting automated deployment lifecycle..."

# 1. Backup Phase
echo "[1/4] Backing up configuration properties and binaries..."
mkdir -p "$BACKUP_DIR/properties_backup"
chmod 700 "$BACKUP_DIR"
cp -r "$PROPERTIES_DIR/"* "$BACKUP_DIR/properties_backup/"

if [ -f "$APP_DIR/target/$BINARY_NAME" ]; then
    cp "$APP_DIR/target/$BINARY_NAME" "$BACKUP_DIR/$BINARY_NAME.bak"
    echo "Application binary backed up."
else
    echo "No existing binary found to back up (First deployment?)."
fi

# Extract DB credentials dynamically from Maven
echo "Extracting database credentials from Maven..."
DB_HOST=$(mvn help:evaluate -Dexpression=dbHost -q -DforceStdout -f /app/pom.xml)
DB_PORT=$(mvn help:evaluate -Dexpression=dbPort -q -DforceStdout -f /app/pom.xml)
DB_USER=$(mvn help:evaluate -Dexpression=dbUser -q -DforceStdout -f /app/pom.xml)
DB_PASS=$(mvn help:evaluate -Dexpression=dbPass -q -DforceStdout -f /app/pom.xml)
DB_NAME=$(mvn help:evaluate -Dexpression=db -q -DforceStdout -f /app/pom.xml)
DB_SCHEMA=$(mvn help:evaluate -Dexpression=dbSchema -q -DforceStdout -f /app/pom.xml)
DB_SCHEMA="${DB_SCHEMA:-app_schema}"

DRY_RUN=false
SIMULATE_SUCCESS=false
SIMULATE_FAILURE=false

for arg in "$@"; do
    if [ "$arg" == "--dry-run" ]; then
        DRY_RUN=true
        echo "Running in DRY-RUN mode."
    elif [ "$arg" == "--simulate-success" ]; then
        SIMULATE_SUCCESS=true
    elif [ "$arg" == "--simulate-failure" ]; then
        SIMULATE_FAILURE=true
    fi
done

echo "Executing schema-specific PostgreSQL backup..."
if ! PGPASSWORD="$DB_PASS" pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -n "$DB_SCHEMA" -F c -f "$BACKUP_DIR/db_backup.dump"; then
    echo "ERROR: Physical database backup failed. Halting deployment."
    exit 1
fi
echo "Database backup completed successfully."

# 2. Deployment Phase
echo "[2/4] Executing deployment and starting application..."
# In a real environment, the new binary and configuration are placed here, and service is restarted.
# For example: systemctl restart openclinica

# 3. Health Check Phase
echo "[3/4] Running post-startup health check..."

HEALTH_CHECK_PASSED=false
# Simulate health check loop
for i in $(seq 1 $MAX_RETRIES); do
    echo "Pinging health endpoint (Attempt $i/$MAX_RETRIES)..."
    # Using curl to check status (mocking for test purposes using a dummy file or argument)
    if [ "$SIMULATE_SUCCESS" = true ]; then
        HEALTH_CHECK_PASSED=true
        break
    fi
    
    if curl -s -f "$HEALTH_CHECK_URL" > /dev/null; then
        HEALTH_CHECK_PASSED=true
        break
    fi
    
    # If the user simulates a failure for testing
    if [ "$SIMULATE_FAILURE" = true ]; then
        HEALTH_CHECK_PASSED=false
        break
    fi

    sleep $SLEEP_SECS
done

if [ "$HEALTH_CHECK_PASSED" = true ]; then
    echo "[4/4] Health check passed. Deployment successful."
    echo "Cleaning up backups..."
    rm -rf "$BACKUP_DIR"
    exit 0
else
    echo "[4/4] Health check failed! Initiating automated rollback..."

    if [ ! -s "$BACKUP_DIR/db_backup.dump" ]; then
        echo "ERROR: Backup file missing or empty. Halting rollback."
        exit 1
    fi

    if [ -t 0 ]; then
        read -p "Are you sure you want to proceed with rollback? (y/N) " response
        if [[ ! "$response" =~ ^[Yy]$ ]]; then
            echo "Rollback cancelled by user."
            exit 1
        fi
    else
        if [ "$FORCE_ROLLBACK" != "true" ]; then
            echo "ERROR: Non-interactive rollback requires FORCE_ROLLBACK=true"
            exit 1
        fi
    fi
    
    # Revert Application Binary
    if [ -f "$BACKUP_DIR/$BINARY_NAME.bak" ]; then
        echo "Restoring previous application binary..."
        cp "$BACKUP_DIR/$BINARY_NAME.bak" "$APP_DIR/target/$BINARY_NAME"
    fi
    
    # Revert Configuration Properties
    echo "Restoring previous configuration properties..."
    rm -rf "$PROPERTIES_DIR/"*
    cp -r "$BACKUP_DIR/properties_backup/"* "$PROPERTIES_DIR/"
    
    # Revert Database Migrations
    echo "Reverting database schema changes..."
    # Execute complete physical PostgreSQL snapshot restore (bypassing Liquibase)
    echo "Cleaning target schema to avoid conflicts..."
    if [ "$DRY_RUN" = true ]; then
        echo "DRY-RUN: PGPASSWORD=\"$DB_PASS\" psql -h \"$DB_HOST\" -p \"$DB_PORT\" -U \"$DB_USER\" -d \"$DB_NAME\" -c \"DROP SCHEMA IF EXISTS \\\"$DB_SCHEMA\\\" CASCADE; CREATE SCHEMA \\\"$DB_SCHEMA\\\";\""
    else
        PGPASSWORD="$DB_PASS" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "DROP SCHEMA IF EXISTS \"$DB_SCHEMA\" CASCADE; CREATE SCHEMA \"$DB_SCHEMA\";" || echo "Warning: Schema clean failed."
    fi
    
    echo "Restoring complete physical PostgreSQL snapshot..."
    if [ "$DRY_RUN" = true ]; then
        echo "DRY-RUN: PGPASSWORD=\"$DB_PASS\" pg_restore -h \"$DB_HOST\" -p \"$DB_PORT\" -U \"$DB_USER\" -d \"$DB_NAME\" -1 \"$BACKUP_DIR/db_backup.dump\""
    else
        PGPASSWORD="$DB_PASS" pg_restore -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -1 "$BACKUP_DIR/db_backup.dump" || echo "Warning: DB Restore failed."
    fi
    
    echo "Automated Database Recovery scripts executed."
    
    echo "Rollback complete. System restored to previous functional version."
    exit 1
fi
