import os
import re

def process_file(file):
    with open(file, 'r') as f:
        c = f.read()
    
    # context.getJobDetail().getFullName() -> context.getJobDetail().getKey().toString()
    c = c.replace(".getJobDetail().getFullName()", ".getJobDetail().getKey().toString()")
    c = c.replace(".getJobDetail().getName()", ".getJobDetail().getKey().getName()")
    c = c.replace(".getJobDetail().getGroup()", ".getJobDetail().getKey().getGroup()")
    
    c = c.replace(".getTrigger().getFullName()", ".getTrigger().getKey().toString()")
    c = c.replace(".getTrigger().getName()", ".getTrigger().getKey().getName()")
    c = c.replace(".getTrigger().getGroup()", ".getTrigger().getKey().getGroup()")

    with open(file, 'w') as f:
        f.write(c)

for root, dirs, files in os.walk('core/src/main/java'):
    for file in files:
        if file.endswith('.java'):
            process_file(os.path.join(root, file))

for root, dirs, files in os.walk('web/src/main/java'):
    for file in files:
        if file.endswith('.java'):
            process_file(os.path.join(root, file))
