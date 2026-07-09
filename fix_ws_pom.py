import re

with open('/app/ws/pom.xml', 'r') as f:
    content = f.read()

content = content.replace('spring-ws-core-tiger', 'spring-ws-core')
content = re.sub(r'<dependency>\s*<groupId>org\.springframework\.ws</groupId>\s*<artifactId>spring-ws-core</artifactId>\s*<version>.*?</version>\s*</dependency>', r'<dependency>\n\t\t\t<groupId>org.springframework.ws</groupId>\n\t\t\t<artifactId>spring-ws-core</artifactId>\n\t\t</dependency>', content, flags=re.DOTALL)

content = re.sub(r'<dependency>\s*<groupId>org\.springframework\.ws</groupId>\s*<artifactId>spring-ws-security</artifactId>\s*<version>.*?</version>\s*</dependency>', r'<dependency>\n\t\t\t<groupId>org.springframework.ws</groupId>\n\t\t\t<artifactId>spring-ws-security</artifactId>\n\t\t</dependency>', content, flags=re.DOTALL)

content = content.replace('javax.xml.soap', 'jakarta.xml.soap')
content = content.replace('saaj-api', 'jakarta.xml.soap-api')
content = re.sub(r'<version>1\.3</version>', '', content)

content = content.replace('jaxb-api', 'jakarta.xml.bind-api')
content = re.sub(r'<version>2\.1</version>', '', content)

with open('/app/ws/pom.xml', 'w') as f:
    f.write(content)
