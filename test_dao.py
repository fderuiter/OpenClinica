import re
path = '/app/core/src/main/java/org/akaza/openclinica/dao/managestudy/StudyDAO.java'
code = open(path).read()
orig_code = code

if 'extends EntityDAO' in code or 'extends AuditableEntityDAO' in code:
    print("Found extends!")
    code = re.sub(r'^package ', 'package ', code) 
    code = re.sub(r'(import .*;\n)', r'\1import org.springframework.stereotype.Repository;\nimport org.springframework.beans.factory.annotation.Autowired;\n', code, count=1)
    
    if '@Repository' not in code:
        code = re.sub(r'(public class [A-Za-z0-9_]+DAO)', r'@Repository\n\1', code)
        
    class_name = re.search(r'public class ([A-Za-z0-9_]+DAO)', code)
    if class_name:
        print("Found class name: " + class_name.group(1))
        cname = class_name.group(1)
        if '@Autowired' not in code:
            code = re.sub(r'(public ' + cname + r'\s*\([^\)]*DataSource[^\)]*\)\s*\{)', r'@Autowired\n    \1', code)

print("Modified:", code != orig_code)
if code != orig_code:
    open(path, 'w').write(code)
