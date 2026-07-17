import re

with open('/app/core/src/main/java/org/akaza/openclinica/dao/core/EntityDAO.java', 'r') as f:
    code = f.read()

# Replace "execute" logic completely
def replace_execute(code):
    lines = code.split('\n')
    new_lines = []
    i = 0
    while i < len(lines):
        line = lines[i]
        
        # fix return;
        if 'execute(' in line and 'return;' in line:
            line = line.replace('return;', 'return true;')
        if 'executeWithPK(' in line and 'return;' in line:
            line = line.replace('return;', 'return 0;')
            
        if 'ps.executeUpdate();' in line:
            new_lines.append(line)
            # check if it's execute or executeWithPK
            in_exec = False
            in_execPK = False
            for j in range(i-20, i):
                if j>=0 and 'public boolean execute(' in lines[j]: in_exec = True
                if j>=0 and 'public int executeWithPK(' in lines[j]: in_execPK = True
            if in_exec:
                new_lines.append('            return true;')
            if in_execPK:
                pass # it has latestPK = this.getCurrentPK(con);
        elif 'latestPK = this.getCurrentPK(con);' in line:
            new_lines.append('            int latestPK = this.getCurrentPK(con);')
            new_lines.append('            return latestPK;')
        elif '} catch (SQLException sqle) {' in line or '} catch (NullPointerException npe) {' in line:
            new_lines.append(line)
            # Find if we are in execute or executeWithPK
            in_exec = False
            in_execPK = False
            for j in range(i-40, i):
                if j>=0 and 'public boolean execute(' in lines[j]: in_exec = True
                if j>=0 and 'public int executeWithPK(' in lines[j]: in_execPK = True
            
            # The catch blocks are usually 5 lines long. We can just append return false; before the closing brace of catch block.
            # We will handle it by injecting return statement.
        elif line.strip() == '}' and ('logger.error' in lines[i-1] or 'logger.error' in lines[i-2] or 'logger.error' in lines[i-3]):
            # End of catch block
            in_exec = False
            in_execPK = False
            for j in range(i-60, i):
                if j>=0 and 'public boolean execute(' in lines[j]: in_exec = True
                if j>=0 and 'public int executeWithPK(' in lines[j]: in_execPK = True
            
            if in_exec:
                new_lines.append('            return false;')
            elif in_execPK:
                new_lines.append('            return -1;')
            new_lines.append(line)
        else:
            new_lines.append(line)
        i += 1
    return '\n'.join(new_lines)

code = replace_execute(code)

with open('/app/core/src/main/java/org/akaza/openclinica/dao/core/EntityDAO.java', 'w') as f:
    f.write(code)

