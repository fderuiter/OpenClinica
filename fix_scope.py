import re
def fix(file):
    with open(file, 'r') as f:
        c = f.read()
    c = re.sub(r'<scope>provided</scope>\s*<scope>provided</scope>', r'<scope>provided</scope>', c)
    with open(file, 'w') as f:
        f.write(c)

fix('core/pom.xml')
fix('web/pom.xml')
fix('ws/pom.xml')
