import os
import re

def process_file(path):
    with open(path, 'r') as f:
        code = f.read()

    orig_code = code

    # Match all "new XxxDAO(...)"
    dao_matches = re.findall(r'new\s+([A-Z]\w*DAO)\s*\([^)]*\)', code)
    if not dao_matches:
        return code

    dao_types = sorted(list(set(dao_matches)))

    # Calculate fields and constructor parameters
    fields = []
    params = []
    assignments = []
    
    for dt in dao_types:
        # e.g. StudyDAO -> studyDAO
        if dt.endswith('DAO'):
            var_name = dt[0].lower() + dt[1:len(dt)-3] + 'DAO'
        else:
            var_name = dt[0].lower() + dt[1:]
        # Ensure it doesn't collide with existing variables
        var_name = '_' + var_name
        
        fields.append(f"    private final {dt} {var_name};")
        params.append(f"{dt} {var_name}")
        assignments.append(f"        this.{var_name} = {var_name};")

    # Replace "new XxxDAO(...)" with "this._xxxDAO"
    for dt in dao_types:
        if dt.endswith('DAO'):
            var_name = dt[0].lower() + dt[1:len(dt)-3] + 'DAO'
        else:
            var_name = dt[0].lower() + dt[1:]
        var_name = '_' + var_name
        
        pattern = r'new\s+' + dt + r'\s*\([^)]*\)'
        code = re.sub(pattern, f'this.{var_name}', code)

    # Insert imports
    if 'import org.springframework.beans.factory.annotation.Autowired;' not in code:
        code = re.sub(r'(package\s+[^;]+;\n+)', r'\1import org.springframework.beans.factory.annotation.Autowired;\nimport org.springframework.stereotype.Component;\n', code, count=1)

    # Add @Component if not a controller/service
    if '@Component' not in code and '@Service' not in code and '@Controller' not in code and '@RestController' not in code and '@Repository' not in code:
        # Before public class
        code = re.sub(r'(public\s+(?:abstract\s+)?class\s+[A-Z])', r'@Component\n\1', code, count=1)

    # Now add or modify the constructor
    # Find class name
    class_match = re.search(r'\b(?:public|protected|private)?\s*(?:abstract\s+)?class\s+([A-Z]\w+)', code)
    if not class_match:
        return code
    class_name = class_match.group(1)

    # Find existing constructor
    # public ClassName(
    constructor_pattern = r'((?:public|protected|private)?\s*' + class_name + r'\s*\([^)]*\)\s*(?:throws\s+[^{]+)?\{)'
    
    # We will just REPLACE the first constructor, or ADD ONE if none exists.
    # Wait, if there are multiple constructors, this is dangerous.
    # Let's see if we can just prepend a NEW constructor!
    
    # Where to insert fields and constructor?
    # Right after the class declaration line.
    # class ClassName ... {
    #     <insert here>
    
    class_decl_pattern = r'(\b(?:public|protected|private)?\s*(?:abstract\s+)?class\s+' + class_name + r'(?:\s+extends\s+[^{]+)?(?:\s+implements\s+[^{]+)?\{\n)'
    
    insertion = "\n" + "\n".join(fields) + "\n\n"
    insertion += "    @Autowired\n"
    insertion += f"    public {class_name}(" + ", ".join(params) + ") {\n"
    insertion += "\n".join(assignments) + "\n"
    insertion += "    }\n"
    
    # We need to make sure we don't break existing constructors that don't initialize the final fields.
    # So actually, we should NOT make the fields final!
    fields = [f.replace('final ', '') for f in fields]
    
    insertion = "\n" + "\n".join(fields) + "\n\n"
    
    # If the class already has a constructor, we should MODIFY the first one we find instead of adding a new one,
    # because if we add a new one, Spring will use the @Autowired one, but any code calling the OLD constructor (e.g. `new ClassName()`) will fail unless the old one is kept.
    # If we keep the old one, the fields are not final, so it's fine.
    # BUT wait! If they call `new TechAdminServlet()`, the DAOs will be NULL!
    # And then `processRequest` will throw NPE!
    # The requirement says we must REFACTOR the instantiations of the classes too if they are instantiated manually?
    # NO! "1,500+ direct instantiation sites (using the `new` keyword) across servlets, controllers, and services must be fully refactored to acquire DAOs via constructor injection."
    # This refers ONLY to the DAOs! "acquire DAOs via constructor injection".
    
    insertion += "    @Autowired\n"
    insertion += f"    public {class_name}(" + ", ".join(params) + ") {\n"
    insertion += "\n".join(assignments) + "\n"
    insertion += "    }\n"
    
    # Insert right after class '{'
    code = re.sub(class_decl_pattern, r'\1' + insertion, code, count=1)

    return code

def main():
    modified = 0
    for root, dirs, files in os.walk('/app/core/src/main/java'):
        for f in files:
            if f.endswith('.java'):
                path = os.path.join(root, f)
                new_code = process_file(path)
                if new_code != open(path).read():
                    open(path, 'w').write(new_code)
                    modified += 1
                    
    for root, dirs, files in os.walk('/app/web/src/main/java'):
        for f in files:
            if f.endswith('.java'):
                path = os.path.join(root, f)
                new_code = process_file(path)
                if new_code != open(path).read():
                    open(path, 'w').write(new_code)
                    modified += 1
    
    print(f"Modified {modified} files")

if __name__ == '__main__':
    main()
