package org.akaza.openclinica.dao.hibernate;

import org.hibernate.Criteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public interface CriteriaCommand {

    public Criteria execute(Criteria criteria);

}
