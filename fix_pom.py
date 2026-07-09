with open("/app/web/pom.xml", 'r') as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if i >= 329 and i <= 360:
        continue
    new_lines.append(line)

with open("/app/web/pom.xml", 'w') as f:
    f.writelines(new_lines)

