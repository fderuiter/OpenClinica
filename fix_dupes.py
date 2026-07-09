def fix_dupes(file):
    with open(file, 'r') as f:
        content = f.read()
    
    # Simple deduplication strategy: since we are not sure where the dupes are exactly,
    # we'll read lines and build the file ignoring the second occurrence.
    # But it's easier to just rebuild it if we can find it.
    pass

import sys
file = 'core/pom.xml'
with open(file, 'r') as f:
    lines = f.readlines()
with open(file, 'w') as f:
    seen_annotation = False
    seen_mail = False
    for line in lines:
        if '<artifactId>jakarta.annotation-api</artifactId>' in line:
            if seen_annotation:
                continue
            seen_annotation = True
        if '<artifactId>spring-boot-starter-mail</artifactId>' in line:
            if seen_mail:
                continue
            seen_mail = True
        f.write(line)
