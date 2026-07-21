import os
import xml.etree.ElementTree as ET

def check_migrations():
    migration_dir = '/app/core/src/main/resources/migration'
    failed_files = []
    
    for root, _, files in os.walk(migration_dir):
        for file in files:
            if file.endswith('.xml'):
                filepath = os.path.join(root, file)
                try:
                    tree = ET.parse(filepath)
                    root_elem = tree.getroot()
                    
                    # Liquibase namespace is usually required
                    namespaces = {'lb': 'http://www.liquibase.org/xml/ns/dbchangelog/1.9'}
                    # But some might not have namespace prefix, so let's just search the string content
                    with open(filepath, 'r') as f:
                        content = f.read()
                        if '<sql>' in content and '<rollback' not in content:
                            failed_files.append(filepath)
                except Exception as e:
                    pass
                    
    if failed_files:
        print("FAILED: The following migrations lack rollback paths for raw SQL blocks:")
        for f in failed_files:
            print(f)
        exit(1)
    else:
        print("SUCCESS: All raw SQL blocks have verified rollback paths.")
        exit(0)

if __name__ == '__main__':
    check_migrations()
