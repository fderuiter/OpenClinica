#!/usr/bin/env bash
set -e

echo "Running exact-match duplication guard..."

# Find all markdown files, ignoring node_modules and explicitly exempting README.md files
# We calculate checksums and find duplicates.
duplicate_hashes=$(find . -type d -name "node_modules" -prune -o -type f -name "*.md" -print | grep -vi '/readme\.md$' | xargs md5sum | awk '{print $1}' | sort | uniq -d)

if [ -n "$duplicate_hashes" ]; then
    echo "ERROR: Exact-match duplicate markdown files detected!"
    for hash in $duplicate_hashes; do
        echo "Duplicate group ($hash):"
        find . -type d -name "node_modules" -prune -o -type f -name "*.md" -print | grep -vi '/readme\.md$' | xargs md5sum | grep "^$hash" | awk '{print "  - "$2}'
    done
    exit 1
fi

echo "No unauthorized duplicates found."
exit 0
