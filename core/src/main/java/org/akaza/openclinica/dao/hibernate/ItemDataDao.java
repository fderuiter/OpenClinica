package org.akaza.openclinica.dao.hibernate;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.akaza.openclinica.domain.datamap.ItemData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class ItemDataDao extends AbstractDomainDao<ItemData> {

    Class<ItemData> domainClass() {
        return ItemData.class;
    }

    public ItemData findByItemEventCrfOrdinal(Integer itemId, Integer eventCrfId, Integer ordinal) {
        String query = "from " + getDomainClassName()
                + " item_data where item_data.item.itemId = :itemid and item_data.eventCrf.eventCrfId = :eventcrfid and item_data.ordinal = :ordinal";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("itemid", itemId);
        q.setParameter("eventcrfid", eventCrfId);
        q.setParameter("ordinal", ordinal);
        return (ItemData) q.getResultList().stream().findFirst().orElse(null);
    }

    public List<ItemData> findAllByEventCrf(Integer eventCrfId) {
        String query = "select * from item_data where event_crf_id = " + eventCrfId;
        jakarta.persistence.Query q = getEntityManager().createNativeQuery(query, ItemData.class);
        
        return (List<ItemData>) q.getResultList();
      
    }

    public List<ItemData> findByEventCrfGroup(Integer eventCrfId, Integer itemGroupId) {
        String query = "select id.* " + 
            "from item_data id " + 
            "join item i on id.item_id = i.item_id " + 
            "join event_crf ec on id.event_crf_id=ec.event_crf_id " + 
            "join item_group_metadata igm on i.item_id=igm.item_id and igm.crf_version_id = ec.crf_version_id " + 
            "where id.event_crf_id = " + eventCrfId + " and igm.item_group_id = " + itemGroupId + " " + 
            "order by id.ordinal, igm.ordinal";
        jakarta.persistence.Query q = getEntityManager().createNativeQuery(query, ItemData.class);
        
        return (List<ItemData>) q.getResultList();
      
    }
    
    public List<ItemData> findByEventCrfId(Integer eventCrfId) {
        String query = "from " + getDomainClassName() + " item_data where item_data.eventCrf.eventCrfId = :eventcrfid";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("eventcrfid", eventCrfId);
        return (List<ItemData>) q.getResultList();
      
    }
    
    public int getMaxGroupRepeat(Integer eventCrfId, Integer itemId) {
        String query = "select max(ordinal) from item_data where event_crf_id = " + eventCrfId + " and item_id = " + itemId;
        jakarta.persistence.Query q = getEntityManager().createNativeQuery(query);
        Number result = (Number) q.getResultList().stream().findFirst().orElse(null);
        if (result == null) return 0;
        else return result.intValue();
    }

    public boolean saveOrUpdateFromBean(org.akaza.openclinica.bean.submit.ItemDataBean itemDataBean, org.akaza.openclinica.bean.login.UserAccountBean ub, boolean overwrite) {
        boolean resetSDV = false;
        ItemData idData = findByItemEventCrfOrdinal(itemDataBean.getItemId(), itemDataBean.getEventCRFId(), itemDataBean.getOrdinal());
        if (overwrite && idData != null && idData.getStatus() != null) {
            if (!idData.getValue().equals(itemDataBean.getValue())) {
                resetSDV = true;
            }
            idData.setDateUpdated(new java.util.Date());
            idData.setUpdateId(ub.getId());
            idData.setValue(itemDataBean.getValue());
            if (itemDataBean.getStatus() != null) {
                idData.setStatus((org.akaza.openclinica.domain.Status) getEntityManager().getReference(org.akaza.openclinica.domain.Status.class, itemDataBean.getStatus().getId()));
            }
            saveOrUpdate(idData);
            itemDataBean.setId(idData.getItemDataId());
        } else if (idData == null) {
            resetSDV = true;
            idData = new ItemData();
            idData.setDateCreated(new java.util.Date());
            idData.setItem((org.akaza.openclinica.domain.datamap.Item) getEntityManager().getReference(org.akaza.openclinica.domain.datamap.Item.class, itemDataBean.getItemId()));
            idData.setEventCrf((org.akaza.openclinica.domain.datamap.EventCrf) getEntityManager().getReference(org.akaza.openclinica.domain.datamap.EventCrf.class, itemDataBean.getEventCRFId()));
            idData.setUserAccount((org.akaza.openclinica.domain.user.UserAccount) getEntityManager().getReference(org.akaza.openclinica.domain.user.UserAccount.class, ub.getId()));
            idData.setValue(itemDataBean.getValue());
            idData.setOrdinal(itemDataBean.getOrdinal());
            int statusId = (itemDataBean.getStatus() != null) ? itemDataBean.getStatus().getId() : 1;
            idData.setStatus((org.akaza.openclinica.domain.Status) getEntityManager().getReference(org.akaza.openclinica.domain.Status.class, statusId));
            saveOrUpdate(idData);
            itemDataBean.setId(idData.getItemDataId());
        }
        return resetSDV;
    }
}
