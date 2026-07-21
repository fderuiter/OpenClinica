const fs = require('fs');
const path = require('path');

const manifestPath = path.join(
  __dirname,
  'src/main/webapp/dist/.vite/manifest.json'
);
const manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf-8'));
const mainScript = manifest['src/main/webapp/js/main.js'].file;

const srcDir = path.join(__dirname, 'src/main/webapp');
const outDir = path.join(__dirname, 'target/vite-resources');

function copyAndReplace(dir) {
  if (!fs.existsSync(dir)) return;
  const files = fs.readdirSync(dir);
  for (const file of files) {
    const fullPath = path.join(dir, file);
    const relPath = path.relative(srcDir, fullPath);
    const outPath = path.join(outDir, relPath);

    if (fs.statSync(fullPath).isDirectory()) {
      copyAndReplace(fullPath);
    } else if (
      fullPath.endsWith('.jsp') ||
      fullPath.endsWith('.html') ||
      fullPath.endsWith('.htm') ||
      fullPath.endsWith('load_scripts.js')
    ) {
      let content = fs.readFileSync(fullPath, 'utf-8');
      let changed = false;

      if (fullPath.endsWith('load_scripts.js')) {
        content = `head.js(
                         app_contextPath + "/dist/${mainScript}",
                         app_contextPath + "/js/util.js"
                       );`;
        changed = true;
      } else {
        // Match prototype.js
        const regex = /(['"])([\.\/]*?)includes\/prototype\.js\1/g;
        if (regex.test(content)) {
          content = content.replace(regex, (match, quote, prefix) => {
            return `${quote}${prefix}dist/${mainScript}${quote}`;
          });
          changed = true;
        }

        // Remove scriptaculous and effects
        const scriptRegex =
          /<script[^>]*src="[^"]*includes\/(scriptaculous|effects|ua-parser\.min)\.js(\?load=effects)?"[^>]*><\/script>\s*/g;
        if (scriptRegex.test(content)) {
          content = content.replace(scriptRegex, '');
          changed = true;
        }

        const headRegex =
          /<script[^>]*src="[^"]*\/js\/lib\/head\.min\.js"[^>]*><\/script>\s*/g;
        if (headRegex.test(content)) {
          content = content.replace(headRegex, '');
          changed = true;
        }
      }

      if (changed) {
        fs.mkdirSync(path.dirname(outPath), { recursive: true });
        fs.writeFileSync(outPath, content);
      }
    }
  }
}

copyAndReplace(srcDir);
console.log('Successfully prepared resources with bundle:', mainScript);
