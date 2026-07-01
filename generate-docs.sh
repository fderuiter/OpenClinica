#!/bin/bash
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR"

# Fail the build if metadata cannot be extracted
PROJECT_VERSION=$(grep -m 1 '<version>' pom.xml | sed 's/.*<version>\(.*\)<\/version>.*/\1/')
if [ -z "$PROJECT_VERSION" ]; then echo "Error: Could not extract PROJECT_VERSION"; exit 1; fi

JDK_VERSION=$(grep -m 1 '<source>' pom.xml | sed 's/.*<source>\(.*\)<\/source>.*/\1/')
if [ -z "$JDK_VERSION" ]; then echo "Error: Could not extract JDK_VERSION"; exit 1; fi

TOMCAT_VERSION=$(grep -oP 'apache-tomcat-\K[0-9\.]+(?=\.tar\.gz)' Dockerfile | head -1)
if [ -z "$TOMCAT_VERSION" ]; then echo "Error: Could not extract TOMCAT_VERSION"; exit 1; fi

PG_VERSION=$(grep -oP 'postgres:\K[0-9]+' docker-compose.yml | head -1)
if [ -z "$PG_VERSION" ]; then echo "Error: Could not extract PG_VERSION"; exit 1; fi

GENERATED_CONTENT="<!-- BEGIN GENERATED REQUIREMENTS -->
> **Note:** The following technical requirements are programmatically generated from the build configuration. Manual modifications will be overwritten.

- **Project Version:** $PROJECT_VERSION
- **JDK Version:** $JDK_VERSION
- **Database:** PostgreSQL $PG_VERSION
- **Application Server:** Tomcat $TOMCAT_VERSION

For non-technical user guides and comprehensive documentation, please visit the [OpenClinica Documentation Portal](https://docs.openclinica.com).
<!-- END GENERATED REQUIREMENTS -->"

awk -v content="$GENERATED_CONTENT" '
    /<!-- BEGIN GENERATED REQUIREMENTS -->/ {
        print content
        skip = 1
        next
    }
    /<!-- END GENERATED REQUIREMENTS -->/ {
        skip = 0
        next
    }
    !skip {
        print
    }
' README.md > README.md.tmp && mv README.md.tmp README.md

