import os
import re

def process_file(path):
    with open(path, 'r') as f:
        code = f.read()

    orig_code = code

    # Match all "new XxxDAO(...)"
    dao_matches = re.findall(r'new\s+([A-Z]\w*DAO)\s*\([^)]*\)', code)
    if not dao_matches:
        return code

    dao_types = sorted(list(set(dao_matches)))

    # Prevent DAOs from being modified as instantiators to avoid breaking super(ds)
    # Wait, the instruction says "zero occurrences of legacy repository constructor calls using the 'new' keyword".
    # So DAOs MUST be modified if they instantiate other DAOs!
    # If a DAO instantiates another DAO, we can add it to the existing constructor without breaking super(ds)!

    # Find the class declaration line
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

    # Replace new XxxDAO(...)
    for dt in dao_types:
        if dt.endswith('DAO'):
            var_name = dt[0].lower() + dt[1:len(dt)-3] + 'DAO'
        else:
            var_name = dt[0].lower() + dt[1:]
        var_name = '_' + var_name
        
        # Careful replacement of "new XxxDAO(args)"
        # We use re.sub with a function to ensure we match balanced parentheses
        pattern = r'new\s+' + dt + r'\s*\([^)]*\)'
        code = re.sub(pattern, f'this.{var_name}', code)

    if 'import org.springframework.beans.factory.annotation.Autowired;' not in code:
        code = re.sub(r'(package\s+[^;]+;\n+)', r'\1import org.springframework.beans.factory.annotation.Autowired;\nimport org.springframework.stereotype.Component;\n', code, count=1)

    if '@Component' not in code and '@Service' not in code and '@Controller' not in code and '@RestController' not in code and '@Repository' not in code:
        code = re.sub(r'(public\s+(?:abstract\s+)?class\s+[A-Z])', r'@Component\n\1', code, count=1)

    # Inject fields and constructor
    # To be safe, we inject fields right before the last closing brace of the class? No, right after class declaration.
    
    # Let's find the primary constructor and modify it.
    # Pattern: public ClassName(
    constructor_pattern = r'(public\s+' + class_name + r'\s*\()([^)]*)(\)\s*(?:throws\s+[^{]+)?\{)'
    
    match = re.search(constructor_pattern, code)
    if match:
        # We append our parameters
        existing_params = match.group(2).strip()
        if existing_params:
            new_params = existing_params + ", " + ", ".join(params)
        else:
            new_params = ", ".join(params)
            
        # We inject assignments after the opening brace and super() call
        # Find the first '{' after match start
        idx = match.end()
        # Check if there's a super(...) call
        super_match = re.match(r'\s*super\s*\([^)]*\)\s*;', code[idx:])
        if super_match:
            idx += super_match.end()
            
        insertion = "\n" + "\n".join(assignments) + "\n"
        
        # Add @Autowired to constructor
        code = code[:match.start()] + "@Autowired\n    " + match.group(1) + new_params + match.group(3) + code[match.end():idx] + insertion + code[idx:]
        
    else:
        # No constructor, generate one
        insertion = "\n" + "    @Autowired\n"
        insertion += f"    public {class_name}(" + ", ".join(params) + ") {\n"
        insertion += "\n".join(assignments) + "\n"
        insertion += "    }\n"
        
        # Insert after class {
        class_decl_pattern = re.compile(r'(\b(?:public|protected|private)?\s*(?:abstract\s+)?class\s+' + class_name + r'(?:<[^>]+>)?(?:\s+extends\s+[^{]+)?(?:\s+implements\s+[^{]+)?\{\s*\n)')
        decl_match = class_decl_pattern.search(code)
        if decl_match:
            code = code[:decl_match.end()] + insertion + code[decl_match.end():]
        else:
            # Fallback
            code += insertion

    # Insert fields
    class_decl_pattern = re.compile(r'(\b(?:public|protected|private)?\s*(?:abstract\s+)?class\s+' + class_name + r'(?:<[^>]+>)?(?:\s+extends\s+[^{]+)?(?:\s+implements\s+[^{]+)?\{\s*\n)')
    decl_match = class_decl_pattern.search(code)
    if decl_match:
        code = code[:decl_match.end()] + "\n".join(fields) + "\n\n" + code[decl_match.end():]

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
