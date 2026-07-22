import os
import sys
import json
import xml.etree.ElementTree as ET

def is_skipped_file(file_path):
    if not os.path.exists(file_path):
        return False
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            first_line = f.readline().strip()
            if first_line != '---':
                return False
            fm_lines = []
            for line in f:
                if line.strip() == '---':
                    break
                fm_lines.append(line)
            
            for line in fm_lines:
                if ':' in line:
                    key, val = line.split(':', 1)
                    if key.strip().lower() == 'visibility':
                        clean_val = val.strip().strip("'\"").lower()
                        if clean_val in ('restricted', 'draft'):
                            return True
    except Exception:
        pass
    return False

def find_markdown_files(directory):
    md_files = []
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith('.md'):
                md_files.append(os.path.join(root, file))
    return md_files

def validate_snippets(file_path):
    errors = []
    with open(file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    in_block = False
    block_type = None
    block_content = []
    start_line = 0
    
    for i, line in enumerate(lines):
        stripped = line.strip()
        if stripped.startswith('```'):
            if in_block:
                # End of block
                content = ''.join(block_content).strip()
                if content:
                    if block_type == 'json':
                        try:
                            json.loads(content)
                        except json.JSONDecodeError as e:
                            errors.append(f"{file_path}:{start_line} - JSON format error: {e}")
                    elif block_type == 'xml':
                        try:
                            # XML needs a single root element, if multiple are present fromstring might fail
                            # but usually examples have a single root
                            ET.fromstring(content)
                        except ET.ParseError as e:
                            errors.append(f"{file_path}:{start_line} - XML format error: {e}")
                
                in_block = False
                block_type = None
                block_content = []
            else:
                lang = stripped[3:].strip().lower()
                if lang in ('json', 'xml'):
                    in_block = True
                    block_type = lang
                    start_line = i + 1  # 1-indexed line number
        elif in_block:
            block_content.append(line)
            
    return errors

def main():
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    docs_dir = os.path.join(base_dir, 'docs')
    
    md_files = find_markdown_files(docs_dir)
    all_errors = []
    
    for md_file in md_files:
        if is_skipped_file(md_file):
            continue
        errors = validate_snippets(md_file)
        all_errors.extend(errors)
        
    if all_errors:
        print("Snippet validation failed:")
        for error in all_errors:
            print(error)
        sys.exit(1)
    else:
        print("All JSON and XML snippets are valid.")
        sys.exit(0)

if __name__ == "__main__":
    main()
