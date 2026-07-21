import os
import sys
import xml.etree.ElementTree as ET

def get_local_name(tag):
    if tag.startswith('{'):
        return tag.split('}', 1)[1]
    return tag

def check_migrations():
    migration_dir = '/app/core/src/main/resources/migration'
    failed_changesets = []
    
    for root, _, files in os.walk(migration_dir):
        for file in files:
            if file.endswith('.xml'):
                filepath = os.path.join(root, file)
                try:
                    tree = ET.parse(filepath)
                except Exception as e:
                    print(f"FAILED: XML parsing error in {filepath}")
                    print(f"Details: {e}")
                    sys.exit(1)
                
                root_elem = tree.getroot()
                
                # Find all changeSets
                for child in root_elem:
                    if get_local_name(child.tag) == 'changeSet':
                        has_sql = False
                        has_rollback = False
                        
                        # Check descendants of this changeSet
                        for descendant in child.iter():
                            local_name = get_local_name(descendant.tag)
                            if local_name == 'sql':
                                has_sql = True
                            elif local_name == 'rollback':
                                has_rollback = True
                                
                        if has_sql and not has_rollback:
                            changeset_id = child.attrib.get('id', 'unknown')
                            failed_changesets.append(f"{filepath} (changeset: {changeset_id})")
                    
    if failed_changesets:
        print("FAILED: The following migrations lack rollback paths for raw SQL blocks:")
        for f in failed_changesets:
            print(f)
        sys.exit(1)
    else:
        print("SUCCESS: All raw SQL blocks have verified rollback paths.")
        sys.exit(0)

if __name__ == '__main__':
    check_migrations()
