import os
import re

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
            
        # Add @Autowired to constructor
        # Find public ClassName(DataSource
        class_name = re.search(r'public class ([A-Za-z0-9_]+DAO)', code)
        if class_name:
            cname = class_name.group(1)
            code = re.sub(r'(public ' + cname + r'\s*\([^\)]*DataSource[^\)]*\)\s*\{)', r'@Autowired\n    \1', code)

    # 2. Refactor types
    code = re.sub(r'this\.unsetTypeExpected\(\);', 'java.util.HashMap<Integer, Integer> _types = new java.util.HashMap<Integer, Integer>();', code)
    code = re.sub(r'\bunsetTypeExpected\(\);', 'java.util.HashMap<Integer, Integer> _types = new java.util.HashMap<Integer, Integer>();', code)
    
    code = re.sub(r'this\.setTypeExpected\(', '_types.put(', code)
    code = re.sub(r'\bsetTypeExpected\(', '_types.put(', code)
    
    # Change method signature
    code = re.sub(r'public void (set[A-Za-z0-9_]*TypesExpected\d*)\(\)', r'public java.util.HashMap<Integer, Integer> \1()', code)
    # Change calls
    code = re.sub(r'this\.(set[A-Za-z0-9_]*TypesExpected\d*)\(\);', r'java.util.HashMap<Integer, Integer> _types = this.\1();', code)
    
    # Append return _types to those methods.
    # We find public java.util.HashMap<Integer, Integer> set...
    # and insert return _types; before the closing brace of the method.
    def add_return(m):
        start = m.end()
        # Find the matching closing brace
        brace_count = 1
        i = start
        while i < len(code) and brace_count > 0:
            if code[i] == '{': brace_count += 1
            elif code[i] == '}': brace_count -= 1
            i += 1
        # Insert return _types; before the last brace
        return code[:i-1] + "\n        return _types;\n    " + code[i-1:]

    # Since we can't easily do it with re.sub using python function on the whole string for nested things,
    # let's just find the positions and rebuild the string
    while True:
        m = re.search(r'(public java\.util\.HashMap<Integer, Integer> set[A-Za-z0-9_]*TypesExpected\d*\(\)\s*\{)(?!\s*java\.util\.HashMap<Integer, Integer> _types = new java\.util\.HashMap<Integer, Integer>\(\);\s*// return added)', code)
        if not m: break
        # we mark it so we don't process again
        start = m.end()
        brace_count = 1
        i = start
        while i < len(code) and brace_count > 0:
            if code[i] == '{': brace_count += 1
            elif code[i] == '}': brace_count -= 1
            i += 1
        code = code[:start] + "\n        // return added\n" + code[start:i-1] + "\n        return _types;\n    " + code[i-1:]

    # Now for select() and execute() calls:
    def process_method_calls(text, method_name, append_arg=None):
        idx = 0
        while True:
            # Find next `this.select(` or `select(`
            # We must be careful not to match `.select(` from other objects if we only look for `select(`.
            # For simplicity, look for `this.select(`
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
                # text[start_idx+1 : i-1] is the arguments
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

    code = process_method_calls(code, 'select', '_types')
    code = process_method_calls(code, 'selectByCache', '_types')
    
    # querySuccessful -> boolean success
    # this.execute(...) -> boolean _success = this.execute(...)
    # BUT wait, they might be in an expression like `if (this.execute(...))`? No, they are statements `this.execute(...);`
    # Let's replace `this.execute(` with `_querySuccessful = this.execute(`
    # and add `boolean _querySuccessful = true;` at the beginning of the methods?
    # Or just replace `isQuerySuccessful()` with `_querySuccessful` and define it.
    
    # Actually, easier:
    # boolean _querySuccessful = false;
    # this.execute -> _querySuccessful = this.execute
    # this.executeWithPK -> _querySuccessful = (this.executeWithPK(...) > 0) or we create a wrapper.
    # Let's write the execution status change manually in Python:
    
    code = re.sub(r'(this\.execute\()', r'_querySuccessful = \1', code)
    code = re.sub(r'(this\.executeWithPK\()', r'_latestPK = \1', code)
    code = re.sub(r'isQuerySuccessful\(\)', r'_querySuccessful', code)
    code = re.sub(r'getLatestPK\(\)', r'_latestPK', code)
    
    # Need to inject `boolean _querySuccessful = true; int _latestPK = 0;` into methods that use them.
    # A simple hack: just inject it as a class field? NO! We want to remove request-specific instance variables.
    # So we MUST inject them as local variables.
    # We can inject them at the start of every public/protected method.
    def inject_locals(text):
        res = []
        in_class = False
        brace_level = 0
        method_brace_level = -1
        for line in text.split('\n'):
            if '{' in line:
                brace_level += line.count('{')
            if '}' in line:
                brace_level -= line.count('}')
            
            res.append(line)
            
            # Very hacky method detection
            if brace_level == 2 and '{' in line and ('public ' in line or 'protected ' in line or 'private ' in line) and '(' in line and ')' in line and '=' not in line:
                if '_querySuccessful' in text or '_latestPK' in text:
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

