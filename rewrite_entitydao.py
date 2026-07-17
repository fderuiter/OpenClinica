import re

with open('/app/core/src/main/java/org/akaza/openclinica/dao/core/EntityDAO.java', 'r') as f:
    code = f.read()

# Make sure processResultRows doesn't use setTypes
new_process_result = """    public ArrayList processResultRows(ResultSet rs) {
        ArrayList al = new ArrayList();
        HashMap hm;
        try {
            while (rs.next()) {
                hm = new HashMap();
                ResultSetMetaData rsmd = rs.getMetaData();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    String column = rsmd.getColumnName(i).toLowerCase();
                    hm.put(column, rs.getObject(i));
                }
                al.add(hm);
            }
        } catch (SQLException e) {
            logger.error("Exception in processResultRows", e);
        }
        return al;
    }"""
m = re.search(r'public ArrayList processResultRows\(ResultSet rs\) \{\s*ArrayList al = new ArrayList\(\);\s*HashMap hm;\s*try \{.*?\n    \}\n', code, re.DOTALL)
if m:
    code = code[:m.start()] + new_process_result + code[m.end():]

# Remove the state fields
code = re.sub(r'private HashMap setTypes = new HashMap\(\);\s*', '', code)
code = re.sub(r'private boolean querySuccessful;\s*', '', code)
code = re.sub(r'private SQLException failureDetails;\s*', '', code)
code = re.sub(r'private int latestPK;\s*', '', code)

# Remove the methods
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

# Remove all clearSignals() and signalSuccess() and signalFailure(...)
code = re.sub(r'\s*clearSignals\(\);', '', code)
code = re.sub(r'\s*signalSuccess\(\);', '', code)
code = re.sub(r'\s*signalFailure\(sqle\);', '', code)
code = re.sub(r'\s*signalFailure\(npe\);', '', code)

# Change return types of execute
code = re.sub(r'public void execute\(String query\)', 'public boolean execute(String query)', code)
code = re.sub(r'public void execute\(String query, Connection con\)', 'public boolean execute(String query, Connection con)', code)
code = re.sub(r'public void execute\(String query, HashMap variables\)', 'public boolean execute(String query, HashMap variables)', code)
code = re.sub(r'public void execute\(String query, HashMap variables, Connection con\)', 'public boolean execute(String query, HashMap variables, Connection con)', code)
code = re.sub(r'public void execute\(String query, HashMap variables, HashMap nullVars\)', 'public boolean execute(String query, HashMap variables, HashMap nullVars)', code)
code = re.sub(r'public void execute\(String query, HashMap variables, HashMap nullVars, Connection con\)', 'public boolean execute(String query, HashMap variables, HashMap nullVars, Connection con)', code)

# Change return types of executeWithPK
code = re.sub(r'public void executeWithPK\(String query, HashMap variables\)', 'public int executeWithPK(String query, HashMap variables)', code)
code = re.sub(r'public void executeWithPK\(String query, HashMap variables, Connection con\)', 'public int executeWithPK(String query, HashMap variables, Connection con)', code)
code = re.sub(r'public void executeWithPK\(String query, HashMap variables, HashMap nullVars\)', 'public int executeWithPK(String query, HashMap variables, HashMap nullVars)', code)
code = re.sub(r'public void executeWithPK\(String query, HashMap variables, HashMap nullVars, Connection con\)', 'public int executeWithPK(String query, HashMap variables, HashMap nullVars, Connection con)', code)

# Replace execute method bodies to return true/false
# A simple way is to replace "return;" with "return true;" or "return 0;"
code = code.replace('            execute(query);\n            return;', '            return execute(query);\n')
code = code.replace('            execute(query, variables);\n            return;', '            return execute(query, variables);\n')
code = code.replace('            execute(query, variables, nullVars);\n            return;', '            return execute(query, variables, nullVars);\n')

code = code.replace('            executeWithPK(query, variables);\n            return;', '            return executeWithPK(query, variables);\n')
code = code.replace('            executeWithPK(query, variables, nullVars);\n            return;', '            return executeWithPK(query, variables, nullVars);\n')

# Replace ps.executeUpdate(); with ps.executeUpdate(); return true;
# BUT executeWithPK also has ps.executeUpdate() followed by latestPK = this.getCurrentPK(con);
# Let's fix executeWithPK first.
code = re.sub(r'latestPK = this\.getCurrentPK\(con\);\s*\} catch', r'return this.getCurrentPK(con);\n        } catch', code)
code = re.sub(r'ps\.executeUpdate\(\);\s*\} catch', r'ps.executeUpdate();\n            return true;\n        } catch', code)

# In catch blocks for execute:
# We need to insert return false; or return 0;
# We can use a simple python script to read lines.
def fix_catch(code):
    lines = code.split('\n')
    new_lines = []
    i = 0
    in_exec = False
    in_execPK = False
    while i < len(lines):
        line = lines[i]
        if 'public boolean execute(' in line:
            in_exec = True; in_execPK = False
        if 'public int executeWithPK(' in line:
            in_exec = False; in_execPK = True
        
        if line.strip() == '}' and ('logger.error' in lines[i-1] or 'logger.error' in lines[i-2] or 'logger.error' in lines[i-3] or 'logger.warn' in lines[i-1] or 'logger.warn' in lines[i-2] or 'logger.warn' in lines[i-3]):
            # Check if we are in execute
            if in_exec:
                new_lines.append('            return false;')
            elif in_execPK:
                new_lines.append('            return 0;')
        
        new_lines.append(line)
        i += 1
    return '\n'.join(new_lines)

code = fix_catch(code)

with open('/app/core/src/main/java/org/akaza/openclinica/dao/core/EntityDAO.java', 'w') as f:
    f.write(code)

