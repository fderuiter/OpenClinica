import os
import re

for root, dirs, files in os.walk('/app/core/src/main/java'):
    for f in files:
        if f.endswith('.java'):
            path = os.path.join(root, f)
            with open(path, 'r') as file:
                code = file.read()
            
            orig_code = code
            
            # Fix duplicate @Autowired
            while re.search(r'@Autowired\s*\n\s*@Autowired', code):
                code = re.sub(r'@Autowired\s*\n\s*@Autowired', '@Autowired', code)
            
            # Remove duplicated arguments in new Xxx(..., DAO, DAO, DAO, DAO)
            # Actually, fixing this is harder with regex, let's just do it manually if there are only a few.
            
            if code != orig_code:
                with open(path, 'w') as file:
                    file.write(code)

print("Cleanup complete.")
