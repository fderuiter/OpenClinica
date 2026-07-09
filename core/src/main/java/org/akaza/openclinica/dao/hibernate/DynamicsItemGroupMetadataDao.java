package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.bean.submit.EventCRFBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.dao.core.CoreResources;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.crfdata.DynamicsItemGroupMetadataBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class DynamicsItemGroupMetadataDao extends AbstractDomainDao<DynamicsItemGroupMetadataBean>{

    @Override 
    public Class<DynamicsItemGroupMetadataBean> domainClass() {
        return DynamicsItemGroupMetadataBean.class;
    }
    
    public DynamicsItemGroupMetadataBean findByMetadataBean(ItemGroupMetadataBean metadataBean, EventCRFBean eventCrfBean) {
        String query =
            "from " + getDomainClassName()
                + " metadata where metadata.itemGroupMetadataId = :id and metadata.itemGroupId = :item_group_id and metadata.eventCrfId = :event_crf_id ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("id", new Integer(metadataBean.getId()));
        q.setParameter("item_group_id", new Integer(metadataBean.getItemGroupId()));
        q.setParameter("event_crf_id", new Integer(eventCrfBean.getId()));
        return (DynamicsItemGroupMetadataBean) q.getResultList().stream().findFirst().orElse(null);
    }
    
    public DynamicsItemGroupMetadataBean findByMetadataBean(ItemGroupMetadataBean metadataBean, int eventCrfBeanId) {
        String query =
            "from " + getDomainClassName()
                + " metadata where metadata.itemGroupMetadataId = :id and metadata.itemGroupId = :item_group_id and metadata.eventCrfId = :event_crf_id ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("id", new Integer(metadataBean.getId()));
        q.setParameter("item_group_id", new Integer(metadataBean.getItemGroupId()));
        q.setParameter("event_crf_id", new Integer(eventCrfBeanId));
        return (DynamicsItemGroupMetadataBean) q.getResultList().stream().findFirst().orElse(null);
    }
    
    public Boolean hasShowingInSection(int sectionId, int crfVersionId, int eventCrfId) {
        String query = "";
        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            query = "select dg.item_group_id from dyn_item_group_metadata dg where dg.event_crf_id = :eventCrfId and dg.item_group_metadata_id in ("
                + " select distinct igm.item_group_metadata_id from item_group_metadata igm where igm.crf_version_id = :crfVersionId"
                + " and igm.show_group = 0"
                + " and igm.item_id in (select im.item_id from item_form_metadata im where im.section_id = :sectionId and im.crf_version_id = :crfVersionId))"
                + " and dg.show_group = 1 and rownum = 1";
        } else {
        query = "select dg.item_group_id from dyn_item_group_metadata dg where dg.event_crf_id = :eventCrfId and dg.item_group_metadata_id in ("
                + " select distinct igm.item_group_metadata_id from item_group_metadata igm where igm.crf_version_id = :crfVersionId"
                + " and igm.show_group = 'false'"
                + " and igm.item_id in (select im.item_id from item_form_metadata im where im.section_id = :sectionId and im.crf_version_id = :crfVersionId))"
                + " and dg.show_group = 'true' limit 1";
        }
        
        jakarta.persistence.Query q = this.getEntityManager().createNativeQuery(query);
        q.setParameter("eventCrfId", eventCrfId);
        q.setParameter("crfVersionId", crfVersionId);
        q.setParameter("sectionId", sectionId);
        q.setParameter("crfVersionId", crfVersionId);
        return q.getResultList() != null && q.getResultList().size() > 0;
    }
    public  void delete(int eventCrfId){
        String query = " delete from " + getDomainClassName() +  "  where eventCrfId =:eventCrfId ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("eventCrfId", eventCrfId);
        q.executeUpdate();
    }

}
