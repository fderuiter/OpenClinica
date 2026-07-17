import os
path = '/app/core/src/main/java/org/akaza/openclinica/repository/UnifiedRepository.java'
with open(path, 'r') as f:
    code = f.read()

code = code.replace('package org.akaza.openclinica.repository;', '''package org.akaza.openclinica.repository;

import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
''')
with open(path, 'w') as f:
    f.write(code)

path2 = '/app/core/src/main/java/org/akaza/openclinica/service/subject/SubjectService.java'
with open(path2, 'r') as f:
    code2 = f.read()
code2 = code2.replace('package org.akaza.openclinica.service.subject;', '''package org.akaza.openclinica.service.subject;

import org.akaza.openclinica.dao.submit.SubjectDAO;
''')
with open(path2, 'w') as f:
    f.write(code2)

