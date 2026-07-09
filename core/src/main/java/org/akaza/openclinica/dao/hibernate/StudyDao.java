package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.Study;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;


public class StudyDao extends AbstractDomainDao<Study> {
	
    @Override
    public Class<Study> domainClass() {
        return Study.class;
    }
}
