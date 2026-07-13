#!/usr/bin/env python3
import sys
import json
import argparse
from urllib.parse import urlparse

WHITELISTED_PATHS = [
    "/DataEntry", "/interop", "/api", "/v3/api-docs", "/swagger-ui",
    "/ListUserAccounts", "/CreateUserAccount", "/EditUserAccount",
    "/ViewUserAccount", "/DeleteUser"
]

def is_whitelisted(path):
    for w in WHITELISTED_PATHS:
        if path.startswith(w):
            return True
    return False

def simulate_proxy(args):
    path = args.path
    method = args.method.upper()
    user = args.user

    result = {
        "source_path": path,
        "method": method,
        "is_proxied": False,
        "target_url": None,
        "headers": {}
    }

    if not is_whitelisted(path):
        result["is_proxied"] = True
        result["target_url"] = f"http://localhost:8080{path}"
        if user:
            result["headers"]["REMOTE_USER"] = user
    else:
        result["is_proxied"] = False

    if args.json:
        print(json.dumps(result, indent=2))
    else:
        print(f"--- Proxy Route Analysis ---")
        print(f"Source Path: {path}")
        print(f"Method: {method}")
        if result["is_proxied"]:
            print(f"Target URL: {result['target_url']}")
            print("Injected Headers:")
            for k, v in result["headers"].items():
                print(f"  {k}: {v}")
        else:
            print(f"Status: Handled Locally (Whitelisted)")

def get_pg_type(data_type_id):
    if data_type_id in (1, 2):
        return 'BOOLEAN'
    elif data_type_id == 6:
        return 'INTEGER'
    elif data_type_id == 7:
        return 'NUMERIC'
    elif data_type_id == 9:
        return 'DATE'
    else:
        return 'VARCHAR(4000)'

def simulate_sandbox(args):
    try:
        schema = json.loads(args.schema)
    except json.JSONDecodeError:
        print("Error: Invalid JSON schema payload")
        sys.exit(1)

    crf_version_id = schema.get("crf_version_id", 1)
    items = schema.get("items", [])

    table_name = f"flattened_crf_{crf_version_id}"
    columns = [
        {"name": "event_crf_id", "type": "INT"},
        {"name": "ordinal", "type": "INT"},
        {"name": "date_updated", "type": "TIMESTAMP"},
        {"name": "update_id", "type": "INT"}
    ]

    for item in items:
        oid = item.get("oc_oid", "").lower()
        if not oid:
            continue
        data_type_id = item.get("item_data_type_id")
        pg_type = get_pg_type(data_type_id)
        columns.append({"name": oid, "type": pg_type})

    result = {
        "table_name": table_name,
        "columns": columns
    }

    if args.json:
        print(json.dumps(result, indent=2))
    else:
        print(f"--- Database Sandbox Preview ---")
        print(f"Table Name: {table_name}")
        print(f"Columns:")
        for col in columns:
            print(f"  - {col['name']} ({col['type']})")

def main():
    parser = argparse.ArgumentParser(description="Shared Integration SDK and Local Developer CLI")
    subparsers = parser.add_subparsers(dest="command", help="Available commands")

    # Proxy Command
    proxy_parser = subparsers.add_parser("proxy", help="Simulate proxy traffic targeting the proxy gateway")
    proxy_parser.add_argument("method", help="HTTP Method (GET, POST, etc.)")
    proxy_parser.add_argument("path", help="Request path (e.g., /api/test)")
    proxy_parser.add_argument("--user", help="Authenticated user (injects REMOTE_USER)", default=None)
    proxy_parser.add_argument("--json", action="store_true", help="Output trace logs in readable JSON format")

    # Sandbox Command
    sandbox_parser = subparsers.add_parser("sandbox", help="Simulate dynamic database schema triggers")
    sandbox_parser.add_argument("schema", help='JSON payload of schema, e.g. {"crf_version_id":1,"items":[{"oc_oid":"AGE","item_data_type_id":6}]}')
    sandbox_parser.add_argument("--json", action="store_true", help="Output trace logs in readable JSON format")

    args = parser.parse_args()

    if args.command == "proxy":
        simulate_proxy(args)
    elif args.command == "sandbox":
        simulate_sandbox(args)
    else:
        parser.print_help()

if __name__ == "__main__":
    main()
