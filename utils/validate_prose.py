import sys
import re
import os

def validate_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    valid_lines = []
    for line in lines:
        if re.search(r'<!--\s*(?:prose-)?ignore\s*-->', line, re.IGNORECASE):
            continue
        valid_lines.append(line)

    content = "".join(valid_lines)

    # 1. Strip reference-style link definitions
    content = re.sub(r'^\s*\[[^\]]+\]:\s*.*$', '', content, flags=re.MULTILINE)

    # 2. Strip HTML comments (multiline)
    content = re.sub(r'<!--.*?-->', '', content, flags=re.DOTALL)
    
    # 3. Strip code blocks
    content = re.sub(r'```.*?```', '', content, flags=re.DOTALL)
    content = re.sub(r'~~~.*?~~~', '', content, flags=re.DOTALL)
    
    # 4. Strip inline code
    content = re.sub(r'``.*?``', '', content, flags=re.DOTALL)
    content = re.sub(r'`[^`]*`', '', content)
    
    # 5. Strip links/images syntax
    # Strip images (don't keep text)
    content = re.sub(r'!\[[^\]]*\]\([^()]*(?:\([^()]*\)[^()]*)*\)', '', content)
    # Strip markdown links but keep text
    content = re.sub(r'\[([^\]]+)\]\([^()]*(?:\([^()]*\)[^()]*)*\)', r'\1', content)
    # Strip reference style links but keep text
    content = re.sub(r'\[([^\]]+)\]\[[^\]]*\]', r'\1', content)
    
    # 6. Strip auto-links and raw URLs
    content = re.sub(r'<https?://[^>]+>', '', content)
    content = re.sub(r'\bhttps?://\S+', '', content)
    
    # 7. Remove header and formatting markers
    content = re.sub(r'^#+\s+', '', content, flags=re.MULTILINE)
    # Remove bold/italics markers
    content = content.replace('**', '').replace('__', '')
    content = content.replace('*', '').replace('_', '')
    
    # 8. Check for task/tasks
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
