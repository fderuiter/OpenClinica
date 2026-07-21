#!/bin/bash
set -e
ACTION=$1
BACKUP_FILE=${BACKUP_FILE:-/tmp/backup/db_backup.dump}

case "$ACTION" in
    backup)
        echo "Starting backup to $BACKUP_FILE..."
        PGPASSWORD="$DB_PASS" pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -n "$DB_SCHEMA" -F c -f "$BACKUP_FILE"
        echo "Backup complete."
        ;;
    restore)
        echo "Starting restore from $BACKUP_FILE..."
        if [ ! -s "$BACKUP_FILE" ]; then
            echo "ERROR: Backup file missing or empty."
            exit 1
        fi
        PGPASSWORD="$DB_PASS" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 -c "DROP SCHEMA IF EXISTS \"$DB_SCHEMA\" CASCADE; CREATE SCHEMA \"$DB_SCHEMA\";"
        PGPASSWORD="$DB_PASS" pg_restore --clean --if-exists -1 -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" "$BACKUP_FILE"
        echo "Restore complete."
        ;;
    migrate)
        echo "Running Database Migrations..."
        DB_HOST=${DB_HOST:-db}
        DB_PORT=${DB_PORT:-5432}
        DB_USER=${DB_USER:-clinica}
        DB_PASS=${DB_PASS:-clinica}
        DB=${DB_NAME:-openclinica}

        # Run from host
        docker run --rm \
            --network=app_default \
            -v $(pwd)/core/src/main/resources:/liquibase/changelog \
            liquibase/liquibase:4.20.0 \
            --url="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB}" \
            --username="${DB_USER}" \
            --password="${DB_PASS}" \
            --changelog-file=migration/master.xml \
            update
        echo "Database Migrations Completed Successfully."
        ;;
    *)
        echo "Usage: $0 {backup|restore|migrate}"
        exit 1
        ;;
esac
