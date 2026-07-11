import sys
import subprocess
import socket
import os
import re
import tempfile
import base64
import signal
import shutil
from urllib.parse import urlparse

created_paths = []

def cleanup_and_exit(signum, frame):
    print("\nSetup aborted. Cleaning up temporary files...")
    for path in reversed(created_paths):
        if os.path.exists(path):
            if os.path.isdir(path):
                try:
                    os.rmdir(path)
                except OSError:
                    shutil.rmtree(path, ignore_errors=True)
            else:
                os.remove(path)
    sys.exit(1)

signal.signal(signal.SIGINT, cleanup_and_exit)
signal.signal(signal.SIGTERM, cleanup_and_exit)

def check_python_version():
    if sys.version_info < (3, 6):
        print("Error: Python 3.6+ is required.")
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

def check_java_version():
    try:
        result = subprocess.run(["java", "-version"], capture_output=True, text=True, check=True)
        output = result.stderr + result.stdout
    except Exception:
        print("Error: Java is missing or not executable.")
        sys.exit(1)
        
    match = re.search(r'version "([^"]+)"', output)
    if not match:
        print("Error: Could not parse Java version. Please install Java 17.")
        sys.exit(1)
    
    version_str = match.group(1)
    
    if version_str.startswith("1."):
        major_version = int(version_str.split(".")[1])
    else:
        major_version = int(version_str.split(".")[0])
        
    if major_version < 17:
        print(f"Error: Installed Java version is {version_str}. Please upgrade to Java 17.")
        sys.exit(1)

def check_maven_version():
    try:
        result = subprocess.run(["mvn", "--version"], capture_output=True, text=True, check=True)
        output = result.stdout + result.stderr
    except Exception:
        print("Error: Maven is missing or not executable.")
        sys.exit(1)
        
    match = re.search(r'Apache Maven (\d+)\.(\d+)\.(\d+)', output)
    if not match:
        print("Error: Could not parse Maven version. Please install Maven >= 3.6.3.")
        sys.exit(1)
        
    major, minor, patch = map(int, match.groups())
    
    if (major < 3) or (major == 3 and minor < 6) or (major == 3 and minor == 6 and patch < 3):
        version_str = f"{major}.{minor}.{patch}"
        print(f"Error: Installed Maven version is {version_str}. Please upgrade to Maven >= 3.6.3.")
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
    check_java_version()
    check_maven_version()
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
    else:
        dummy_path = os.path.abspath("dummy_template.xlsx")
        empty_xlsx_base64 = (
            "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"
        )
        
        temp_dummy = tempfile.NamedTemporaryFile(mode='wb', delete=False, dir=os.path.dirname(dummy_path))
        try:
            temp_dummy.write(base64.b64decode(empty_xlsx_base64))
            temp_dummy.close()
            os.replace(temp_dummy.name, dummy_path)
            created_paths.append(dummy_path)
        except Exception as e:
            os.remove(temp_dummy.name)
            raise e
            
        env_vars["HOST_CLINICAL_TEMPLATE_PATH"] = dummy_path
        env_vars["CLINICAL_TEMPLATE_PATH"] = ""

    # Directory structures
    if not os.path.exists(host_file_path):
        os.makedirs(host_file_path, exist_ok=True)
        created_paths.append(host_file_path)
        print(f"Created directory structure: {host_file_path}")
    else:
        print(f"Directory structure already exists: {host_file_path}")

    env_path = os.path.abspath(".env")
    temp_env = tempfile.NamedTemporaryFile(mode='w', delete=False, dir=os.path.dirname(env_path))
    try:
        for k, v in env_vars.items():
            temp_env.write(f"{k}={v}\n")
        temp_env.close()
        os.replace(temp_env.name, env_path)
        created_paths.append(env_path)
    except Exception as e:
        os.remove(temp_env.name)
        raise e
    
    print("\nSetup complete! You can now start the application.")

if __name__ == "__main__":
    main()
