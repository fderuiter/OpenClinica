import os
import re

def fix_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Replace <version> for jakarta dependencies
    # Wait, it's easier to just do regex.
    # We want to map old javax artifact ids to new jakarta artifact ids and remove versions.
    
    # 1. jakarta.transaction:transaction-api -> jakarta.transaction:jakarta.transaction-api
    content = re.sub(r'<groupId>jakarta.transaction</groupId>\s*<artifactId>transaction-api</artifactId>\s*<version>.*?</version>', 
                     r'<groupId>jakarta.transaction</groupId><artifactId>jakarta.transaction-api</artifactId>', content)
                     
    # 2. jakarta.servlet:servlet-api -> jakarta.servlet:jakarta.servlet-api
    content = re.sub(r'<groupId>jakarta.servlet</groupId>\s*<artifactId>servlet-api</artifactId>\s*<version>.*?</version>',
                     r'<groupId>jakarta.servlet</groupId><artifactId>jakarta.servlet-api</artifactId><scope>provided</scope>', content)
                     
    # 3. jakarta.servlet.jsp:jsp-api -> jakarta.servlet.jsp:jakarta.servlet.jsp-api
    content = re.sub(r'<groupId>jakarta.servlet.jsp</groupId>\s*<artifactId>jsp-api</artifactId>\s*<version>.*?</version>',
                     r'<groupId>jakarta.servlet.jsp</groupId><artifactId>jakarta.servlet.jsp-api</artifactId><scope>provided</scope>', content)
                     
    # 4. jakarta.mail:mail -> org.eclipse.angus:jakarta.mail or com.sun.mail:jakarta.mail
    # Wait, in Spring Boot 3, it's provided by spring-boot-starter-mail. We can just use that.
    content = re.sub(r'<groupId>jakarta.mail</groupId>\s*<artifactId>mail</artifactId>\s*<version>.*?</version>',
                     r'<groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-mail</artifactId>', content)

    # 5. jakarta.validation:validation-api -> jakarta.validation:jakarta.validation-api
    content = re.sub(r'<groupId>jakarta.validation</groupId>\s*<artifactId>validation-api</artifactId>\s*<version>.*?</version>',
                     r'<groupId>jakarta.validation</groupId><artifactId>jakarta.validation-api</artifactId>', content)

    # 6. jakarta.annotation:jakarta.annotation-api (remove version)
    content = re.sub(r'<groupId>jakarta.annotation</groupId>\s*<artifactId>jakarta.annotation-api</artifactId>\s*<version>.*?</version>',
                     r'<groupId>jakarta.annotation</groupId><artifactId>jakarta.annotation-api</artifactId>', content)

    # 7. jakarta.servlet:jstl -> jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api
    content = re.sub(r'<groupId>jakarta.servlet</groupId>\s*<artifactId>jstl</artifactId>\s*<version>.*?</version>',
                     r'<groupId>jakarta.servlet.jsp.jstl</groupId><artifactId>jakarta.servlet.jsp.jstl-api</artifactId>', content)

    # 8. jakarta.activation:activation -> jakarta.activation:jakarta.activation-api
    content = re.sub(r'<groupId>jakarta.activation</groupId>\s*<artifactId>activation</artifactId>\s*<version>.*?</version>',
                     r'<groupId>jakarta.activation</groupId><artifactId>jakarta.activation-api</artifactId>', content)

    with open(filepath, 'w') as f:
        f.write(content)

fix_file('core/pom.xml')
fix_file('web/pom.xml')
fix_file('ws/pom.xml')

