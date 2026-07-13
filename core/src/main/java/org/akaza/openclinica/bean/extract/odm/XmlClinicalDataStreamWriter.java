package org.akaza.openclinica.bean.extract.odm;

import org.akaza.openclinica.bean.odmbeans.*;
import org.akaza.openclinica.bean.submit.crfdata.*;
import javax.xml.stream.*;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class XmlClinicalDataStreamWriter implements ClinicalDataStreamWriter {
    private final XMLStreamWriter writer;
    private final String ODMVersion;

    public XmlClinicalDataStreamWriter(OutputStream os, String odmVersion) throws Exception {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        this.writer = factory.createXMLStreamWriter(os, "UTF-8");
        this.ODMVersion = odmVersion;
    }

    @Override
    public void writeStartDocument(String studyOID, String metaDataVersionOID, String metadataXml) throws Exception {
        // Echo metadataXml to writer
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(metadataXml));
        
        while (reader.hasNext()) {
            int event = reader.next();
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    writer.writeStartElement(reader.getPrefix() == null ? "" : reader.getPrefix(), reader.getLocalName(), reader.getNamespaceURI() == null ? "" : reader.getNamespaceURI());
                    for (int i = 0; i < reader.getNamespaceCount(); i++) {
                        writer.writeNamespace(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
                    }
                    for (int i = 0; i < reader.getAttributeCount(); i++) {
                        writer.writeAttribute(reader.getAttributePrefix(i) == null ? "" : reader.getAttributePrefix(i), reader.getAttributeNamespace(i) == null ? "" : reader.getAttributeNamespace(i), reader.getAttributeLocalName(i), reader.getAttributeValue(i));
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if ("ODM".equals(reader.getLocalName())) {
                        // Do NOT write the end element for ODM yet! We will append ClinicalData inside it!
                    } else {
                        writer.writeEndElement();
                    }
                    break;
                case XMLStreamConstants.CHARACTERS:
                    writer.writeCharacters(reader.getText());
                    break;
                case XMLStreamConstants.CDATA:
                    writer.writeCData(reader.getText());
                    break;
            }
        }
        reader.close();

        // Now write Start for ClinicalData
        writer.writeStartElement("ClinicalData");
        writer.writeAttribute("StudyOID", studyOID == null ? "" : studyOID);
        writer.writeAttribute("MetaDataVersionOID", metaDataVersionOID == null ? "" : metaDataVersionOID);
    }

    @Override
    public void writeSubjectData(ExportSubjectDataBean sub) throws Exception {
        writer.writeStartElement("SubjectData");
        writer.writeAttribute("SubjectKey", sub.getSubjectOID() == null ? "" : sub.getSubjectOID());
        
        if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
            if (sub.getStudySubjectId() != null) writer.writeAttribute("OpenClinica:StudySubjectID", sub.getStudySubjectId());
            if (sub.getUniqueIdentifier() != null && sub.getUniqueIdentifier().length() > 0)
                writer.writeAttribute("OpenClinica:UniqueIdentifier", sub.getUniqueIdentifier());
            if (sub.getStatus() != null && sub.getStatus().length() > 0)
                writer.writeAttribute("OpenClinica:Status", sub.getStatus());
            if (sub.getSecondaryId() != null && sub.getSecondaryId().length() > 0)
                writer.writeAttribute("OpenClinica:SecondaryID", sub.getSecondaryId());
            
            if (sub.getYearOfBirth() != null) {
                writer.writeAttribute("OpenClinica:YearOfBirth", String.valueOf(sub.getYearOfBirth()));
            } else if (sub.getDateOfBirth() != null) {
                writer.writeAttribute("OpenClinica:DateOfBirth", sub.getDateOfBirth());
            }
            if (sub.getSubjectGender() != null && sub.getSubjectGender().length() > 0)
                writer.writeAttribute("OpenClinica:Sex", sub.getSubjectGender());
            if (sub.getEnrollmentDate() != null && sub.getEnrollmentDate().length() > 0)
                writer.writeAttribute("OpenClinica:EnrollmentDate", sub.getEnrollmentDate());
        }

        ArrayList<ExportStudyEventDataBean> ses = (ArrayList<ExportStudyEventDataBean>) sub.getExportStudyEventData();
        if (ses != null) {
            for (ExportStudyEventDataBean se : ses) {
                writeStudyEventData(se);
            }
        }

        if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
            ArrayList<SubjectGroupDataBean> sgddata = (ArrayList<SubjectGroupDataBean>) sub.getSubjectGroupData();
            if (sgddata != null && sgddata.size() > 0) {
                for (SubjectGroupDataBean sgd : sgddata) {
                    writer.writeEmptyElement("OpenClinica:SubjectGroupData");
                    if (sgd.getStudyGroupClassId() != null)
                        writer.writeAttribute("OpenClinica:StudyGroupClassID", sgd.getStudyGroupClassId());
                    if (sgd.getStudyGroupClassName() != null)
                        writer.writeAttribute("OpenClinica:StudyGroupClassName", sgd.getStudyGroupClassName());
                    if (sgd.getStudyGroupName() != null)
                        writer.writeAttribute("OpenClinica:StudyGroupName", sgd.getStudyGroupName());
                }
            }
            addAuditLogs(sub.getAuditLogs(), "sub");
            addDiscrepancyNotes(sub.getDiscrepancyNotes());
        }

        writer.writeEndElement(); // SubjectData
        writer.flush();
    }

    private void writeStudyEventData(ExportStudyEventDataBean se) throws Exception {
        writer.writeStartElement("StudyEventData");
        writer.writeAttribute("StudyEventOID", se.getStudyEventOID() == null ? "" : se.getStudyEventOID());

        if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
            if (se.getLocation() != null && se.getLocation().length() > 0)
                writer.writeAttribute("OpenClinica:StudyEventLocation", se.getLocation());
            if (se.getStartDate() != null && se.getStartDate().length() > 0)
                writer.writeAttribute("OpenClinica:StartDate", se.getStartDate());
            if (se.getEndDate() != null && se.getEndDate().length() > 0)
                writer.writeAttribute("OpenClinica:EndDate", se.getEndDate());
            if (se.getStatus() != null && se.getStatus().length() > 0)
                writer.writeAttribute("OpenClinica:Status", se.getStatus());
            if (se.getAgeAtEvent() != null)
                writer.writeAttribute("OpenClinica:SubjectAgeAtEvent", se.getAgeAtEvent().toString());
            if ("Yes".equalsIgnoreCase(se.getLocked()))
                writer.writeAttribute("OpenClinica:Locked", "Yes");
            if ("Yes".equalsIgnoreCase(se.getSigned()))
                writer.writeAttribute("OpenClinica:Signed", "Yes");
            if ("Yes".equalsIgnoreCase(se.getStopped()))
                writer.writeAttribute("OpenClinica:Stopped", "Yes");
        }

        if (se.getStudyEventRepeatKey() != null && !"-1".equals(se.getStudyEventRepeatKey())) {
            writer.writeAttribute("StudyEventRepeatKey", se.getStudyEventRepeatKey());
        }

        ArrayList<ExportFormDataBean> forms = se.getExportFormData();
        if (forms != null) {
            for (ExportFormDataBean form : forms) {
                writeFormData(form);
            }
        }

        if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
            addAuditLogs(se.getAuditLogs(), "se");
            addDiscrepancyNotes(se.getDiscrepancyNotes());
        }

        writer.writeEndElement(); // StudyEventData
    }

    private void writeFormData(ExportFormDataBean form) throws Exception {
        writer.writeStartElement("FormData");
        writer.writeAttribute("FormOID", form.getFormOID() == null ? "" : form.getFormOID());

        if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
            if (form.getCrfVersion() != null && form.getCrfVersion().length() > 0)
                writer.writeAttribute("OpenClinica:Version", form.getCrfVersion());
            if (form.getInterviewerName() != null && form.getInterviewerName().length() > 0)
                writer.writeAttribute("OpenClinica:InterviewerName", form.getInterviewerName());
            if (form.getInterviewDate() != null && form.getInterviewDate().length() > 0)
                writer.writeAttribute("OpenClinica:InterviewDate", form.getInterviewDate());
            if (form.getStatus() != null && form.getStatus().length() > 0)
                writer.writeAttribute("OpenClinica:Status", form.getStatus());
            if ("Yes".equalsIgnoreCase(form.getLocked()))
                writer.writeAttribute("OpenClinica:Locked", "Yes");
            if ("Yes".equalsIgnoreCase(form.getSigned()))
                writer.writeAttribute("OpenClinica:Signed", "Yes");
            if ("Yes".equalsIgnoreCase(form.getStopped()))
                writer.writeAttribute("OpenClinica:Stopped", "Yes");
        }

        ArrayList<ImportItemGroupDataBean> igs = form.getItemGroupData();
        if (igs != null) {
            sortImportItemGroupDataBeanList(igs);
            for (ImportItemGroupDataBean ig : igs) {
                writeItemGroupData(ig);
            }
        }

        if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
            addAuditLogs(form.getAuditLogs(), "form");
            addDiscrepancyNotes(form.getDiscrepancyNotes());
        }

        writer.writeEndElement(); // FormData
    }

    private void writeItemGroupData(ImportItemGroupDataBean ig) throws Exception {
        writer.writeStartElement("ItemGroupData");
        writer.writeAttribute("ItemGroupOID", ig.getItemGroupOID() == null ? "" : ig.getItemGroupOID());
        if (ig.getItemGroupRepeatKey() != null && !"-1".equals(ig.getItemGroupRepeatKey())) {
            writer.writeAttribute("ItemGroupRepeatKey", ig.getItemGroupRepeatKey());
        }
        writer.writeAttribute("TransactionType", "Insert");

        ArrayList<ImportItemDataBean> items = ig.getItemData();
        if (items != null) {
            sortImportItemDataBeanList(items);
            for (ImportItemDataBean item : items) {
                writeItemData(item);
            }
        }
        writer.writeEndElement(); // ItemGroupData
    }

    private void writeItemData(ImportItemDataBean item) throws Exception {
        boolean printValue = true;
        boolean isNull = "Yes".equals(item.getIsNull());
        if (isNull && !item.isHasValueWithNull()) {
            printValue = false;
        }

        boolean hasMeasurementUnit = printValue && item.getMeasurementUnitRef() != null && item.getMeasurementUnitRef().getElementDefOID() != null && item.getMeasurementUnitRef().getElementDefOID().length() > 0;
        boolean hasAuditLogs = printValue && ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) && item.getAuditLogs() != null && item.getAuditLogs().getAuditLogs() != null && item.getAuditLogs().getAuditLogs().size() > 0;
        boolean hasDiscrepancyNotes = printValue && ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) && item.getDiscrepancyNotes() != null && item.getDiscrepancyNotes().getDiscrepancyNotes() != null && item.getDiscrepancyNotes().getDiscrepancyNotes().size() > 0;
        
        boolean hasChildren = hasMeasurementUnit || hasAuditLogs || hasDiscrepancyNotes;

        if (!printValue && !hasChildren && ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) && item.getReasonForNull() != null) {
            writer.writeEmptyElement("ItemData");
        } else if (!hasChildren) {
            writer.writeEmptyElement("ItemData");
        } else {
            writer.writeStartElement("ItemData");
        }

        writer.writeAttribute("ItemOID", item.getItemOID() == null ? "" : item.getItemOID());
        
        if (isNull) {
            writer.writeAttribute("IsNull", "Yes");
            if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
                if (item.getReasonForNull() != null) {
                    writer.writeAttribute("OpenClinica:ReasonForNull", item.getReasonForNull());
                }
            }
        }

        if (printValue) {
            writer.writeAttribute("Value", item.getValue() == null ? "" : item.getValue());
            
            if (hasMeasurementUnit) {
                writer.writeEmptyElement("MeasurementUnitRef");
                writer.writeAttribute("MeasurementUnitOID", item.getMeasurementUnitRef().getElementDefOID());
            }

            if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
                addAuditLogs(item.getAuditLogs(), "item");
                addDiscrepancyNotes(item.getDiscrepancyNotes());
            }
        }

        if (hasChildren) {
            writer.writeEndElement(); // ItemData
        }
    }

    private void addAuditLogs(AuditLogsBean auditLogs, String entity) throws Exception {
        if (auditLogs == null) return;
        ArrayList<AuditLogBean> audits = auditLogs.getAuditLogs();
        if (audits == null || audits.isEmpty()) return;

        int count = 0;
        for (AuditLogBean audit : audits) {
            if ("item".equals(entity) && "".equals(audit.getOldValue()) && "".equals(audit.getNewValue())) {
                count++;
            }
        }
        if (count == audits.size()) return;

        writer.writeStartElement("OpenClinica:AuditLogs");
        if (auditLogs.getEntityID() != null) {
            writer.writeAttribute("EntityID", auditLogs.getEntityID());
        }
        
        for (AuditLogBean audit : audits) {
            if ("item".equals(entity) && "".equals(audit.getOldValue()) && "".equals(audit.getNewValue())) continue;
            
            writer.writeEmptyElement("OpenClinica:AuditLog");
            if (audit.getOid() != null && audit.getOid().length() > 0) writer.writeAttribute("ID", audit.getOid());
            if (audit.getUserId() != null && audit.getUserId().length() > 0) writer.writeAttribute("UserID", audit.getUserId());
            if (audit.getUserName() != null && audit.getUserName().length() > 0) writer.writeAttribute("UserName", audit.getUserName());
            if (audit.getName() != null && audit.getName().length() > 0) writer.writeAttribute("Name", audit.getName());
            if (audit.getDatetimeStamp() != null) writer.writeAttribute("DateTimeStamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(audit.getDatetimeStamp()));
            if (audit.getType() != null && audit.getType().length() > 0) writer.writeAttribute("AuditType", audit.getType());
            if (audit.getReasonForChange() != null && audit.getReasonForChange().length() > 0) writer.writeAttribute("ReasonForChange", audit.getReasonForChange());
            if (audit.getOldValue() != null && audit.getOldValue().length() > 0) writer.writeAttribute("OldValue", audit.getOldValue());
            if (audit.getNewValue() != null && audit.getNewValue().length() > 0) writer.writeAttribute("NewValue", audit.getNewValue());
            if (audit.getValueType() != null && audit.getValueType().length() > 0) writer.writeAttribute("ValueType", audit.getValueType());
        }
        writer.writeEndElement();
    }

    private void addDiscrepancyNotes(DiscrepancyNotesBean DNs) throws Exception {
        if (DNs == null) return;
        ArrayList<DiscrepancyNoteBean> dns = DNs.getDiscrepancyNotes();
        if (dns == null || dns.isEmpty()) return;

        writer.writeStartElement("OpenClinica:DiscrepancyNotes");
        if (DNs.getEntityID() != null) {
            writer.writeAttribute("EntityID", DNs.getEntityID());
        }

        for (DiscrepancyNoteBean dn : dns) {
            writer.writeStartElement("OpenClinica:DiscrepancyNote");
            if (dn.getOid() != null && dn.getOid().length() > 0) writer.writeAttribute("ID", dn.getOid());
            if (dn.getStatus() != null && dn.getStatus().length() > 0) writer.writeAttribute("Status", dn.getStatus());
            if (dn.getNoteType() != null && dn.getNoteType().length() > 0) writer.writeAttribute("NoteType", dn.getNoteType());
            if (dn.getDateUpdated() != null) writer.writeAttribute("DateUpdated", new SimpleDateFormat("yyyy-MM-dd").format(dn.getDateUpdated()));
            if (dn.getEntityName() != null && dn.getEntityName().length() > 0) writer.writeAttribute("EntityName", dn.getEntityName());
            if (dn.getNumberOfChildNotes() > 0) writer.writeAttribute("NumberOfChildNotes", String.valueOf(dn.getNumberOfChildNotes()));

            if (dn.getChildNotes() != null && !dn.getChildNotes().isEmpty()) {
                for (ChildNoteBean cn : dn.getChildNotes()) {
                    writer.writeStartElement("OpenClinica:ChildNote");
                    if (cn.getOid() != null && cn.getOid().length() > 0) writer.writeAttribute("ID", cn.getOid());
                    if (cn.getStatus() != null && cn.getStatus().length() > 0) writer.writeAttribute("Status", cn.getStatus());
                    if (cn.getDateCreated() != null) writer.writeAttribute("DateCreated", new SimpleDateFormat("yyyy-MM-dd").format(cn.getDateCreated()));
                    if (cn.getOwnerUserName() != null && cn.getOwnerUserName().length() > 0) writer.writeAttribute("UserName", cn.getOwnerUserName());
                    
                    String ownerName = "";
                    if (cn.getOwnerFirstName() != null) ownerName += cn.getOwnerFirstName() + " ";
                    if (cn.getOwnerLastName() != null) ownerName += cn.getOwnerLastName();
                    ownerName = ownerName.trim();
                    if (ownerName.length() > 0) writer.writeAttribute("Name", ownerName);

                    if (cn.getDescription() != null && cn.getDescription().length() > 0) {
                        writer.writeStartElement("OpenClinica:Description");
                        writer.writeCharacters(cn.getDescription());
                        writer.writeEndElement();
                    }
                    if (cn.getDetailedNote() != null && cn.getDetailedNote().length() > 0) {
                        writer.writeStartElement("OpenClinica:DetailedNote");
                        writer.writeCharacters(cn.getDetailedNote());
                        writer.writeEndElement();
                    }
                    if (cn.getUserRef() != null && cn.getUserRef().getElementDefOID() != null && cn.getUserRef().getElementDefOID().length() > 0) {
                        writer.writeEmptyElement("UserRef");
                        writer.writeAttribute("UserOID", cn.getUserRef().getElementDefOID());
                        if (cn.getUserRef().getUserName() != null && cn.getUserRef().getUserName().length() > 0)
                            writer.writeAttribute("OpenClinica:UserName", cn.getUserRef().getUserName());
                        if (cn.getUserRef().getFullName() != null && cn.getUserRef().getFullName().length() > 0)
                            writer.writeAttribute("OpenClinica:FullName", cn.getUserRef().getFullName());
                    }
                    writer.writeEndElement(); // OpenClinica:ChildNote
                }
            }
            writer.writeEndElement(); // OpenClinica:DiscrepancyNote
        }
        writer.writeEndElement(); // OpenClinica:DiscrepancyNotes
    }

    @Override
    public void writeEndDocument() throws Exception {
        writer.writeEndElement(); // ClinicalData
        writer.writeEndElement(); // ODM
        writer.writeEndDocument();
        writer.flush();
    }

    @Override
    public void close() throws Exception {
        writer.close();
    }

    private void sortImportItemGroupDataBeanList(ArrayList<ImportItemGroupDataBean> igs) {
        Collections.sort(igs, new Comparator<ImportItemGroupDataBean>() {
            public int compare(ImportItemGroupDataBean o1, ImportItemGroupDataBean o2) {
                String x1 = o1.getItemGroupOID();
                String x2 = o2.getItemGroupOID();
                if (x1 == null) x1 = "";
                if (x2 == null) x2 = "";
                int sComp = x1.compareTo(x2);
                if (sComp != 0) return sComp;
                Integer i1 = 0;
                Integer i2 = 0;
                try { i1 = Integer.valueOf(o1.getItemGroupRepeatKey()); } catch (Exception e) {}
                try { i2 = Integer.valueOf(o2.getItemGroupRepeatKey()); } catch (Exception e) {}
                return i1.compareTo(i2);
            }
        });
    }

    private void sortImportItemDataBeanList(ArrayList<ImportItemDataBean> items) {
        Collections.sort(items, new Comparator<ImportItemDataBean>() {
            public int compare(ImportItemDataBean o1, ImportItemDataBean o2) {
                String i1 = o1.getItemOID();
                String i2 = o2.getItemOID();
                if (i1 == null) i1 = "";
                if (i2 == null) i2 = "";
                return i1.compareTo(i2);
            }
        });
    }
}
