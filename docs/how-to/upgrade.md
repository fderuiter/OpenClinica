# Upgrade Guide

This how-to guide details the manual procedure for upgrading an active OpenClinica deployment without losing transactional data. Upgrading requires executing specific database migrations and updating configuration files.

## System Prerequisites

Your target environment must be running the following modern baseline:

- **Java Development Kit:** JDK 17
- **Database:** PostgreSQL 15
- **Application Server:** Tomcat 9

## Manual Execution Steps

Follow these manual instructions to safely upgrade your environment.

### 1. Stop the Application Server

To prevent active transactions from failing during the migration, stop Tomcat 9:

```bash
systemctl stop tomcat9
```

### 2. Backup the Database

Always create a backup of your PostgreSQL 15 database before executing migrations. Run this manual command:

```bash
pg_dump -U clinica -F c -f /backup/clinica_backup.dump clinica
```

### 3. Apply Database Migrations

Instead of performing a clean installation, you must apply the explicit database migrations for your target version. Execute the SQL migration scripts manually:

```bash
psql -U clinica -d clinica -f /migrations/update_schema.sql
```

Ensure no errors occur during this step. If errors arise, restore from your backup and investigate.

### 4. Deploy the New Application Package

Replace the existing application archive with the latest version.

```bash
rm -rf /opt/tomcat9/webapps/openclinica*
cp openclinica_new.war /opt/tomcat9/webapps/openclinica.war
```

### 5. Restart the Service

Finally, start Tomcat 9 using JDK 17 to complete the upgrade process.

```bash
systemctl start tomcat9
```

Monitor the application logs to verify that the migration and startup sequence finishes successfully.
