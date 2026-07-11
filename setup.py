import sys
import subprocess
import socket
import os
import re
from urllib.parse import urlparse

def handle_validation_failure(tool_name, error_msg, required_version):
    if sys.stdin.isatty():
        print(f"Warning: {error_msg}")
        print(f"Required version for {tool_name} is {required_version}.")
        while True:
            resp = input("Do you wish to bypass this warning and continue? (y/n): ").strip().lower()
            if resp == 'y':
                return
            elif resp == 'n':
                print("Aborting setup.")
                sys.exit(1)
            else:
                print("Invalid input. Please enter 'y' or 'n'.")
    else:
        print(f"Error: {error_msg}")
        print(f"Required version for {tool_name} is {required_version}.")
        sys.exit(1)

def check_java_version():
    required_version = "17"
    try:
        result = subprocess.run(["java", "-version"], capture_output=True, text=True, check=True)
        output = result.stderr + result.stdout
    except Exception:
        handle_validation_failure("Java", "Java is missing or not executable (or error running java -version).", required_version)
        return
        
    match = re.search(r'version "([^"]+)"', output)
    if not match:
        handle_validation_failure("Java", "Could not parse Java version from output.", required_version)
        return
    
    version_str = match.group(1)
    
    major_version = 0
    if version_str.startswith("1."):
        parts = version_str.split(".")
        if len(parts) > 1 and parts[1].isdigit():
            major_version = int(parts[1])
    else:
        parts = version_str.split(".")
        if len(parts) > 0 and parts[0].isdigit():
            major_version = int(parts[0])
            
    if major_version < 17:
        handle_validation_failure("Java", f"Installed Java version is {version_str}.", required_version)
        return
    print("Java version check passed.")

def check_maven_version():
    required_version = "3.0.0"
    try:
        result = subprocess.run(["mvn", "--version"], capture_output=True, text=True, check=True)
        output = result.stdout + result.stderr
    except Exception:
        handle_validation_failure("Maven", "Maven is missing or not executable.", required_version)
        return
        
    match = re.search(r'Apache Maven (\d+)\.(\d+)(?:\.(\d+))?', output)
    if not match:
        handle_validation_failure("Maven", "Could not parse Maven version.", required_version)
        return
        
    major = int(match.group(1))
    minor = int(match.group(2))
    patch = int(match.group(3)) if match.group(3) else 0
    
    if (major, minor, patch) < (3, 0, 0):
        version_str = f"{major}.{minor}.{patch}"
        handle_validation_failure("Maven", f"Installed Maven version is {version_str}.", required_version)
        return
    print("Maven version check passed.")

def check_docker_version():
    required_version = "19.03.0"
    try:
        result = subprocess.run(["docker", "--version"], capture_output=True, text=True, check=True)
        output = result.stdout + result.stderr
    except Exception:
        handle_validation_failure("Docker", "Docker is missing or not executable.", required_version)
        return
        
    match = re.search(r'Docker version (\d+)\.(\d+)(?:\.(\d+))?', output)
    if not match:
        handle_validation_failure("Docker", "Could not parse Docker version.", required_version)
        return
        
    major = int(match.group(1))
    minor = int(match.group(2))
    patch = int(match.group(3)) if match.group(3) else 0
    
    if (major, minor, patch) < (19, 3, 0):
        version_str = f"{major}.{minor}.{patch}"
        handle_validation_failure("Docker", f"Installed Docker version is {version_str}.", required_version)
        return
    print("Docker version check passed.")

def check_node_version():
    required_version = "22.13.0"
    try:
        result = subprocess.run(["node", "--version"], capture_output=True, text=True, check=True)
        output = result.stdout + result.stderr
    except Exception:
        handle_validation_failure("Node", "Node is missing or not executable.", required_version)
        return
        
    match = re.search(r'v?(\d+)\.(\d+)(?:\.(\d+))?', output)
    if not match:
        handle_validation_failure("Node", "Could not parse Node version.", required_version)
        return
        
    major = int(match.group(1))
    minor = int(match.group(2))
    patch = int(match.group(3)) if match.group(3) else 0
    
    if (major, minor, patch) < (22, 13, 0):
        version_str = f"{major}.{minor}.{patch}"
        handle_validation_failure("Node", f"Installed Node version is {version_str}.", required_version)
        return
    print("Node version check passed.")

def check_npm_version():
    required_version = "9.6.7"
    try:
        result = subprocess.run(["npm", "--version"], capture_output=True, text=True, check=True)
        output = result.stdout + result.stderr
    except Exception:
        handle_validation_failure("NPM", "NPM is missing or not executable.", required_version)
        return
        
    match = re.search(r'(\d+)\.(\d+)(?:\.(\d+))?', output)
    if not match:
        handle_validation_failure("NPM", "Could not parse NPM version.", required_version)
        return
        
    major = int(match.group(1))
    minor = int(match.group(2))
    patch = int(match.group(3)) if match.group(3) else 0
    
    if (major, minor, patch) < (9, 6, 7):
        version_str = f"{major}.{minor}.{patch}"
        handle_validation_failure("NPM", f"Installed NPM version is {version_str}.", required_version)
        return
    print("NPM version check passed.")

def verify_connection(host, port, service_name):
    try:
        port = int(port)
        s = socket.create_connection((host, port), timeout=5)
        s.close()
        return True
    except Exception as e:
        print(f"Immediate feedback: Connectivity error! Failed to connect to {service_name} at {host}:{port} ({e})")
        return False

def main():
    print("Welcome to the Guided Developer Setup!")
    check_java_version()
    check_maven_version()
    check_docker_version()
    check_node_version()
    check_npm_version()

    print("\nLet's configure your environment.")
    
    # Defaults
    env_vars = {}
    
    db_host = input("Database Host (default: localhost): ").strip() or "localhost"
    db_port = input("Database Port (default: 5432): ").strip() or "5432"
    db_user = input("Database User (default: clinica): ").strip() or "clinica"
    db_pass = input("Database Password (default: clinica): ").strip() or "clinica"
    db_name = input("Database Name (default: clinica): ").strip() or "clinica"
    
    # immediate feedback
    if db_host not in ["db", "localhost", "127.0.0.1"]:
        print(f"Verifying connection to {db_host}:{db_port}...")
        if not verify_connection(db_host, db_port, "Database"):
            sys.exit(1)

    env_vars["DB_HOST"] = db_host
    env_vars["DB_PORT"] = db_port
    env_vars["DB_USER"] = db_user
    env_vars["DB_PASS"] = db_pass
    env_vars["DB"] = db_name
    env_vars["DB_TYPE"] = "postgres"

    ldap_enabled = input("Enable LDAP? (y/N): ").strip().lower() == 'y'
    env_vars["LDAP_ENABLED"] = "true" if ldap_enabled else "false"
    if ldap_enabled:
        ldap_host = input("LDAP Host (e.g., ldap://localhost:389): ").strip() or "ldap://localhost:389"
        env_vars["LDAP_HOST"] = ldap_host
        env_vars["LDAP_USER_DN"] = input("LDAP User DN: ").strip() or "cn=admin,dc=localhost"
        env_vars["LDAP_PASSWORD"] = input("LDAP Password: ").strip() or "localhost"
        
        parsed_url = urlparse(ldap_host)
        l_host = parsed_url.hostname or "localhost"
        l_port = parsed_url.port or 389
        if l_host not in ["localhost", "127.0.0.1"]:
            print(f"Verifying connection to {l_host}:{l_port}...")
            if not verify_connection(l_host, l_port, "LDAP"):
                sys.exit(1)

    host_file_path = input("File Path for data (default: /app/data): ").strip() or "/app/data"
    env_vars["HOST_FILE_PATH"] = os.path.abspath(host_file_path)
    env_vars["FILE_PATH"] = "/opt/clinica/data/"

    seed_clinical = input("Enable clinical data seeding? (y/N): ").strip().lower() == 'y'
    env_vars["SEED_CLINICAL_DATA"] = "true" if seed_clinical else "false"
    if seed_clinical:
        template_path = input("Path to local Excel template: ").strip()
        if not os.path.isfile(template_path):
            print(f"Error: Template file not found at {template_path}")
            sys.exit(1)
        env_vars["HOST_CLINICAL_TEMPLATE_PATH"] = os.path.abspath(template_path)
        env_vars["CLINICAL_TEMPLATE_PATH"] = "/opt/clinica/template.xlsx"
    else:
        dummy_path = os.path.abspath("dummy_template.xlsx")
        with open(dummy_path, "w") as f:
            pass
        env_vars["HOST_CLINICAL_TEMPLATE_PATH"] = dummy_path
        env_vars["CLINICAL_TEMPLATE_PATH"] = ""

    # Directory structures
    os.makedirs(host_file_path, exist_ok=True)
    print(f"Created directory structure: {host_file_path}")

    with open(".env", "w") as f:
        for k, v in env_vars.items():
            f.write(f"{k}={v}\n")
    
    print("\nSetup complete! You can now start the application.")

if __name__ == "__main__":
    main()
