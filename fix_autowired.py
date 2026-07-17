import os
import re

for root, dirs, files in os.walk('/app'):
    for f in files:
        if f.endswith('.java'):
            path = os.path.join(root, f)
            with open(path, 'r') as file:
                code = file.read()
            
            orig_code = code
            code = re.sub(r'@Autowired\s*\n\s*@Autowired', '@Autowired', code)
            
            if code != orig_code:
                with open(path, 'w') as file:
                    file.write(code)

print("Autowired fixed")
