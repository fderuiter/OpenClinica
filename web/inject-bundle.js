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
        content = `
          var bundleScript = document.createElement('script');
          bundleScript.type = 'module';
          bundleScript.src = app_contextPath + "/dist/${mainScript}";
          document.head.appendChild(bundleScript);

          var utilScript = document.createElement('script');
          utilScript.src = app_contextPath + "/js/util.js";
          document.head.appendChild(utilScript);
        `;
        changed = true;
      } else {
        // Match script tag for prototype.js and replace it with a module script
        const scriptTagRegex =
          /<script[^>]*src=(['"])([\.\/]*?)includes\/prototype\.js\1[^>]*><\/script>/g;
        if (scriptTagRegex.test(content)) {
          content = content.replace(
            scriptTagRegex,
            `<script>
          window._calQueue = window._calQueue || [];
          window.Calendar = { setup: function(c) { window._calQueue.push(c); } };
          window.Tip = function() {};
          window.UnTip = function() {};
          window.TagToTip = function() {};
          </script>\n<script type="module" src=$1$2dist/${mainScript}$1></script>`
          );
          changed = true;
        } else {
          // Fallback if it's just the path
          const regex = /(['"])([\.\/]*?)includes\/prototype\.js\1/g;
          if (regex.test(content)) {
            content = content.replace(regex, (match, quote, prefix) => {
              return `${quote}${prefix}dist/${mainScript}${quote}`;
            });
            changed = true;
          }
        }

        // Remove legacy scripts and links that were replaced by the bundle
        const legacyRegex =
          /<script[\s\S]*?includes\/(?:scriptaculous|effects|ua-parser\.min|CalendarPopup|new_cal|wz_tooltip)[\s\S]*?<\/script>\s*|<link[\s\S]*?includes\/(?:new_cal|wz_tooltip)[\s\S]*?>\s*/g;
        if (legacyRegex.test(content)) {
          content = content.replace(legacyRegex, '');
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
