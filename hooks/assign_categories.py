import os

def on_page_markdown(markdown, page, config, files):
    if not hasattr(page, 'meta'):
        page.meta = {}
        
    path = page.file.src_path.replace('\\', '/')
    
    tags = page.meta.get('tags', [])
    if not isinstance(tags, list):
        tags = [tags]
        
    if 'explanation' in path:
        tags.append('Explanation')
    elif 'tutorials' in path:
        tags.append('Tutorials')
    elif 'how-to' in path:
        tags.append('Guides')
    elif 'reference' in path:
        tags.append('Reference')
        
    if tags:
        page.meta['tags'] = list(set(tags))
        
    return markdown
