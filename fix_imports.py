import os

for root, dirs, files in os.walk('/app/core/src/main/java/org/akaza/openclinica/dao/hibernate'):
    for file in files:
        if file.endswith('.java'):
            filepath = os.path.join(root, file)
            with open(filepath, 'r') as f:
                content = f.read()
            if 'import jakarta.persistence.EntityManager;' not in content:
                content = content.replace('package org.akaza.openclinica.dao.hibernate;', 'package org.akaza.openclinica.dao.hibernate;\n\nimport jakarta.persistence.EntityManager;\nimport jakarta.persistence.Query;\n')
                with open(filepath, 'w') as f:
                    f.write(content)
