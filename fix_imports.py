import os
import re

missing_imports = {
    'EventCRFDAO': 'org.akaza.openclinica.dao.submit.EventCRFDAO',
    'SubjectGroupMapDAO': 'org.akaza.openclinica.dao.submit.SubjectGroupMapDAO',
    'RuleSetDAO': 'org.akaza.openclinica.dao.rule.RuleSetDAO'
}

for root, dirs, files in os.walk('/app'):
    for f in files:
        if f.endswith('.java'):
            path = os.path.join(root, f)
            with open(path, 'r') as file:
                code = file.read()
            
            orig_code = code
            for class_name, import_path in missing_imports.items():
                if class_name in code and import_path not in code:
                    code = re.sub(r'(package\s+[^;]+;\n+)', r'\1import ' + import_path + ';\n', code, count=1)
            
            if code != orig_code:
                with open(path, 'w') as file:
                    file.write(code)

print("Imports fixed")
