package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.VersioningMap;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class VersioningMapDao extends AbstractDomainDao<VersioningMap> {

    @Override
    Class<VersioningMap> domainClass() {
        // TODO Auto-generated method stub
        return VersioningMap.class;
    }

}
