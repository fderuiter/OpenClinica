const fs = require('fs');
const crypto = require('crypto');
const path = require('path');

if (!fs.existsSync('local-pkgs-hashes.json')) {
    console.error("local-pkgs-hashes.json missing");
    process.exit(1);
}

const expectedHashes = JSON.parse(fs.readFileSync('local-pkgs-hashes.json', 'utf8'));
let failed = false;

const actualFiles = [];
function findFiles(dir) {
    if (!fs.existsSync(dir)) return;
    for (const f of fs.readdirSync(dir)) {
        const full = path.join(dir, f);
        if (fs.statSync(full).isDirectory()) findFiles(full);
        else if (full.endsWith('.tgz')) actualFiles.push(full.replace(/\\\\/g, '/'));
    }
}
findFiles('local-pkgs');

for (const f of actualFiles) {
    if (!expectedHashes[f]) {
        console.error(`Unexpected local package found (not in hashes): ${f}`);
        failed = true;
    }
}

for (const [filePath, expectedHash] of Object.entries(expectedHashes)) {
    if (!fs.existsSync(filePath)) {
        console.error(`File missing: ${filePath}`);
        failed = true;
        continue;
    }
    const fileBuffer = fs.readFileSync(filePath);
    const actualHash = crypto.createHash('sha256').update(fileBuffer).digest('hex');
    if (actualHash !== expectedHash) {
        console.error(`Hash mismatch for ${filePath}. Expected ${expectedHash}, got ${actualHash}`);
        failed = true;
    }
}

if (failed) {
    console.error("Local package hash verification failed.");
    process.exit(1);
} else {
    console.log("Local package hash verification passed.");
}
