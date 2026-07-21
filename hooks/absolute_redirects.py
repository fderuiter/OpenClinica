import logging

log = logging.getLogger("mkdocs.hooks.absolute_redirects")

def on_post_build(config, **kwargs):
    site_url = config.get('site_url')
    if not site_url:
        return
        
    if not site_url.endswith('/'):
        site_url += '/'
        
    redirects_plugin = config['plugins'].get('redirects')
    if not redirects_plugin:
        return
        
    use_directory_urls = config.get('use_directory_urls')
    
    # redirects_plugin.redirects is the mapping
    # redirects_plugin.doc_pages is the available pages
    for page_old, page_new in redirects_plugin.redirects.items():
        # Reconstruct how the redirect file is written
        # We need to rewrite it to use absolute URL based on site_url
        from mkdocs_redirects.plugin import get_html_path, _split_hash_fragment, write_html
        
        page_new_without_hash, hash = _split_hash_fragment(str(page_new))
        
        if page_new.lower().startswith(("http://", "https://")):
            dest_path = page_new
        elif page_new_without_hash in redirects_plugin.doc_pages:
            file = redirects_plugin.doc_pages[page_new_without_hash]
            dest_url = file.url + hash
            dest_path = site_url + dest_url
        else:
            continue
            
        old_html_path = get_html_path(page_old, use_directory_urls)
        write_html(config['site_dir'], old_html_path, dest_path)
        
