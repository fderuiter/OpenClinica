import os
import re

def fix_call(file_path, line_num, req_types, is_super=False):
    with open(file_path, 'r') as f:
        lines = f.readlines()
        
    line_idx = line_num - 1
    target = 'super' if is_super else 'new '
    
    if target not in lines[line_idx]:
        for offset in [-2, -1, 1, 2]:
            if line_idx + offset >= 0 and line_idx + offset < len(lines) and target in lines[line_idx + offset]:
                line_idx = line_idx + offset
                break

    dao_vars = []
    for req in req_types:
        if req.endswith('DAO'):
            dt = req.split('.')[-1]
            var_name = '_' + dt[0].lower() + dt[1:len(dt)-3] + 'DAO'
            dao_vars.append(var_name)
    
    if not dao_vars:
        return False
        
    line = lines[line_idx]
    
    target_idx = line.find(target)
    if target_idx != -1:
        start_paren = line.find('(', target_idx)
        if start_paren != -1:
            end_paren = line.rfind(')')
            if end_paren != -1 and end_paren > start_paren:
                current_args = line[start_paren+1:end_paren].strip()
                if current_args:
                    new_args = current_args + ", " + ", ".join(dao_vars)
                else:
                    new_args = ", ".join(dao_vars)
                lines[line_idx] = line[:start_paren+1] + new_args + line[end_paren:]
                
                with open(file_path, 'w') as f:
                    f.writelines(lines)
                return True
    return False
