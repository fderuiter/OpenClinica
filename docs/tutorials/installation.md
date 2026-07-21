# Installation Guide

Welcome to the installation guide for OpenClinica. This guide will walk you through the manual steps required to set up a clean deployment on a Linux server. By the end of this tutorial, you will have a running instance configured with the modern system baseline.

## System Prerequisites

Before beginning the installation, ensure that your environment meets the strict baseline requirements. Legacy configurations are no longer supported.

- **Java Development Kit:** JDK 17 is strictly required.
- **Database:** PostgreSQL 15 must be installed and running.
- **Application Server:** Tomcat 9 is necessary to host the application.

## Manual Execution Steps

Follow these manual instructions to complete the clean deployment.

### 1. Database Configuration

Start by preparing your database. Open your PostgreSQL 15 shell and execute the following manual commands to create the dedicated database and user:

```sql
CREATE DATABASE clinica;
CREATE USER clinica WITH PASSWORD 'clinica';
GRANT ALL PRIVILEGES ON DATABASE clinica TO clinica;
```

### 2. Application Server Setup

Next, configure Tomcat 9. Navigate to your Tomcat 9 installation directory and ensure that the memory settings are appropriate for your environment. We rely on the modern garbage collector in JDK 17, so avoid using deprecated JVM flags in your environment variables.

### 3. Deploying the Application

Once the database and application server are ready, place the application package into the Tomcat 9 webapps directory.

```bash
cp openclinica.war /opt/tomcat9/webapps/
```

Restart the Tomcat 9 service to initiate the deployment.

```bash
systemctl restart tomcat9
```

### 4. Verification

Finally, verify that the deployment was successful by navigating to the application root URL in your browser. The initial startup may take a few minutes as the database schema is initialized.

```bash
curl http://localhost:8080/openclinica
```

Congratulations! You have successfully completed the manual deployment process for OpenClinica.
