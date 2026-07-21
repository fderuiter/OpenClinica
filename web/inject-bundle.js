const fs = require('fs');
const path = require('path');
const { JSDOM } = require('jsdom');

const manifestPath = path.join(
  __dirname,
  'src/main/webapp/dist/.vite/manifest.json'
);
const manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf-8'));
const mainScript = manifest['src/main/webapp/js/main.js'].file;

const srcDir = path.join(__dirname, 'src/main/webapp');
const outDir = path.join(__dirname, 'target/vite-resources');

function modifyScripts(rootNode, mainScript) {
  let changed = false;
  const scripts = Array.from(rootNode.querySelectorAll('script'));
  for (const script of scripts) {
    const src = script.getAttribute('src');
    if (src && src.includes('includes/prototype.js')) {
      script.setAttribute('type', 'module');
      script.setAttribute('src', src.replace(/includes\/prototype\.js/, `dist/${mainScript}`));
      changed = true;
    } else if (src && (
      src.includes('includes/scriptaculous.js') ||
      src.includes('includes/effects.js') ||
      src.includes('includes/ua-parser.min.js') ||
      src.includes('/js/lib/head.min.js') ||
      src.includes('includes/head.min.js')
    )) {
      script.remove();
      changed = true;
    }
  }
  return changed;
}

function processTemplate(content, mainScript) {
  // 1. Extract leading directives
  const directiveRegex = /^\s*<%@\s[^%]*%>/;
  let directives = '';
  let remaining = content;
  while (true) {
    const match = remaining.match(directiveRegex);
    if (match) {
      directives += match[0];
      remaining = remaining.substring(match[0].length);
    } else {
      break;
    }
  }

  // 2. Shield remaining JSP custom tags and scriptlets
  const placeholders = [];
  let placeholderCounter = 0;
  
  // Matches <%...%> scriptlets and <prefix:name ...> or </prefix:name> tags
  const jspRegex = /(<%[\s\S]*?%>|<\/?[a-zA-Z0-9_-]+:[a-zA-Z0-9_-]+(?:\s+(?:[^"'>]|"[^"]*"|'[^']*')*)*\s*\/?>)/gi;

  const shielded = remaining.replace(jspRegex, (match) => {
    const id = placeholderCounter++;
    placeholders.push({ id, original: match });
    return `<!-- JSP_PLACEHOLDER_${id} -->`;
  });

  // 3. Determine parsing strategy
  const hasHtmlTag = /<html/i.test(shielded);
  let serialized = '';
  let changed = false;

  if (hasHtmlTag) {
    const dom = new JSDOM(shielded);
    const document = dom.window.document;
    
    changed = modifyScripts(document, mainScript);
    
    serialized = dom.serialize();
  } else {
    const dom = new JSDOM();
    const fragment = JSDOM.fragment(shielded);
    
    changed = modifyScripts(fragment, mainScript);
    
    const tempContainer = dom.window.document.createElement('div');
    tempContainer.appendChild(fragment);
    serialized = tempContainer.innerHTML;
  }

  // 4. Restore placeholders
  let restored = serialized;
  for (const ph of placeholders) {
    const unescaped = `<!-- JSP_PLACEHOLDER_${ph.id} -->`;
    const escaped = `&lt;!-- JSP_PLACEHOLDER_${ph.id} --&gt;`;
    
    restored = restored.split(unescaped).join(ph.original);
    restored = restored.split(escaped).join(ph.original);
  }

  // 5. Prepend top-level directives back
  let finalContent = directives + restored;

  // 6. Run fallback string replacement on final output (to catch raw javascript/inline occurrences)
  const fallbackRegex = /(['"])([\.\/]*?)includes\/prototype\.js\1/g;
  if (fallbackRegex.test(finalContent)) {
    finalContent = finalContent.replace(fallbackRegex, (match, quote, prefix) => {
      return `${quote}${prefix}dist/${mainScript}${quote}`;
    });
    changed = true;
  }

  return { finalContent, changed };
}

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
        const result = processTemplate(content, mainScript);
        content = result.finalContent;
        changed = result.changed;
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
