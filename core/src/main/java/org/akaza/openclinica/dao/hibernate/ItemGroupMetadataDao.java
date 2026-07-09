package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.akaza.openclinica.domain.datamap.ItemGroupMetadata;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Query;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class ItemGroupMetadataDao extends AbstractDomainDao<ItemGroupMetadata> {

    @Override
    Class<ItemGroupMetadata> domainClass() {
        // TODO Auto-generated method stub
        return ItemGroupMetadata.class;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<ItemGroupMetadata> findByItemGroupCrfVersion(Integer itemGroupId, Integer crfVersionId) {
        String query = "select distinct igm.* from item_group_metadata igm, item_group ig where igm.crf_version_id = " + String.valueOf(crfVersionId)
                + " and ig.item_group_id = igm.item_group_id and ig.item_group_id = " + String.valueOf(itemGroupId) + " order by igm.ordinal asc";
        jakarta.persistence.Query q = getEntityManager().createNativeQuery(query, ItemGroupMetadata.class);
        return (ArrayList<ItemGroupMetadata>) q.getResultList();
    }

    public ItemGroupMetadata findByItemCrfVersion(int item_id, int crf_version_id) {
        String query = "from " + getDomainClassName() + " do where do.item.itemId = :itemid and do.crfVersion.crfVersionId = :crfversionid";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("itemid", item_id);
        q.setParameter("crfversionid", crf_version_id);
        return (ItemGroupMetadata) q.getResultList().stream().findFirst().orElse(null);
    }

    public static final String findAllByCrfVersionQuery = "select distinct * from item_group_metadata igm where igm.crf_version_id = :crfversionid";

    @SuppressWarnings("unchecked")
    public List<ItemGroupMetadata> findAllByCrfVersion(int crf_version_id) {
        Query q = getEntityManager().createNativeQuery(findAllByCrfVersionQuery, ItemGroupMetadata.class);
        q.setParameter("crfversionid", crf_version_id);
        return (List<ItemGroupMetadata>) q.getResultList();
    }
}
