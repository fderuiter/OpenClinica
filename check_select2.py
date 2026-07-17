import os, re
count = 0
for root, dirs, files in os.walk('/app/core/src/main/java/org/akaza/openclinica/dao'):
    for file in files:
        if file.endswith('DAO.java') or file.endswith('Dao.java'):
            with open(os.path.join(root, file)) as f:
                content = f.read()
                if 'extends EntityDAO' in content or 'extends AuditableEntityDAO' in content:
                    matches = re.findall(r'\bselect\(', content)
                    if matches:
                        count += len(matches)
print(f"Total select() calls: {count}")
