with open("web/pom.xml", "r") as f:
    c = f.read()

c = c.replace(r"\nn", "")
c = c.replace(r"\n", "\n")
c = c.replace(r"\t", "\t")

with open("web/pom.xml", "w") as f:
    f.write(c)
