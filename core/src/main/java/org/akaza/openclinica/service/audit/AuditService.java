package org.akaza.openclinica.service.audit;

import org.akaza.openclinica.bean.admin.AuditEventBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.dao.admin.AuditEventDAO;

import javax.sql.DataSource;
import java.util.Date;

public class AuditService {
    private AuditEventDAO auditEventDAO;

    public AuditService(DataSource dataSource) {
        this.auditEventDAO = new AuditEventDAO(dataSource);
    }
    
    public AuditService(AuditEventDAO auditEventDAO) {
        this.auditEventDAO = auditEventDAO;
    }

    /**
     * Unified method to log an audit event and optionally link it to a discrepancy/justification note.
     * This centralizes the audit log creation and ensures the ID is properly linked.
     */
    public AuditEventBean logEvent(AuditEventBean auditEvent, DiscrepancyNoteBean note) {
        if (auditEvent.getAuditDate() == null) {
            auditEvent.setAuditDate(new Date());
        }
        
        AuditEventBean createdEvent = (AuditEventBean) auditEventDAO.create(auditEvent);
        
        if (note != null && note.getId() > 0 && createdEvent.getId() > 0) {
            auditEventDAO.createAuditEventDiscrepancyNoteLink(createdEvent.getId(), note.getId());
        }
        
        return createdEvent;
    }
}
