import os
import shutil

def on_post_build(config, **kwargs):
    site_dir = config['site_dir']
    
    files_to_copy = [
        'core/external-api.json',
        'core/src/main/resources/randomize-api.yaml'
    ]
    
    for f in files_to_copy:
        if os.path.exists(f):
            dest = os.path.join(site_dir, f)
            os.makedirs(os.path.dirname(dest), exist_ok=True)
            shutil.copy2(f, dest)
            print(f"Copied {f} to {dest}")
