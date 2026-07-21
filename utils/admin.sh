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
    *)
        echo "Usage: $0 {backup|restore}"
        exit 1
        ;;
esac
