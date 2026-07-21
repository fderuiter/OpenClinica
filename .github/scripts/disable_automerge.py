import os
import json
import subprocess
import time
import sys

def run_command(cmd, max_retries=3):
    for attempt in range(max_retries):
        result = subprocess.run(cmd, capture_output=True, text=True)
        if result.returncode == 0:
            return result.stdout
        else:
            print(f"Command failed (attempt {attempt + 1}/{max_retries}): {' '.join(cmd)}")
            print(f"Error: {result.stderr}")
            if attempt < max_retries - 1:
                time.sleep(2 ** attempt)
            else:
                raise Exception(f"Command failed: {' '.join(cmd)}")

def get_event_data():
    event_path = os.environ.get('GITHUB_EVENT_PATH')
    if not event_path or not os.path.exists(event_path):
        print("GITHUB_EVENT_PATH not found or file does not exist.")
        sys.exit(1)
    with open(event_path, 'r') as f:
        return json.load(f)

def get_pr_files(repo, pr_number):
    stdout = run_command(['gh', 'api', f'repos/{repo}/pulls/{pr_number}/files', '--paginate'])
    files = json.loads(stdout)
    return [f['filename'] for f in files]

def check_automerge_status(node_id):
    query = """
    query($nodeId: ID!) {
      node(id: $nodeId) {
        ... on PullRequest {
          autoMergeRequest {
            enabledAt
            enabledBy {
              login
            }
          }
        }
      }
    }
    """
    stdout = run_command(['gh', 'api', 'graphql', '-f', f'nodeId={node_id}', '-f', f'query={query}'])
    data = json.loads(stdout)
    pr_node = data.get('data', {}).get('node', {})
    return pr_node.get('autoMergeRequest') is not None

def disable_automerge(node_id):
    mutation = """
    mutation($pullRequestId: ID!) {
      disablePullRequestAutoMerge(input: { pullRequestId: $pullRequestId }) {
        clientMutationId
      }
    }
    """
    run_command(['gh', 'api', 'graphql', '-f', f'pullRequestId={node_id}', '-f', f'query={mutation}'])

def get_existing_comments(repo, pr_number):
    stdout = run_command(['gh', 'api', f'repos/{repo}/issues/{pr_number}/comments', '--paginate'])
    return json.loads(stdout)

def post_comment(repo, pr_number, body):
    run_command(['gh', 'api', f'repos/{repo}/issues/{pr_number}/comments', '-f', f'body={body}'])

def main():
    if not os.environ.get('GITHUB_TOKEN'):
        print("GITHUB_TOKEN is not set.")
        sys.exit(1)

    event_data = get_event_data()
    pr_data = event_data.get('pull_request')
    if not pr_data:
        print("Not a pull request event.")
        return

    pr_number = pr_data['number']
    node_id = pr_data['node_id']
    repo = pr_data['base']['repo']['full_name']

    files = get_pr_files(repo, pr_number)
    
    has_db_migration = False
    has_core_backend = False

    for f in files:
        if f.startswith('core/src/main/resources/migration/') and f.endswith('.xml'):
            has_db_migration = True
        elif f.startswith('core/'):
            has_core_backend = True

    if not has_db_migration and not has_core_backend:
        print("No core or database migration files modified. Exiting.")
        return

    # Check if auto-merge is active
    is_automerge_active = check_automerge_status(node_id)
    if is_automerge_active:
        print("Auto-merge is active. Disabling it...")
        disable_automerge(node_id)
        print("Auto-merge disabled.")
    else:
        print("Auto-merge is not active.")

    # Check for existing comment
    comments = get_existing_comments(repo, pr_number)
    signature = "<!-- auto-merge-disabler-signal -->"
    has_comment = any(signature in c.get('body', '') for c in comments)

    if not has_comment:
        reasons = []
        if has_db_migration:
            reasons.append("database schema migration files")
        if has_core_backend:
            reasons.append("core backend module files")
        
        reason_str = " and ".join(reasons)
        
        body = f"{signature}\n\n"
        body += f"⚠️ **Auto-Merge Disabled** ⚠️\n\n"
        body += f"This pull request contains modifications to **{reason_str}**. "
        body += "To prevent unintended production deployments and database schema corruption, automatic merging is disabled for these critical components.\n\n"
        body += "A release manager must manually execute the merge after completing the required verification."
        
        print("Posting warning comment...")
        post_comment(repo, pr_number, body)
        print("Comment posted.")
    else:
        print("Warning comment already exists. Skipping.")

if __name__ == "__main__":
    main()
