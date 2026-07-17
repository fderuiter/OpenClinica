import os
import re

def process_method_calls(text, method_name, append_arg=None):
    idx = 0
    while True:
        match = re.search(r'\bthis\.' + method_name + r'\(', text[idx:])
        if not match:
            break
        
        start_idx = idx + match.end() - 1 # points to '('
        paren_count = 1
        i = start_idx + 1
        while i < len(text) and paren_count > 0:
            if text[i] == '(': paren_count += 1
            elif text[i] == ')': paren_count -= 1
            i += 1
        
        if append_arg:
            args = text[start_idx+1 : i-1]
            if args.strip() == '':
                new_args = append_arg
            else:
                new_args = args + ', ' + append_arg
            text = text[:start_idx+1] + new_args + text[i-1:]
            idx = start_idx + 1 + len(new_args) + 1
        else:
            idx = i
    return text

def refactor_file(path):
    with open(path, 'r') as f:
        code = f.read()

    orig_code = code

    # 1. Add @Repository and @Autowired
    if 'extends EntityDAO' in code or 'extends AuditableEntityDAO' in code:
        if 'import org.springframework.stereotype.Repository;' not in code:
            code = re.sub(r'^package ', 'package ', code) # dummy
            code = re.sub(r'(import .*;\n)', r'\1import org.springframework.stereotype.Repository;\nimport org.springframework.beans.factory.annotation.Autowired;\n', code, count=1)
        
        if '@Repository' not in code:
            code = re.sub(r'(public class [A-Za-z0-9_]+DAO)', r'@Repository\n\1', code)
            
        class_name = re.search(r'public class ([A-Za-z0-9_]+DAO)', code)
        if class_name:
            cname = class_name.group(1)
            code = re.sub(r'(public ' + cname + r'\s*\([^\)]*DataSource[^\)]*\)\s*\{)', r'@Autowired\n    \1', code)

    # 2. Refactor types
    code = re.sub(r'this\.unsetTypeExpected\(\);', 'java.util.HashMap<Integer, Integer> _types = new java.util.HashMap<Integer, Integer>();', code)
    code = re.sub(r'\bunsetTypeExpected\(\);', 'java.util.HashMap<Integer, Integer> _types = new java.util.HashMap<Integer, Integer>();', code)
    
    code = re.sub(r'this\.setTypeExpected\(', '_types.put(', code)
    code = re.sub(r'\bsetTypeExpected\(', '_types.put(', code)
    
    code = re.sub(r'public void (set[A-Za-z0-9_]*TypesExpected\d*)\(\)', r'public java.util.HashMap<Integer, Integer> \1()', code)
    code = re.sub(r'this\.(set[A-Za-z0-9_]*TypesExpected\d*)\(\);', r'java.util.HashMap<Integer, Integer> _types = this.\1();', code)
    
    # insert return _types
    # We will search by index
    idx = 0
    while True:
        m = re.search(r'(public java\.util\.HashMap<Integer, Integer> set[A-Za-z0-9_]*TypesExpected\d*\(\)\s*\{)', code[idx:])
        if not m: break
        start = idx + m.end()
        # skip if already processed
        if '/*return_added*/' in code[idx + m.start() : start + 30]:
            idx = start
            continue
            
        code = code[:start] + '/*return_added*/' + code[start:]
        start += len('/*return_added*/')
        
        brace_count = 1
        i = start
        while i < len(code) and brace_count > 0:
            if code[i] == '{': brace_count += 1
            elif code[i] == '}': brace_count -= 1
            i += 1
            
        code = code[:i-1] + "\n        return _types;\n    " + code[i-1:]
        idx = start

    code = process_method_calls(code, 'select', '_types')
    code = process_method_calls(code, 'selectByCache', '_types')
    
    code = re.sub(r'(this\.execute\()', r'_querySuccessful = \1', code)
    code = re.sub(r'(this\.executeWithPK\()', r'_latestPK = \1', code)
    code = re.sub(r'\bisQuerySuccessful\(\)', r'_querySuccessful', code)
    code = re.sub(r'\bgetLatestPK\(\)', r'_latestPK', code)
    
    def inject_locals(text):
        res = []
        brace_level = 0
        for line in text.split('\n'):
            if '{' in line:
                brace_level += line.count('{')
            if '}' in line:
                brace_level -= line.count('}')
            
            res.append(line)
            
            if brace_level == 2 and '{' in line and ('public ' in line or 'protected ' in line or 'private ' in line) and '(' in line and ')' in line and '=' not in line:
                # To be safe, just inject it in ALL methods inside DAOs.
                res.append('        boolean _querySuccessful = true; int _latestPK = 0;')
        return '\n'.join(res)

    code = inject_locals(code)

    if code != orig_code:
        with open(path, 'w') as f:
            f.write(code)
        print(f"Refactored {path}")

for root, dirs, files in os.walk('/app/core/src/main/java/org/akaza/openclinica/dao'):
    for file in files:
        if file.endswith('DAO.java') or file.endswith('Dao.java'):
            refactor_file(os.path.join(root, file))

