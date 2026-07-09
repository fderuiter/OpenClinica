package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.AuditLogEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.Filter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class AuditLogEventDao extends AbstractDomainDao<AuditLogEvent> {

	 @Override
	    public Class<AuditLogEvent> domainClass() {
	        return AuditLogEvent.class;
	    }
	 
	 @SuppressWarnings("unchecked")
	public <T> T findByParam(AuditLogEvent auditLogEvent, String anotherAuditTable){
		   
		   String query = "from " + getDomainClassName();
		   String buildQuery = "";
	       if(auditLogEvent.getEntityId()!=null && auditLogEvent.getAuditTable()!=null && anotherAuditTable==null)
	       {	   buildQuery+= "do.entityId =:entity_id ";
	         	   buildQuery+= " and  do.auditTable =:audit_table order by do.auditId ";
	       }
	       else if(auditLogEvent.getEntityId()!=null && auditLogEvent.getAuditTable()!=null && anotherAuditTable!=null)
	       {
	    	   buildQuery+= "do.entityId =:entity_id ";
         	   buildQuery+= " and ( do.auditTable =:audit_table or do.auditTable =:anotherAuditTable) order by do.auditId ";
	       }
	       if(!buildQuery.isEmpty())
		    query = "from " + getDomainClassName() +  " do  where "+buildQuery;
	       else
	    	   query = "from " + getDomainClassName()  ;
	       jakarta.persistence.Query q = getEntityManager().createQuery(query);
	       if(auditLogEvent.getEntityId()!=null && auditLogEvent.getAuditTable()!=null && anotherAuditTable==null)
	       {  q.setParameter("entity_id", auditLogEvent.getEntityId());
	        	   q.setParameter("audit_table", auditLogEvent.getAuditTable());
	       }
	       else if(auditLogEvent.getEntityId()!=null && auditLogEvent.getAuditTable()!=null && anotherAuditTable!=null)
	       {
	    	   q.setParameter("entity_id", auditLogEvent.getEntityId());
        	   q.setParameter("audit_table", auditLogEvent.getAuditTable());
        	   q.setParameter("anotherAuditTable", anotherAuditTable);
	       }
	        	   return (T) q.getResultList();
	 }
}