package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.CrfVersionMedia;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class CrfVersionMediaDao extends AbstractDomainDao<CrfVersionMedia> {

    @Override
    Class<CrfVersionMedia> domainClass() {
        // TODO Auto-generated method stub
        return CrfVersionMedia.class;
    }

    public ArrayList<CrfVersionMedia> findByCrfVersionId(int crf_version_id) {
        String query = "from " + getDomainClassName() + " crf_version_media  where crf_version_media.crfVersion.crfVersionId = :crfversionid ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("crfversionid", crf_version_id);
        return (ArrayList<CrfVersionMedia>) q.getResultList();
    }
}
