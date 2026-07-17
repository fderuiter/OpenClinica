import os
import re

path = '/app/core/src/main/java/org/akaza/openclinica/bean/extract/DownloadDiscrepancyNote.java'
with open(path, 'r') as f:
    code = f.read()

imports = """
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
"""
code = re.sub(r'(package\s+[^;]+;\n+)', r'\1' + imports, code, count=1)
code = re.sub(r'(public class DownloadDiscrepancyNote)', r'@Component\n\1', code, count=1)

fields = """
    private DiscrepancyNoteDAO _discrepancyNoteDAO;
    private EventCRFDAO _eventCRFDAO;
    private EventDefinitionCRFDAO _eventDefinitionCRFDAO;
    private StudyDAO _studyDAO;
    private StudySubjectDAO _studySubjectDAO;
"""
code = code.replace("public class DownloadDiscrepancyNote implements DownLoadBean{", "public class DownloadDiscrepancyNote implements DownLoadBean{" + fields)

constructor = """
    @Autowired
    public DownloadDiscrepancyNote(DiscrepancyNoteDAO discrepancyNoteDAO, EventCRFDAO eventCRFDAO, EventDefinitionCRFDAO eventDefinitionCRFDAO, StudyDAO studyDAO, StudySubjectDAO studySubjectDAO) {
        this._discrepancyNoteDAO = discrepancyNoteDAO;
        this._eventCRFDAO = eventCRFDAO;
        this._eventDefinitionCRFDAO = eventDefinitionCRFDAO;
        this._studyDAO = studyDAO;
        this._studySubjectDAO = studySubjectDAO;
        this.firstColumnHeaderLine = false;
    }
"""
code = re.sub(r'public DownloadDiscrepancyNote\(\) \{\s*this\.firstColumnHeaderLine = false;\s*\}', constructor, code)

code = code.replace("new DiscrepancyNoteUtil()", "new DiscrepancyNoteUtil(_discrepancyNoteDAO, _eventCRFDAO, _eventDefinitionCRFDAO, _studyDAO, _studySubjectDAO)")

with open(path, 'w') as f:
    f.write(code)

print("Fixed DownloadDiscrepancyNote")
