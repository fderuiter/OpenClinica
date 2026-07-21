import sys
import re
import os

def validate_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Strip HTML comments
    content = re.sub(r'<!--.*?-->', '', content, flags=re.DOTALL)
    
    # 2. Strip code blocks
    content = re.sub(r'```.*?```', '', content, flags=re.DOTALL)
    content = re.sub(r'~~~.*?~~~', '', content, flags=re.DOTALL)
    
    # 3. Strip inline code
    content = re.sub(r'``.*?``', '', content, flags=re.DOTALL)
    content = re.sub(r'`[^`]*`', '', content)
    
    # 4. Strip links/images syntax
    content = re.sub(r'!\[[^\]]*\]\([^)]+\)', '', content)
    content = re.sub(r'\[([^\]]+)\]\([^)]+\)', r'\1', content)
    content = re.sub(r'\[([^\]]+)\]\[[^\]]*\]', r'\1', content)
    
    # 5. Remove header and formatting markers
    content = re.sub(r'^#+\s+', '', content, flags=re.MULTILINE)
    # Remove bold/italics markers
    content = content.replace('**', '').replace('__', '')
    content = content.replace('*', '').replace('_', '')
    
    # 6. Check for task/tasks
    matches = re.findall(r'\btasks?\b', content, re.IGNORECASE)
    
    return len(matches) > 0

def main():
    if len(sys.argv) < 2:
        print("Usage: python3 validate_prose.py <files...>")
        sys.exit(1)
        
    files = sys.argv[1:]
    
    # Expand globs if not already done by shell
    import glob
    expanded_files = []
    for f in files:
        if '*' in f or '?' in f:
            expanded_files.extend(glob.glob(f))
        else:
            expanded_files.append(f)
            
    violations = []
    for filepath in expanded_files:
        if not os.path.exists(filepath):
            continue
        if validate_file(filepath):
            violations.append(filepath)
            
    if violations:
        print("Error: The following tutorial files violate Diátaxis structural standards by including task-oriented language in prose:")
        for v in violations:
            print(v)
        sys.exit(1)

if __name__ == "__main__":
    main()
