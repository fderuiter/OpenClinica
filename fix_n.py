for file in ["core/pom.xml", "web/pom.xml", "ws/pom.xml"]:
    with open(file, 'r') as f:
        c = f.read()
    c = c.replace("n\t\t<dependency>", "\t\t<dependency>")
    with open(file, 'w') as f:
        f.write(c)
