import os
import re

for root, dirs, files in os.walk('/app/core/src/main/java/org/akaza/openclinica/dao'):
    for file in files:
        if file.endswith('DAO.java') or file.endswith('Dao.java'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                code = f.read()
            orig_code = code

            # Remove ALL setTypeExpected calls
            code = re.sub(r'^[ \t]*(?:this\.)?(?:un)?setTypeExpected.*?\r?\n', '', code, flags=re.MULTILINE)
            code = re.sub(r'^[ \t]*(?:this\.)?set[A-Za-z0-9_]*TypesExpected.*?\r?\n', '', code, flags=re.MULTILINE)
            code = re.sub(r'^[ \t]*//[ \t]*(?:this\.)?set[A-Za-z0-9_]*TypesExpected.*?\r?\n', '', code, flags=re.MULTILINE)

            # Remove floating @Override followed by nothing or the end of the class
            # Actually, it's safer to remove @Override if it's right before where the method used to be, but the method is gone.
            # Instead of regex for @Override, we can delete the @Override at the same time as we delete the method!
            
            while True:
                m = re.search(r'(?:^[ \t]*@Override\s*\n)?[ \t]*(?:public|private|protected)\s+(?:abstract\s+)?void\s+set[A-Za-z0-9_]*TypesExpected[A-Za-z0-9_]*\s*\([^)]*\)\s*(?:\{|;)', code, flags=re.MULTILINE)
                if not m:
                    break
                start = m.start()
                if code[m.end()-1] == ';':
                    code = code[:start] + code[m.end():]
                else:
                    brace_count = 1
                    i = m.end()
                    while i < len(code) and brace_count > 0:
                        if code[i] == '{': brace_count += 1
                        elif code[i] == '}': brace_count -= 1
                        i += 1
                    code = code[:start] + code[i:]

            if code != orig_code:
                with open(path, 'w') as f:
                    f.write(code)

