#!/bin/bash
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
cd "$DIR"

echo "Regenerating REST and SOAP specifications..."
python3 utils/merge_openapi.py
python3 utils/extract_soap.py

echo "Checking for undocumented changes in generated specifications..."
if ! git diff --exit-code docs/openapi.json docs/reference/soap.md; then
    echo ""
    echo "ERROR: API documentation drift detected!"
    echo "The generated specifications (docs/openapi.json or docs/reference/soap.md) differ from the committed versions."
    echo "Please commit the updated API documentation files."
    exit 1
fi

echo "API specifications are perfectly aligned with version control."
