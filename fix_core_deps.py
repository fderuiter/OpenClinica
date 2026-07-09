import re

with open('/app/core/pom.xml', 'r') as f:
    content = f.read()

new_deps = """
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
"""
content = content.replace('</dependencies>', new_deps + '\n</dependencies>')

with open('/app/core/pom.xml', 'w') as f:
    f.write(content)
