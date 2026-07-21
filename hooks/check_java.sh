#!/bin/bash
PMD_VERSION="6.38.0" # matching the version in the project
PMD_DIR="/tmp/pmd-bin-$PMD_VERSION"

if [ ! -d "$PMD_DIR" ]; then
    curl -sL https://github.com/pmd/pmd/releases/download/pmd_releases%2F$PMD_VERSION/pmd-bin-$PMD_VERSION.zip -o /tmp/pmd.zip
    unzip -q /tmp/pmd.zip -d /tmp/
fi

# Join all arguments with comma
FILES=$(IFS=,; echo "$*")

# Run PMD only on the staged files
$PMD_DIR/bin/run.sh pmd -d "$FILES" -R pmd-ruleset.xml -f text
if [ $? -ne 0 ]; then
    echo "PMD validation failed for staged Java files."
    exit 1
fi
