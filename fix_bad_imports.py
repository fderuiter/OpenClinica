import os
import re

for root, dirs, files in os.walk('/app/core/src/main/java'):
    for f in files:
        if f.endswith('.java'):
            path = os.path.join(root, f)
            with open(path, 'r') as file:
                code = file.read()
            
            orig_code = code
            code = code.replace("import ItemDAO;", "import org.akaza.openclinica.dao.submit.ItemDAO;")
            code = code.replace("import CRFDAO;", "import org.akaza.openclinica.dao.admin.CRFDAO;")
            code = code.replace("import CRFVersionDAO;", "import org.akaza.openclinica.dao.submit.CRFVersionDAO;")
            
            if code != orig_code:
                with open(path, 'w') as file:
                    file.write(code)

print("Fixed bad imports")
