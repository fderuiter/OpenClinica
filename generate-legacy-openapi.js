const fs = require('fs');
const path = require('path');

const walkSync = (dir, filelist = []) => {
    fs.readdirSync(dir).forEach(file => {
        const dirFile = path.join(dir, file);
        if (fs.statSync(dirFile).isDirectory()) {
            filelist = walkSync(dirFile, filelist);
        } else if (dirFile.endsWith('.java')) {
            filelist.push(dirFile);
        }
    });
    return filelist;
};

const files = walkSync('/app/web/src/main/java');
const openapi = {
    openapi: "3.0.0",
    info: { title: "Legacy API", version: "1.0.0" },
    paths: {}
};

files.forEach(file => {
    const content = fs.readFileSync(file, 'utf8');
    const regex = /@api\s+\{([^}]+)\}\s+([^\s]+)\s+(.*)/g;
    let match;
    while ((match = regex.exec(content)) !== null) {
        const method = match[1].toLowerCase();
        let apiPath = match[2];
        const summary = match[3].trim();
        
        // Convert :param to {param}
        const pathParams = [];
        apiPath = apiPath.replace(/:([a-zA-Z0-9_]+)/g, (m, p1) => {
            if (!pathParams.includes(p1)) pathParams.push(p1);
            return `{${p1}}`;
        });
        
        let paramMatch;
        const braceRegex = /\{([a-zA-Z0-9_]+)\}/g;
        while ((paramMatch = braceRegex.exec(apiPath)) !== null) {
            if (!pathParams.includes(paramMatch[1])) {
                pathParams.push(paramMatch[1]);
            }
        }
        
        if (!openapi.paths[apiPath]) openapi.paths[apiPath] = {};
        openapi.paths[apiPath][method] = {
            summary: summary,
            responses: { "200": { description: "Successful operation" } },
            parameters: pathParams.map(p => ({
                name: p,
                in: "path",
                required: true,
                schema: { type: "string" }
            }))
        };
    }
});

fs.writeFileSync('/app/web/openapi.json', JSON.stringify(openapi, null, 2));
console.log("Generated /app/web/openapi.json with " + Object.keys(openapi.paths).length + " paths");
