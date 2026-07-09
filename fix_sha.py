import re
file = "web/src/main/java/org/akaza/openclinica/web/pform/PFormCache.java"
with open(file, "r") as f:
    c = f.read()

c = c.replace("import org.springframework.security.authentication.encoding.ShaPasswordEncoder;", "import org.apache.commons.codec.digest.DigestUtils;")
c = re.sub(r'ShaPasswordEncoder\s+encoder\s*=\s*new\s+ShaPasswordEncoder\(256\);\s*String\s+(\w+)\s*=\s*encoder\.encodePassword\(([^,]+)\s*,\s*null\);',
           r'String \1 = DigestUtils.sha256Hex(\2);', c)

with open(file, "w") as f:
    f.write(c)

