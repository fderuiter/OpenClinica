#!/bin/bash
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR"

# Fail the build if metadata cannot be extracted
PROJECT_VERSION=$(python3 -c "import xml.etree.ElementTree as ET; tree = ET.parse('pom.xml'); root = tree.getroot(); print([elem.text for elem in root.iter('{http://maven.apache.org/POM/4.0.0}version')][0])")
if [ -z "$PROJECT_VERSION" ]; then echo "Error: Could not extract PROJECT_VERSION"; exit 1; fi

JDK_VERSION=$(python3 -c "import xml.etree.ElementTree as ET; tree = ET.parse('pom.xml'); root = tree.getroot(); print([elem.text for elem in root.iter('{http://maven.apache.org/POM/4.0.0}source')][0])")
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

# Check if README.md exists and if it was modified manually
if [ -f "README.md" ]; then
    STORED_CHECKSUM=$(tail -n 1 README.md | grep -oP '(?<=<!-- CHECKSUM: )[a-f0-9]+(?= -->)' || echo "")
    if [ ! -z "$STORED_CHECKSUM" ]; then
        ACTUAL_CHECKSUM=$(head -n -1 README.md | md5sum | awk '{print $1}')
        if [ "$STORED_CHECKSUM" != "$ACTUAL_CHECKSUM" ]; then
            echo "Error: README.md was modified manually! Please edit README.template.md instead."
            exit 1
        fi
    else
        echo "Warning: README.md lacks a checksum. It may have been manually modified or created from an older version. Overwriting."
    fi
fi

awk -v content="$GENERATED_CONTENT" '{
    gsub(/\{\{GENERATED_REQUIREMENTS\}\}/, content)
    print
}' README.template.md > README.md.tmp

NEW_CHECKSUM=$(md5sum README.md.tmp | awk '{print $1}')
cat README.md.tmp > README.md
echo "<!-- CHECKSUM: $NEW_CHECKSUM -->" >> README.md
rm README.md.tmp

# Install MkDocs if not present
if ! python3 -m mkdocs --version &> /dev/null; then
    python3 -m pip install mkdocs mkdocs-material pyyaml --break-system-packages || python3 -m pip install mkdocs mkdocs-material pyyaml
fi

# Ensure docs directory has the latest README
cp README.md docs/project-info.md

# Validate markdown file locations
APPROVED_DIRS=("^docs/$" "^docs/installation/$" "^docs/maintenance/$" "^docs/configuration/$" "^docs/frontend/$" "^docs/frontend-api/" "^docs/diataxis/tutorials/$" "^docs/diataxis/how-to/$" "^docs/diataxis/references/$" "^docs/diataxis/explanation/$")

for file in $(find docs -name '*.md'); do
    dir_path=$(dirname "$file")"/"
    matched=false
    for pattern in "${APPROVED_DIRS[@]}"; do
        if echo "$dir_path" | grep -Eq "$pattern"; then
            matched=true
            break
        fi
    done
    if [ "$matched" = false ]; then
        echo "Error: Markdown file $file is outside the approved structure ($dir_path)."
        exit 1
    fi
done

# Generate frontend API documentation
if [ -d "web" ] && [ -f "web/package.json" ]; then
    echo "Generating frontend API documentation..."
    cd web
    npm install
    npm run docs
    cd ..
fi

# Generate REST API docs (Unified OpenAPI)
python3 merge_openapi.py

# Extract static SOAP definitions
python3 extract_soap.py

# Build the documentation site
python3 -m mkdocs build


