package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.AuditEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;


public class AuditEventDao extends AbstractDomainDao<AuditEvent> {

	 @Override
	    public Class<AuditEvent> domainClass() {
	        return AuditEvent.class;
	    }
}
