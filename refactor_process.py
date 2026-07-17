import re

with open('/app/core/src/main/java/org/akaza/openclinica/dao/core/EntityDAO.java', 'r') as f:
    code = f.read()

# Replace processResultRows with simple one:
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

# Find the old method
m = re.search(r'public ArrayList processResultRows\(ResultSet rs\) \{\s*// throws SQLException\s*ArrayList al = new ArrayList\(\);\s*HashMap hm;\s*try \{.*?\n    \}\n', code, re.DOTALL)
if m:
    code = code[:m.start()] + new_process_result + code[m.end():]
else:
    print("Could not find processResultRows")
    m2 = re.search(r'public ArrayList processResultRows\(ResultSet rs\) \{', code)
    if m2:
        print("Found processResultRows start, but didn't match full block")

with open('/app/core/src/main/java/org/akaza/openclinica/dao/core/EntityDAO.java', 'w') as f:
    f.write(code)

