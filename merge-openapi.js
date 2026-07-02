const fs = require('fs');

const legacySpec = JSON.parse(fs.readFileSync('/app/web/openapi.json', 'utf8'));
const modernSpec = JSON.parse(fs.readFileSync('/app/modern/target/openapi.json', 'utf8'));

// Merge paths
const mergedPaths = { ...legacySpec.paths, ...modernSpec.paths };

// Merge components.schemas
const mergedSchemas = { 
  ...(legacySpec.components && legacySpec.components.schemas ? legacySpec.components.schemas : {}),
  ...(modernSpec.components && modernSpec.components.schemas ? modernSpec.components.schemas : {})
};

const mergedSpec = {
  openapi: "3.0.1",
  info: {
    title: "Unified API Infrastructure",
    version: "1.0.0",
    description: "Merged OpenAPI specification for Legacy and Modern endpoints."
  },
  servers: [
    { url: "http://localhost:8080", description: "Default Server" }
  ],
  paths: mergedPaths,
  components: {
    schemas: mergedSchemas
  }
};

fs.writeFileSync('/app/modern/target/merged-openapi.json', JSON.stringify(mergedSpec, null, 2));
console.log("Successfully merged legacy and modern OpenAPI specs into /app/modern/target/merged-openapi.json");
