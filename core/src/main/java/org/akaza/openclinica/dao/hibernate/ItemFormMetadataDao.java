package org.akaza.openclinica.dao.hibernate;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.akaza.openclinica.domain.datamap.ItemFormMetadata;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Query;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class ItemFormMetadataDao extends AbstractDomainDao<ItemFormMetadata> {

    @Override
    Class<ItemFormMetadata> domainClass() {
        return ItemFormMetadata.class;
    }

    public ItemFormMetadata findByItemCrfVersion(Integer itemId, Integer crfVersionId) {
        String query = "SELECT distinct m.* " + " FROM item_form_metadata m" + " WHERE m.item_id= " + String.valueOf(itemId) + " AND m.crf_version_id= "
                + String.valueOf(crfVersionId);
        Query q = getEntityManager().createNativeQuery(query, ItemFormMetadata.class);
        return (ItemFormMetadata) q.getResultList().stream().findFirst().orElse(null);

    }

    public static final String findAllByCrfVersionQuery = "select distinct * from item_form_metadata ifm where ifm.crf_version_id = :crfversionid";

    @SuppressWarnings("unchecked")
    public List<ItemFormMetadata> findAllByCrfVersion(int crf_version_id) {
        jakarta.persistence.Query q = getEntityManager().createNativeQuery(findAllByCrfVersionQuery, ItemFormMetadata.class);
        q.setParameter("crfversionid", crf_version_id);
        return (List<ItemFormMetadata>) q.getResultList();
    }

}
