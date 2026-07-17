import re

with open('/app/core/src/main/java/org/akaza/openclinica/dao/core/EntityDAO.java', 'r') as f:
    code = f.read()

# Remove fields
code = re.sub(r'private HashMap setTypes = new HashMap\(\);\s*', '', code)
code = re.sub(r'private boolean querySuccessful;\s*', '', code)
code = re.sub(r'private SQLException failureDetails;\s*', '', code)
code = re.sub(r'private int latestPK;\s*', '', code)

# Remove methods
code = re.sub(r'public void setTypeExpected\(int num, int type\) \{.*?\n    \}\s*', '', code, flags=re.DOTALL)
code = re.sub(r'public void unsetTypeExpected\(\) \{.*?\n    \}\s*', '', code, flags=re.DOTALL)

code = re.sub(r'public void clearSignals\(\) \{.*?\n    \}\s*', '', code, flags=re.DOTALL)
code = re.sub(r'protected void signalSuccess\(\) \{.*?\n    \}\s*', '', code, flags=re.DOTALL)
code = re.sub(r'protected void signalFailure\(SQLException failureDetails\) \{.*?\n    \}\s*', '', code, flags=re.DOTALL)
code = re.sub(r'public SQLException getFailureDetails\(\) \{.*?\n    \}\s*', '', code, flags=re.DOTALL)
code = re.sub(r'public boolean isQuerySuccessful\(\) \{.*?\n    \}\s*', '', code, flags=re.DOTALL)
code = re.sub(r'protected int getLatestPK\(\) \{.*?\n    \}\s*', '', code, flags=re.DOTALL)

# Refactor processResultRows
code = re.sub(r'public ArrayList processResultRows\(ResultSet rs\) \{', 'public ArrayList processResultRows(ResultSet rs, java.util.HashMap<Integer, Integer> _types) {', code)
code = re.sub(r'setTypes\.get\(Integer\.valueOf\(i\)\)', '_types.get(Integer.valueOf(i))', code)

# Refactor select
code = re.sub(r'public ArrayList select\(String query\) \{', 'public ArrayList select(String query, java.util.HashMap<Integer, Integer> _types) {', code)
code = re.sub(r'public ArrayList<V> select\(String query, HashMap variables\) \{', 'public ArrayList<V> select(String query, HashMap variables, java.util.HashMap<Integer, Integer> _types) {', code)
code = re.sub(r'public ArrayList select\(String query, Connection con\) \{', 'public ArrayList select(String query, Connection con, java.util.HashMap<Integer, Integer> _types) {', code)
code = re.sub(r'public ArrayList<V> selectByCache\(String query, HashMap variables\) \{', 'public ArrayList<V> selectByCache(String query, HashMap variables, java.util.HashMap<Integer, Integer> _types) {', code)

# Inside select methods, processResultRows(rs) -> processResultRows(rs, _types)
code = re.sub(r'processResultRows\(rs\)', 'processResultRows(rs, _types)', code)

# Remove clearSignals(), signalSuccess(), signalFailure(...) calls from everywhere in EntityDAO
code = re.sub(r'clearSignals\(\);\s*', '', code)
code = re.sub(r'signalSuccess\(\);\s*', '', code)
code = re.sub(r'signalFailure\(sqle\);\s*', '', code)

# Refactor execute to return boolean
code = re.sub(r'public void execute\(String query\)', 'public boolean execute(String query)', code)
code = re.sub(r'public void execute\(String query, Connection con\)', 'public boolean execute(String query, Connection con)', code)
code = re.sub(r'public void execute\(String query, HashMap variables\)', 'public boolean execute(String query, HashMap variables)', code)
code = re.sub(r'public void execute\(String query, HashMap variables, Connection con\)', 'public boolean execute(String query, HashMap variables, Connection con)', code)
code = re.sub(r'public void execute\(String query, HashMap variables, HashMap nullVars\)', 'public boolean execute(String query, HashMap variables, HashMap nullVars)', code)
code = re.sub(r'public void execute\(String query, HashMap variables, HashMap nullVars, Connection con\)', 'public boolean execute(String query, HashMap variables, HashMap nullVars, Connection con)', code)

# Inside execute methods, add return true on success, return false on failure
# This is tricky with regex. Let's write a simple loop over the code blocks.
# Execute blocks have a structure:
# public boolean execute(...) {
#    if (...) { execute(...); return; }
#    ...
#    try (...) {
#       ...
#       ps.executeUpdate();
#       // we should return true here!
#    } catch (SQLException sqle) {
#       ...
#       // we should return false here!
#    }
# }
# Let's fix the execute logic in the python script.

def fix_execute_methods(code):
    lines = code.split('\n')
    new_lines = []
    in_execute = False
    in_executeWithPK = False
    brace_level = 0
    for line in lines:
        if 'public boolean execute(' in line:
            in_execute = True
            brace_level = 0
        if 'public int executeWithPK(' in line:
            in_executeWithPK = True
            brace_level = 0
            
        if '{' in line:
            brace_level += line.count('{')
        if '}' in line:
            brace_level -= line.count('}')
            
        # We need to replace "return;" with "return true;" or "return result;"
        if in_execute and 'return;' in line:
            line = line.replace('return;', 'return true;')
        if in_executeWithPK and 'return;' in line:
            line = line.replace('return;', 'return 0;')
            
        # if we see ps.executeUpdate(); we append return true; (or we just add it at the end of try)
        if in_execute and 'ps.executeUpdate();' in line:
            pass # we will just add return true at the end of the method
            
        new_lines.append(line)
        
        if (in_execute or in_executeWithPK) and brace_level == 0 and '}' in line:
            in_execute = False
            in_executeWithPK = False
            
    return '\n'.join(new_lines)

# Actually, the easiest way is to modify EntityDAO manually using regex for the easy parts, then use another script for try/catches.
# Since there are only 8 execute methods, we can just replace them one by one.

execute_block1 = """    public boolean execute(String query) {
        try (Connection con = ds.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            if (con.isClosed()) { throw new SQLException(); }
            ps.executeUpdate();
            return true;
        } catch (SQLException sqle) {
            return false;
        } catch (NullPointerException npe) {
            return false;
        }
    }"""
# Wait, let's just do an AST or simple replace for all executes.
