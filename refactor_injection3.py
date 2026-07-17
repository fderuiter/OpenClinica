import os
import re

def replace_new_calls(code, dao_name, var_name):
    # Find all "new XxxDAO"
    idx = 0
    while True:
        idx = code.find(f"new {dao_name}(", idx)
        if idx == -1:
            break
        
        # We found "new XxxDAO(". Now find the matching closing parenthesis.
        start = idx
        paren_idx = idx + len(f"new {dao_name}(")
        brace_count = 1
        i = paren_idx
        while i < len(code) and brace_count > 0:
            if code[i] == '(':
                brace_count += 1
            elif code[i] == ')':
                brace_count -= 1
            i += 1
            
        if brace_count == 0:
            # We found the end!
            code = code[:start] + f"this.{var_name}" + code[i:]
            idx = start + len(f"this.{var_name}")
        else:
            idx = paren_idx
            
    return code

def process_file(path):
    with open(path, 'r') as f:
        code = f.read()

    orig_code = code

    dao_matches = re.findall(r'new\s+([A-Z]\w*DAO)\s*\(', code)
    if not dao_matches:
        return code

    dao_types = sorted(list(set(dao_matches)))

    class_match = re.search(r'\b(?:public|protected|private)?\s*(?:abstract\s+)?class\s+([A-Z]\w+)', code)
    if not class_match:
        return code
    class_name = class_match.group(1)

    fields = []
    params = []
    assignments = []
    
    for dt in dao_types:
        if dt.endswith('DAO'):
            var_name = dt[0].lower() + dt[1:len(dt)-3] + 'DAO'
        else:
            var_name = dt[0].lower() + dt[1:]
        var_name = '_' + var_name
        
        fields.append(f"    private {dt} {var_name};")
        params.append(f"{dt} {var_name}")
        assignments.append(f"        this.{var_name} = {var_name};")

    for dt in dao_types:
        if dt.endswith('DAO'):
            var_name = dt[0].lower() + dt[1:len(dt)-3] + 'DAO'
        else:
            var_name = dt[0].lower() + dt[1:]
        var_name = '_' + var_name
        
        code = replace_new_calls(code, dt, var_name)

    if 'import org.springframework.beans.factory.annotation.Autowired;' not in code:
        code = re.sub(r'(package\s+[^;]+;\n+)', r'\1import org.springframework.beans.factory.annotation.Autowired;\nimport org.springframework.stereotype.Component;\n', code, count=1)

    if '@Component' not in code and '@Service' not in code and '@Controller' not in code and '@RestController' not in code and '@Repository' not in code:
        code = re.sub(r'(public\s+(?:abstract\s+)?class\s+[A-Z])', r'@Component\n\1', code, count=1)

    constructor_pattern = r'(public\s+' + class_name + r'\s*\()([^)]*)(\)\s*(?:throws\s+[^{]+)?\{)'
    match = re.search(constructor_pattern, code)
    
    class_decl_match = re.search(r'\b(?:public|protected|private)?\s*(?:abstract\s+)?class\s+' + class_name + r'[^\{]*\{', code)
    if not class_decl_match:
        return orig_code
        
    if match:
        existing_params = match.group(2).strip()
        if existing_params:
            new_params = existing_params + ", " + ", ".join(params)
        else:
            new_params = ", ".join(params)
            
        idx = match.end()
        super_match = re.match(r'\s*super\s*\([^)]*\)\s*;', code[idx:])
        if super_match:
            idx += super_match.end()
            
        insertion = "\n" + "\n".join(assignments) + "\n"
        code = code[:match.start()] + "@Autowired\n    " + match.group(1) + new_params + match.group(3) + code[match.end():idx] + insertion + code[idx:]
        
        # Also need to insert fields right after class {
        code = code[:class_decl_match.end()] + "\n" + "\n".join(fields) + "\n" + code[class_decl_match.end():]
        
    else:
        insertion = "\n" + "\n".join(fields) + "\n\n"
        insertion += "    @Autowired\n"
        insertion += f"    public {class_name}(" + ", ".join(params) + ") {\n"
        insertion += "\n".join(assignments) + "\n"
        insertion += "    }\n"
        
        code = code[:class_decl_match.end()] + insertion + code[class_decl_match.end():]

    return code

def main():
    modified = 0
    for root, dirs, files in os.walk('/app/core/src/main/java'):
        for f in files:
            if f.endswith('.java'):
                path = os.path.join(root, f)
                new_code = process_file(path)
                if new_code != open(path).read():
                    open(path, 'w').write(new_code)
                    modified += 1
                    
    for root, dirs, files in os.walk('/app/web/src/main/java'):
        for f in files:
            if f.endswith('.java'):
                path = os.path.join(root, f)
                new_code = process_file(path)
                if new_code != open(path).read():
                    open(path, 'w').write(new_code)
                    modified += 1
    
    print(f"Modified {modified} files")

if __name__ == '__main__':
    main()
