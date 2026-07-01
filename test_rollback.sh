#!/bin/bash
set -e
echo "Running automated verification of rollback paths..."
MISSING_ROLLBACK=$(grep -L "<rollback" $(grep -l "<sql>" $(find /app/core/src/main/resources/migration -name "*.xml")) || true)
if [ -n "$MISSING_ROLLBACK" ]; then
    echo "FAILED: The following migrations lack rollback paths for raw SQL blocks:"
    echo "$MISSING_ROLLBACK"
    exit 1
fi
echo "SUCCESS: All raw SQL blocks have verified rollback paths."
