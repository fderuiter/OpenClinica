with open("core/pom.xml", "r") as f:
    lines = f.readlines()
with open("core/pom.xml", "w") as f:
    for i, line in enumerate(lines):
        if line.strip() == "<dependency>" and lines[i+1].strip() == "</dependency>":
            continue
        if line.strip() == "</dependency>" and i > 0 and lines[i-1].strip() == "<dependency>":
            continue
        if line.strip() == "<groupId>org.springframework.boot</groupId>" and lines[i+1].strip() == "</dependency>":
            continue
        if line.strip() == "</dependency>" and i > 0 and lines[i-1].strip() == "<groupId>org.springframework.boot</groupId>":
            continue
        if line.strip() == "<dependency>" and lines[i+1].strip() == "<groupId>org.springframework.boot</groupId>" and lines[i+2].strip() == "</dependency>":
            continue
        f.write(line)
