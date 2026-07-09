package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.CrfVersion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.ResponseType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class ResponseTypeDao extends AbstractDomainDao<ResponseType> {

    @Override
    Class<ResponseType> domainClass() {
        // TODO Auto-generated method stub
        return ResponseType.class;
    }

    public ResponseType findByResponseTypeName(String name) {
        String query = "from " + getDomainClassName() + " response_type  where response_type.name = :name ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("name", name);
        return (ResponseType) q.getResultList().stream().findFirst().orElse(null);
    }

    public ResponseType findByItemFormMetaDataId(Integer itemFormMetadataId) {
        String query = "select rt.* from response_type rt, response_set rs, item_form_metadata ifm where ifm.response_set_id=rs.response_set_id"
                + " and rs.response_type_id=rt.response_type_id and ifm.item_form_metadata_id = " + String.valueOf(itemFormMetadataId);
        jakarta.persistence.Query q = getEntityManager().createNativeQuery(query, ResponseType.class);
        return (ResponseType) q.getResultList().stream().findFirst().orElse(null);
    }

}
