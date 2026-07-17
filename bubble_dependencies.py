import os
import re
import subprocess
from fix_super_and_new import fix_call

def run_maven():
    print("Running maven...")
    result = subprocess.run(['mvn', 'clean', 'test-compile', '-pl', 'core', '-am', '-DskipTests', '-T', '1C'], capture_output=True, text=True)
    return result.stdout, result.returncode

def parse_errors(output):
    errors = []
    lines = output.split('\n')
    i = 0
    while i < len(lines):
        line = lines[i]
        if '[ERROR]' in line and '.java:[' in line:
            m = re.search(r'\[ERROR\] (.*?\.java):\[(\d+),\d+\] (.*)', line)
            if m:
                file_path = m.group(1)
                line_num = int(m.group(2))
                err_msg = m.group(3)
                
                details = []
                j = i + 1
                while j < len(lines) and '[ERROR] /app/' not in lines[j] and 'Failed to execute' not in lines[j] and '[INFO]' not in lines[j] and '-> [Help 1]' not in lines[j]:
                    details.append(lines[j].strip())
                    j += 1
                
                errors.append({
                    'file': file_path,
                    'line': line_num,
                    'msg': err_msg,
                    'details': '\n'.join(details)
                })
        i += 1
    return errors

def inject_dependencies(file_path, req_types):
    with open(file_path, 'r') as f:
        code = f.read()

    orig_code = code
    dao_types = [t.split('.')[-1] for t in req_types if t.endswith('DAO')]
    if not dao_types:
        return False
        
    class_match = re.search(r'\b(?:public|protected|private)?\s*(?:abstract\s+)?class\s+([A-Z]\w+)', code)
    if not class_match:
        return False
    class_name = class_match.group(1)

    fields = []
    params = []
    assignments = []
    
    for dt in dao_types:
        var_name = '_' + dt[0].lower() + dt[1:len(dt)-3] + 'DAO'
        if f'{dt} {var_name}' not in code:
            fields.append(f"    private {dt} {var_name};")
            params.append(f"{dt} {var_name}")
            assignments.append(f"        this.{var_name} = {var_name};")

    if not fields and not params:
        return False

    if 'import org.springframework.beans.factory.annotation.Autowired;' not in code:
        code = re.sub(r'(package\s+[^;]+;\n+)', r'\1import org.springframework.beans.factory.annotation.Autowired;\nimport org.springframework.stereotype.Component;\n', code, count=1)

    for req in req_types:
        if req.endswith('DAO') and req not in code:
            code = re.sub(r'(package\s+[^;]+;\n+)', r'\1import ' + req + ';\n', code, count=1)

    if '@Component' not in code and '@Service' not in code and '@Controller' not in code and '@RestController' not in code and '@Repository' not in code:
        code = re.sub(r'(public\s+(?:abstract\s+)?class\s+[A-Z])', r'@Component\n\1', code, count=1)

    constructor_pattern = r'(public\s+' + class_name + r'\s*\()([^)]*)(\)\s*(?:throws\s+[^{]+)?\{)'
    match = re.search(constructor_pattern, code)
    
    class_decl_match = re.search(r'\b(?:public|protected|private)?\s*(?:abstract\s+)?class\s+' + class_name + r'[^\{]*\{', code)
    
    if match:
        existing_params = match.group(2).strip()
        if existing_params:
            new_params = existing_params + ", " + ", ".join(params)
        else:
            new_params = ", ".join(params)
            
        idx = match.end()
        insertion = "\n" + "\n".join(assignments) + "\n"
        
        # If there's a super call, insert AFTER the super call
        super_match = re.match(r'\s*super\s*\([^)]*\)\s*;', code[idx:])
        if super_match:
            idx += super_match.end()
            
        code = code[:match.start()] + "@Autowired\n    " + match.group(1) + new_params + match.group(3) + code[match.end():idx] + insertion + code[idx:]
        
        if fields:
            code = code[:class_decl_match.end()] + "\n" + "\n".join(fields) + "\n" + code[class_decl_match.end():]
        
    else:
        insertion = "\n" + "\n".join(fields) + "\n\n"
        insertion += "    @Autowired\n"
        insertion += f"    public {class_name}(" + ", ".join(params) + ") {\n"
        insertion += "\n".join(assignments) + "\n"
        insertion += "    }\n"
        
        code = code[:class_decl_match.end()] + insertion + code[class_decl_match.end():]

    with open(file_path, 'w') as f:
        f.write(code)
    return True

def analyze_and_fix(errors):
    fixed_count = 0
    for e in errors:
        if 'constructor' in e['msg'] or 'no suitable constructor' in e['msg']:
            details = e['details']
            
            req_match = re.search(r'required:\s*(.*)', details)
            if not req_match:
                req_match = re.search(r'constructor .*?\((.*?)\) is not applicable', details)
            
            if req_match:
                req_types = [t.strip() for t in req_match.group(1).split(',')]
                
                # Check if it's a super call error
                is_super = False
                with open(e['file'], 'r') as f:
                    lines = f.readlines()
                    if e['line'] - 1 < len(lines):
                        if 'super' in lines[e['line'] - 1]:
                            is_super = True
                        elif e['line'] - 2 >= 0 and 'super' in lines[e['line'] - 2]:
                            is_super = True
                            
                injected = inject_dependencies(e['file'], req_types)
                fixed = fix_call(e['file'], e['line'], req_types, is_super)
                if injected or fixed:
                    fixed_count += 1
    return fixed_count

if __name__ == '__main__':
    for _ in range(5):
        out, code = run_maven()
        if code == 0:
            print("Build success!")
            break
        errors = parse_errors(out)
        fixes = analyze_and_fix(errors)
        print(f"Fixed {fixes} errors")
        if fixes == 0:
            print("Could not automatically fix any more errors.")
            break
