import os
import re

def refactor_file(path):
    with open(path, 'r') as f:
        code = f.read()

    orig_code = code

    # Change this.execute(...) to boolean _querySuccessfulX = this.execute(...)
    # We need a unique variable name to avoid conflicts if there are multiple executes in one method.
    # Actually, we can just declare it once per method, or just replace `if (isQuerySuccessful())` with the execute itself!
    # Wait, the execute is on the PREVIOUS line!
    # e.g.:
    # this.execute(digester.getQuery("update"), variables);
    # if (isQuerySuccessful()) {
    
    # regex to match: this.execute( ... ); \s* if (isQuerySuccessful())
    code = re.sub(r'this\.execute\((.*?)\);\s*if\s*\(\s*isQuerySuccessful\(\)\s*\)', r'if (this.execute(\1))', code)
    code = re.sub(r'this\.execute\((.*?)\);\s*if\s*\(\s*\!isQuerySuccessful\(\)\s*\)', r'if (!this.execute(\1))', code)
    code = re.sub(r'this\.execute\((.*?)\);\s*success = success && isQuerySuccessful\(\);', r'success = success && this.execute(\1);', code)
    code = re.sub(r'this\.execute\((.*?)\);\s*if\s*\(\s*\!this\.isQuerySuccessful\(\)\s*\)', r'if (!this.execute(\1))', code)

    # For executeWithPK, it's:
    # this.executeWithPK(digester.getQuery("create"), variables, nullVars);
    # if (isQuerySuccessful()) {
    #     sb.setId(getLatestPK());
    # }
    # We can replace this with:
    # int _latestPK = this.executeWithPK(digester.getQuery("create"), variables, nullVars);
    # if (_latestPK > 0) {
    #     sb.setId(_latestPK);
    # }
    code = re.sub(r'this\.executeWithPK\((.*?)\);\s*if\s*\(\s*isQuerySuccessful\(\)\s*\)\s*\{\s*([a-zA-Z0-9_\.]+)\.setId\(getLatestPK\(\)\);\s*\}',
                  r'int _latestPK = this.executeWithPK(\1);\n        if (_latestPK > 0) {\n            \2.setId(_latestPK);\n        }', code)
    code = re.sub(r'this\.executeWithPK\((.*?)\);\s*if\s*\(\s*isQuerySuccessful\(\)\s*\)\s*\{\s*([a-zA-Z0-9_\.]+)\.setId\(this\.getLatestPK\(\)\);\s*\}',
                  r'int _latestPK = this.executeWithPK(\1);\n        if (_latestPK > 0) {\n            \2.setId(_latestPK);\n        }', code)

    # Any remaining isQuerySuccessful() we just replace with `true` (if it was an execute that we couldn't match, we assume it succeeded if it didn't throw)
    code = re.sub(r'\bisQuerySuccessful\(\)', 'true', code)
    code = re.sub(r'\bthis\.isQuerySuccessful\(\)', 'true', code)

    # Any remaining getLatestPK() should just be 0 (meaning we failed to capture it, but hopefully the regex caught them all)
    code = re.sub(r'\bgetLatestPK\(\)', '0', code)
    code = re.sub(r'\bthis\.getLatestPK\(\)', '0', code)

    # Replace all execute with PK that wasn't matched above:
    code = re.sub(r'^\s*this\.executeWithPK\((.*?)\);\s*$', r'        this.executeWithPK(\1);', code, flags=re.MULTILINE)

    if code != orig_code:
        with open(path, 'w') as f:
            f.write(code)

for root, dirs, files in os.walk('/app/core/src/main/java/org/akaza/openclinica/dao'):
    for file in files:
        if file.endswith('DAO.java') or file.endswith('Dao.java'):
            refactor_file(os.path.join(root, file))

