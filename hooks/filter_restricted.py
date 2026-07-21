import os
import yaml
from mkdocs.exceptions import BuildError

def get_front_matter(src_path):
    if not os.path.exists(src_path):
        return {}
    try:
        with open(src_path, 'r', encoding='utf-8') as f:
            first_line = f.readline().strip()
            if first_line != '---':
                return {}
            fm_lines = []
            for line in f:
                if line.strip() == '---':
                    break
                fm_lines.append(line)
            return yaml.safe_load(''.join(fm_lines)) or {}
    except Exception:
        return {}

def check_nav_for_restricted(nav_item, restricted_paths):
    if isinstance(nav_item, list):
        for item in nav_item:
            check_nav_for_restricted(item, restricted_paths)
    elif isinstance(nav_item, dict):
        for key, value in nav_item.items():
            check_nav_for_restricted(value, restricted_paths)
    elif isinstance(nav_item, str):
        # nav_item is a path, like 'explanation/project-info.md'
        # Convert path separators to standard slashes for comparison
        nav_path = nav_item.replace('\\', '/')
        if nav_path in restricted_paths:
            raise BuildError(f"Restricted page explicitly linked in public navigation: {nav_item}")

def on_files(files, config):
    restricted_files = []
    restricted_paths = set()
    
    # Identify restricted files
    for file in files.documentation_pages():
        fm = get_front_matter(file.abs_src_path)
        if fm.get('visibility') == 'restricted':
            restricted_files.append(file)
            restricted_paths.add(file.src_path.replace('\\', '/'))

    # Check if any restricted file is in nav
    nav_config = config.get('nav', [])
    if nav_config:
        check_nav_for_restricted(nav_config, restricted_paths)
        
    # Remove restricted files from the build
    for f in restricted_files:
        files.remove(f)
        
    return files
