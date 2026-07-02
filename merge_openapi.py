import json
import yaml
import os

def merge_dicts(dict1, dict2):
    for k, v in dict2.items():
        if isinstance(v, dict) and k in dict1 and isinstance(dict1[k], dict):
            merge_dicts(dict1[k], v)
        else:
            dict1[k] = v
    return dict1

def merge_specs():
    base_spec = {
        "openapi": "3.0.0",
        "info": {
            "title": "Unified OpenClinica REST API",
            "version": "1.0.0",
            "description": "Interactive documentation for all OpenClinica REST endpoints."
        },
        "paths": {},
        "components": {}
    }

    specs_to_merge = [
        'core/external-api.json',
        'core/src/main/resources/randomize-api.yaml',
        'web/target/generated-docs/legacy-openapi.json',
        'modern/target/generated-docs/modern-openapi.json'
    ]

    for spec_path in specs_to_merge:
        if not os.path.exists(spec_path):
            print(f"Warning: {spec_path} not found. Skipping.")
            continue
            
        with open(spec_path, 'r') as f:
            if spec_path.endswith('.yaml') or spec_path.endswith('.yml'):
                spec = yaml.safe_load(f)
            else:
                spec = json.load(f)
                
            merge_dicts(base_spec['paths'], spec.get('paths', {}))
            merge_dicts(base_spec['components'], spec.get('components', {}))

    os.makedirs('docs', exist_ok=True)
    with open('docs/openapi.json', 'w') as f:
        json.dump(base_spec, f, indent=2)

if __name__ == "__main__":
    merge_specs()
