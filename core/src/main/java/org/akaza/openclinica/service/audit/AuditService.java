package org.akaza.openclinica.service.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.akaza.openclinica.bean.admin.AuditEventBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.dao.admin.AuditEventDAO;

import javax.sql.DataSource;
import java.util.Date;

@Component
public class AuditService {
    private AuditEventDAO _auditEventDAO;

    private AuditEventDAO auditEventDAO;

    @Autowired
    public AuditService(DataSource dataSource, AuditEventDAO _auditEventDAO) {
        this._auditEventDAO = _auditEventDAO;

        this.auditEventDAO = this._auditEventDAO;
    }
    
    public AuditService(AuditEventDAO auditEventDAO) {
        this.auditEventDAO = auditEventDAO;
    }

    /**
     * Unified method to log an audit event and optionally link it to a discrepancy/justification note.
     * This centralizes the audit log creation and ensures the ID is properly linked.
     */
    
    public void setBatchingEnabled(boolean enabled) {
        auditEventDAO.setBatchingEnabled(enabled);
    }

    public void flushBatch() {
        auditEventDAO.flushBatch();
    }

    public AuditEventBean logEvent(AuditEventBean auditEvent, DiscrepancyNoteBean note) {
        if (auditEvent.getAuditDate() == null) {
            auditEvent.setAuditDate(new Date());
        }
        
        AuditEventBean createdEvent = (AuditEventBean) auditEventDAO.create(auditEvent);
        
        if (note != null && note.getId() > 0) {
            auditEventDAO.createAuditEventDiscrepancyNoteLink(createdEvent.getId(), note.getId());
        }
        
        return createdEvent;
    }
}
