package org.akaza.openclinica.dao.hibernate;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.akaza.openclinica.domain.datamap.ResponseSet;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class ResponseSetDao extends AbstractDomainDao<ResponseSet> {

    @Override
    Class<ResponseSet> domainClass() {
        // TODO Auto-generated method stub
        return ResponseSet.class;
    }

    public ResponseSet findByLabelVersion(String label, Integer version) {
        String query = "from " + getDomainClassName() + " response_set  where response_set.label = :label and response_set.versionId = :version ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("label", label);
        q.setParameter("version", version);
        return (ResponseSet) q.getResultList().stream().findFirst().orElse(null);
    }

    public List<ResponseSet> findAllByItemId(int itemId) {
        String query = "select rs.* from item_form_metadata ifm join response_set rs on ifm.response_set_id = rs.response_set_id " + "where ifm.item_id = "
                + itemId;
        jakarta.persistence.Query q = getEntityManager().createNativeQuery(query, ResponseSet.class);
        return ((List<ResponseSet>) q.getResultList());
    }

}
