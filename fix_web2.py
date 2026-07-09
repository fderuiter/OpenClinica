import re
with open("web/pom.xml", "r") as f:
    c = f.read()

# I will find the first <dependencies> and just put my dependencies there manually.
# First, remove all `\t\t<dependency>\n\t\t\t<groupId>org.springframework</groupId>\n\t\t\t<artifactId>spring-context-support</artifactId>\n\t\t</dependency>\n\t\t<dependency>\n\t\t\t<groupId>jakarta.ws.rs</groupId>\n\t\t\t<artifactId>jakarta.ws.rs-api</artifactId>\n\t\t</dependency>`
bad = """		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-context-support</artifactId>
		</dependency>
		<dependency>
			<groupId>jakarta.ws.rs</groupId>
			<artifactId>jakarta.ws.rs-api</artifactId>
		</dependency>"""

c = c.replace(bad, "")
c = c.replace(bad.strip(), "")
c = c.replace("""		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-context-support</artifactId>
		</dependency>
		<dependency>
			<groupId>jakarta.ws.rs</groupId>
			<artifactId>jakarta.ws.rs-api</artifactId>
		</dependency>""", "")

# Then insert them ONCE at the top
deps = """
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-context-support</artifactId>
		</dependency>
		<dependency>
			<groupId>jakarta.ws.rs</groupId>
			<artifactId>jakarta.ws.rs-api</artifactId>
		</dependency>
"""
# insert after first <dependencies>
c = c.replace("<dependencies>", "<dependencies>" + deps, 1)

with open("web/pom.xml", "w") as f:
    f.write(c)

