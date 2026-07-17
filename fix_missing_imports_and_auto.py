import os
import re

dao_imports = {
    'ItemDAO': 'org.akaza.openclinica.dao.submit.ItemDAO',
    'CRFDAO': 'org.akaza.openclinica.dao.admin.CRFDAO',
    'CRFVersionDAO': 'org.akaza.openclinica.dao.submit.CRFVersionDAO',
    'SubjectDAO': 'org.akaza.openclinica.dao.submit.SubjectDAO',
    'Component': 'org.springframework.stereotype.Component'
}

for root, dirs, files in os.walk('/app/core/src/main/java'):
    for f in files:
        if f.endswith('.java'):
            path = os.path.join(root, f)
            with open(path, 'r') as file:
                code = file.read()
            
            orig_code = code
            for class_name, import_path in dao_imports.items():
                if class_name in code and import_path not in code:
                    code = re.sub(r'(package\s+[^;]+;\n+)', r'\1import ' + import_path + ';\n', code, count=1)
            
            # fix double autowired
            code = re.sub(r'@Autowired\s*\n\s*@Autowired', '@Autowired', code)
            
            if code != orig_code:
                with open(path, 'w') as file:
                    file.write(code)

print("Fixed imports and duplicate autowireds")
