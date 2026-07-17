import os, re
for root, dirs, files in os.walk('/app/core/src/main/java/org/akaza/openclinica/dao'):
    for file in files:
        if file.endswith('DAO.java') or file.endswith('Dao.java'):
            with open(os.path.join(root, file)) as f:
                content = f.read()
                if 'this.select(' in content and 'extends EntityDAO' in content:
                    print(file)
