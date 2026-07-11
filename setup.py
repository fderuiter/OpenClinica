import sys
import subprocess
import socket
import os
import signal
import re
from urllib.parse import urlparse

def signal_handler(sig, frame):
    print("\nSetup cancelled. Cleaning up partial configuration...")
    if os.path.exists(".env"):
        os.remove(".env")
    if os.path.exists("docker-compose.override.yml"):
        os.remove("docker-compose.override.yml")
    sys.exit(0)

signal.signal(signal.SIGINT, signal_handler)

def check_python_version():
    if sys.version_info < (3, 6):
        print("Error: Python 3.6 or higher is required.")
        sys.exit(1)

def check_java_version():
    try:
        output = subprocess.check_output(["java", "-version"], stderr=subprocess.STDOUT, text=True)
        match = re.search(r'version "(\d+)', output)
        if match:
            major = int(match.group(1))
            if major == 1:
                match_minor = re.search(r'version "1\.(\d+)', output)
                if match_minor:
                    major = int(match_minor.group(1))
            if major < 17:
                print("Error: Java 17 or higher is required.")
                sys.exit(1)
        else:
            print("Error: Could not determine Java version.")
            sys.exit(1)
    except Exception as e:
        print(f"Error checking Java version: {e}")
        sys.exit(1)


def check_command(cmd, name):
    try:
        subprocess.run([cmd, "--version"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=True)
    except FileNotFoundError:
        print(f"Error: {name} is missing from the host system.")
        sys.exit(1)
    except Exception:
        # Some commands might not support --version, try without
        try:
            subprocess.run([cmd], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        except Exception:
            print(f"Error: {name} is missing or not executable.")
            sys.exit(1)

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
    check_python_version()
    print("Welcome to the Guided Developer Setup!")
    check_command("java", "Java")
    check_java_version()
    check_command("mvn", "Maven")
    check_command("docker", "Docker")

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
        with open("docker-compose.override.yml", "w") as f:
            f.write(f"version: '3.8'\nservices:\n  web:\n    volumes:\n    - {env_vars['HOST_CLINICAL_TEMPLATE_PATH']}:/opt/clinica/template.xlsx:ro\n")
    else:
        env_vars["CLINICAL_TEMPLATE_PATH"] = "/opt/clinica/fallback_template.xls"
        if os.path.exists("docker-compose.override.yml"):
            os.remove("docker-compose.override.yml")

    # Directory structures
    os.makedirs(host_file_path, exist_ok=True)
    print(f"Created directory structure: {host_file_path}")

    with open(".env", "w") as f:
        for k, v in env_vars.items():
            f.write(f"{k}={v}\n")
    
    print("\nSetup complete! You can now start the application.")

if __name__ == "__main__":
    main()
