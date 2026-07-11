import os

def on_page_markdown(markdown, page, config, files):
    if not hasattr(page, 'meta'):
        page.meta = {}
        
    path = page.file.src_path.replace('\\', '/')
    
    tags = page.meta.get('tags', [])
    if not isinstance(tags, list):
        tags = [tags]
        
    if 'diataxis/tutorials' in path:
        tags.append('Tutorials')
    elif 'diataxis/how-to' in path or 'installation' in path or 'maintenance' in path or 'configuration' in path or 'frontend/' in path:
        tags.append('Guides')
    elif 'diataxis/references' in path or 'frontend-api' in path or 'api.md' in path or 'soap.md' in path:
        tags.append('Reference')
    elif 'diataxis/explanation' in path:
        tags.append('Explanation')
        
    if tags:
        page.meta['tags'] = list(set(tags))
        
    return markdown
