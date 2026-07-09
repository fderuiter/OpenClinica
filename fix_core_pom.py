import re

with open('/app/core/pom.xml', 'r') as f:
    content = f.read()

# Remove hibernate-core
content = re.sub(r'<dependency>\s*<groupId>org\.hibernate</groupId>\s*<artifactId>hibernate-core</artifactId>\s*<version>.*?</version>\s*</dependency>', '', content, flags=re.DOTALL)

# Remove hibernate-annotations
content = re.sub(r'<dependency>\s*<groupId>org\.hibernate</groupId>\s*<artifactId>hibernate-annotations</artifactId>\s*<version>.*?</version>\s*</dependency>', '', content, flags=re.DOTALL)

# Remove hibernate-validator
content = re.sub(r'<dependency>\s*<groupId>org\.hibernate</groupId>\s*<artifactId>hibernate-validator</artifactId>\s*<version>.*?</version>\s*</dependency>', '', content, flags=re.DOTALL)

# Update jaxb
content = content.replace('jaxb-api', 'jakarta.xml.bind-api')
content = content.replace('<version>2.3.1</version>', '')

# Add spring-boot-starter-data-jpa
new_deps = """
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
"""
content = content.replace('</dependencies>', new_deps + '\n</dependencies>')

with open('/app/core/pom.xml', 'w') as f:
    f.write(content)
