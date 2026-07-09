import re

with open("pom.xml", "r") as f:
    c = f.read()

# I will just add the quartz dependencies to the root pom dependencyManagement if they are missing or add version.
# Actually I deleted the version. Let me restore it.
c = c.replace("<artifactId>quartz</artifactId>\n\t\t\t</dependency>", "<artifactId>quartz</artifactId>\n\t\t\t\t<version>2.3.2</version>\n\t\t\t</dependency>")
c = c.replace("<artifactId>quartz-oracle</artifactId>\n\t\t\t</dependency>", "<artifactId>quartz-oracle</artifactId>\n\t\t\t\t<version>2.1.7</version>\n\t\t\t</dependency>")

with open("pom.xml", "w") as f:
    f.write(c)

with open("web/pom.xml", "r") as f:
    c2 = f.read()

c2 = c2.replace("<version>3.1.0</version><version>3.1.0</version>", "<version>3.1.0</version>")

with open("web/pom.xml", "w") as f:
    f.write(c2)

