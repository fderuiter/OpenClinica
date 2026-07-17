import re

with open('/app/core/src/main/java/org/akaza/openclinica/dao/core/EntityDAO.java', 'r') as f:
    code = f.read()

# Fields to remove
code = re.sub(r'private HashMap setTypes = new HashMap\(\);\s*', '', code)
code = re.sub(r'private boolean querySuccessful;\s*', '', code)
code = re.sub(r'private SQLException failureDetails;\s*', '', code)
code = re.sub(r'private int latestPK;\s*', '', code)

# Methods to remove
methods_to_remove = [
    r'public void setTypeExpected\(int num, int type\) \{.*?\n    \}\s*',
    r'public void unsetTypeExpected\(\) \{.*?\n    \}\s*',
    r'public void clearSignals\(\) \{.*?\n    \}\s*',
    r'protected void signalSuccess\(\) \{.*?\n    \}\s*',
    r'protected void signalFailure\(SQLException failureDetails\) \{.*?\n    \}\s*',
    r'public SQLException getFailureDetails\(\) \{.*?\n    \}\s*',
    r'public boolean isQuerySuccessful\(\) \{.*?\n    \}\s*',
    r'protected int getLatestPK\(\) \{.*?\n    \}\s*'
]
for m in methods_to_remove:
    code = re.sub(m, '', code, flags=re.DOTALL)

# Refactor processResultRows
code = re.sub(r'public ArrayList processResultRows\(ResultSet rs\) \{', 'public ArrayList processResultRows(ResultSet rs, java.util.HashMap<Integer, Integer> _types) {', code)
code = re.sub(r'setTypes\.get\(Integer\.valueOf\(i\)\)', '_types.get(Integer.valueOf(i))', code)

# Refactor select
code = re.sub(r'public ArrayList select\(String query\) \{', 'public ArrayList select(String query, java.util.HashMap<Integer, Integer> _types) {', code)
code = re.sub(r'public ArrayList<V> select\(String query, HashMap variables\) \{', 'public ArrayList<V> select(String query, HashMap variables, java.util.HashMap<Integer, Integer> _types) {', code)
code = re.sub(r'public ArrayList select\(String query, Connection con\) \{', 'public ArrayList select(String query, Connection con, java.util.HashMap<Integer, Integer> _types) {', code)
code = re.sub(r'public ArrayList<V> selectByCache\(String query, HashMap variables\) \{', 'public ArrayList<V> selectByCache(String query, HashMap variables, java.util.HashMap<Integer, Integer> _types) {', code)

code = re.sub(r'processResultRows\(rs\)', 'processResultRows(rs, _types)', code)

code = re.sub(r'clearSignals\(\);\s*', '', code)
code = re.sub(r'signalSuccess\(\);\s*', '', code)
code = re.sub(r'signalFailure\(sqle\);\s*', '', code)
code = re.sub(r'signalFailure\(npe\);\s*', '', code)

# execute methods
code = re.sub(r'public void execute\(String query\)', 'public boolean execute(String query)', code)
code = re.sub(r'public void execute\(String query, Connection con\)', 'public boolean execute(String query, Connection con)', code)
code = re.sub(r'public void execute\(String query, HashMap variables\)', 'public boolean execute(String query, HashMap variables)', code)
code = re.sub(r'public void execute\(String query, HashMap variables, Connection con\)', 'public boolean execute(String query, HashMap variables, Connection con)', code)
code = re.sub(r'public void execute\(String query, HashMap variables, HashMap nullVars\)', 'public boolean execute(String query, HashMap variables, HashMap nullVars)', code)
code = re.sub(r'public void execute\(String query, HashMap variables, HashMap nullVars, Connection con\)', 'public boolean execute(String query, HashMap variables, HashMap nullVars, Connection con)', code)

# executeWithPK
code = re.sub(r'public void executeWithPK\(String query, HashMap variables\)', 'public int executeWithPK(String query, HashMap variables)', code)
code = re.sub(r'public void executeWithPK\(String query, HashMap variables, Connection con\)', 'public int executeWithPK(String query, HashMap variables, Connection con)', code)
code = re.sub(r'public void executeWithPK\(String query, HashMap variables, HashMap nullVars\)', 'public int executeWithPK(String query, HashMap variables, HashMap nullVars)', code)
code = re.sub(r'public void executeWithPK\(String query, HashMap variables, HashMap nullVars, Connection con\)', 'public int executeWithPK(String query, HashMap variables, HashMap nullVars, Connection con)', code)

# Add return statements to execute
code = re.sub(r'(execute\(.*?return);', r'\1 true;', code)
code = re.sub(r'(executeWithPK\(.*?return);', r'\1 0;', code)

# Insert return true; at end of try blocks for execute
code = re.sub(r'(ps\.executeUpdate\(\);\s*\n\s*)(\}\s*catch \(SQLException sqle\))', r'\1return true;\n        \2', code)

# Insert return false; in catch blocks for execute
# Wait, this is hard with regex. Let's just do it manually by finding and replacing.
