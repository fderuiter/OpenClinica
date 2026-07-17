import os
import re

def remove_type_methods(path):
    with open(path, 'r') as f:
        code = f.read()

    # Remove calls
    code = re.sub(r'^\s*this\.set[A-Za-z0-9_]*TypesExpected.*?\n', '', code, flags=re.MULTILINE)
    code = re.sub(r'^\s*set[A-Za-z0-9_]*TypesExpected.*?\n', '', code, flags=re.MULTILINE)
    # Remove things like // this.setTypesExpected();
    code = re.sub(r'^\s*//\s*this\.set[A-Za-z0-9_]*TypesExpected.*?\n', '', code, flags=re.MULTILINE)

    # Remove method declarations
    while True:
        m = re.search(r'\b(?:public|private|protected)\s+void\s+set[A-Za-z0-9_]*TypesExpected[A-Za-z0-9_]*\s*\([^)]*\)\s*\{', code)
        if not m:
            break
        start = m.start()
        brace_count = 1
        i = m.end()
        while i < len(code) and brace_count > 0:
            if code[i] == '{': brace_count += 1
            elif code[i] == '}': brace_count -= 1
            i += 1
        code = code[:start] + code[i:]

    with open(path, 'w') as f:
        f.write(code)

for root, dirs, files in os.walk('/app/core/src/main/java/org/akaza/openclinica/dao'):
    for file in files:
        if file.endswith('DAO.java') or file.endswith('Dao.java'):
            remove_type_methods(os.path.join(root, file))

