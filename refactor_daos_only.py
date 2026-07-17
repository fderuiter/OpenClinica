import os
import re

def refactor_daos():
    modified_count = 0
    for root, dirs, files in os.walk('/app/core/src/main/java/org/akaza/openclinica/dao'):
        for file in files:
            if file.endswith('DAO.java') or file.endswith('Dao.java'):
                path = os.path.join(root, file)
                with open(path, 'r') as f:
                    code = f.read()

                orig_code = code

                if 'extends EntityDAO' in code or 'extends AuditableEntityDAO' in code:
                    if 'import org.springframework.stereotype.Repository;' not in code:
                        code = re.sub(r'(import .*;\n)', r'\1import org.springframework.stereotype.Repository;\nimport org.springframework.beans.factory.annotation.Autowired;\n', code, count=1)
                    
                    if '@Repository' not in code:
                        code = re.sub(r'(public\s+class\s+[A-Za-z0-9_]+DAO)', r'@Repository\n\1', code, count=1)
                        
                    class_name = re.search(r'public\s+class\s+([A-Za-z0-9_]+DAO)', code)
                    if class_name:
                        cname = class_name.group(1)
                        if '@Autowired' not in code:
                            code = re.sub(r'(public ' + cname + r'\s*\([^\)]*DataSource[^\)]*\)\s*\{)', r'@Autowired\n    \1', code)

                if code != orig_code:
                    with open(path, 'w') as f:
                        f.write(code)
                    modified_count += 1
    print("Modified", modified_count, "DAOs")

if __name__ == '__main__':
    refactor_daos()
