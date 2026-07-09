package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.akaza.openclinica.bean.submit.EventCRFBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.dao.core.CoreResources;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.crfdata.DynamicsItemFormMetadataBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.apache.commons.collections.CollectionUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Query;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.slf4j.LoggerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

/**
 *
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class DynamicsItemFormMetadataDao extends AbstractDomainDao<DynamicsItemFormMetadataBean> {

    protected static final Logger LOG = LoggerFactory.getLogger(DynamicsItemFormMetadataDao.class);

    @Override
    public Class<DynamicsItemFormMetadataBean> domainClass() {
        return DynamicsItemFormMetadataBean.class;
    }

    public DynamicsItemFormMetadataBean findByMetadataBean(ItemFormMetadataBean metadataBean, EventCRFBean eventCrfBean,
            ItemDataBean itemDataBean) {

        String query = "from DynamicsItemFormMetadataBean metadata where " +
        		"metadata.itemId = :item_id and metadata.eventCrfId = :event_crf_id and " +
                "metadata.itemDataId = :item_data_id order by metadata.id desc ";

        Query q = getEntityManager().createQuery(query);
        q.setParameter("item_id", new Integer(metadataBean.getItemId()));
        q.setParameter("event_crf_id", new Integer(eventCrfBean.getId()));
        q.setParameter("item_data_id", new Integer(itemDataBean.getId()));
        ArrayList <DynamicsItemFormMetadataBean> list = (ArrayList<DynamicsItemFormMetadataBean>) q.getResultList();
        return list.size() !=0 ? list.get(0) : null;
    }

    
    public ArrayList <DynamicsItemFormMetadataBean> findByItemAndEventCrfShown(EventCRFBean eventCrfBean,
            int itemId) {

        String query = "from DynamicsItemFormMetadataBean metadata where " +
                "metadata.itemId = :item_id and metadata.eventCrfId = :event_crf_id and " +
                "metadata.showItem = true order by metadata.id desc ";

        Query q = getEntityManager().createQuery(query);
        q.setParameter("item_id", itemId);
        q.setParameter("event_crf_id", new Integer(eventCrfBean.getId()));
        ArrayList <DynamicsItemFormMetadataBean> list = (ArrayList<DynamicsItemFormMetadataBean>) q.getResultList();
        return list;
    }

    
    public DynamicsItemFormMetadataBean findByItemDataBean(ItemDataBean itemDataBean) {
        String query = "from " + getDomainClassName() + " metadata where metadata.itemDataId = :item_data_id ";
        Query q = getEntityManager().createQuery(query);

        q.setParameter("item_data_id", new Integer(itemDataBean.getId()));
        return (DynamicsItemFormMetadataBean) q.getResultList().stream().findFirst().orElse(null);
    }

        
    public List<Integer> findItemIdsForAGroupInSection(int groupId, int sectionId, int crfVersionId, int eventCrfId) {
        String oracle = "select distinct ditem.item_id from dyn_item_form_metadata ditem"
            + " where ditem.item_data_id in (select idata.item_data_id from item_data idata"
            + " where idata.event_crf_id = :eventCrfId and idata.item_id in ("
            + " select distinct igm.item_id from item_group_metadata igm"
            + " where igm.item_group_id = :groupId and igm.crf_version_id = :crfVersionId and igm.item_id in ("
            + " select ifm.item_id from item_form_metadata ifm where ifm.show_item=0 and ifm.section_id = :sectionId"
            + " and ifm.crf_version_id = :crfVersionId))"
            + " and (idata.status_id != 5 and idata.status_id != 7) )";

        String postgres = "select distinct ditem.item_id from dyn_item_form_metadata ditem"
            + " where ditem.item_data_id in (select idata.item_data_id from item_data idata"
            + " where idata.event_crf_id = :eventCrfId and idata.item_id in ("
            + " select distinct igm.item_id from item_group_metadata igm"
            + " where igm.item_group_id = :groupId and igm.crf_version_id = :crfVersionId and igm.item_id in ("
            + " select ifm.item_id from item_form_metadata ifm where ifm.show_item='false' and ifm.section_id = :sectionId"
            + " and ifm.crf_version_id = :crfVersionId))"
            + " and (idata.status_id != 5 and idata.status_id != 7) )";

        return queryForIDs(oracle, postgres, groupId, sectionId, eventCrfId, crfVersionId);
    }

    public List<Integer> findShowItemIdsForAGroupInSection(int groupId, int sectionId, int crfVersionId, int eventCrfId) {
        String oracle = "select distinct ditem.item_id from dyn_item_form_metadata ditem"
            + " where ditem.item_data_id in (select idata.item_data_id from item_data idata"
            + " where idata.event_crf_id = :eventCrfId and idata.item_id in ("
            + " select distinct igm.item_id from item_group_metadata igm"
            + " where igm.item_group_id = :groupId and igm.crf_version_id = :crfVersionId and igm.item_id in ("
            + " select ifm.item_id from item_form_metadata ifm where ifm.show_item=0 and ifm.section_id = :sectionId"
            + " and ifm.crf_version_id = :crfVersionId))"
            + " and (idata.status_id != 5 and idata.status_id != 7) )"
            + " and ditem.show_item=1";
        String postgres = "select distinct ditem.item_id from dyn_item_form_metadata ditem"
            + " where ditem.item_data_id in (select idata.item_data_id from item_data idata"
            + " where idata.event_crf_id = :eventCrfId and idata.item_id in ("
            + " select distinct igm.item_id from item_group_metadata igm"
            + " where igm.item_group_id = :groupId and igm.crf_version_id = :crfVersionId and igm.item_id in ("
            + " select ifm.item_id from item_form_metadata ifm where ifm.show_item='false' and ifm.section_id = :sectionId"
            + " and ifm.crf_version_id = :crfVersionId))"
            + " and (idata.status_id != 5 and idata.status_id != 7) )"
            + " and ditem.show_item='true'";
        return queryForIDs(oracle, postgres, groupId, sectionId, eventCrfId, crfVersionId);
    }

    public List<Integer> findShowItemDataIdsForAGroupInSection(int groupId, int sectionId, int crfVersionId, int eventCrfId) {
        String oracle = "select ditem.item_data_id from dyn_item_form_metadata ditem"
            + " where ditem.item_data_id in (select idata.item_data_id from item_data idata"
            + " where idata.event_crf_id = :eventCrfId and idata.item_id in ("
            + " select distinct igm.item_id from item_group_metadata igm"
            + " where igm.item_group_id = :groupId and igm.crf_version_id = :crfVersionId and igm.item_id in ("
            + " select ifm.item_id from item_form_metadata ifm where ifm.show_item=0 and ifm.section_id = :sectionId"
            + " and ifm.crf_version_id = :crfVersionId))"
            + " and (idata.status_id != 5 and idata.status_id != 7) )"
            + " and ditem.show_item=1";

        String postgres = "select ditem.item_data_id from dyn_item_form_metadata ditem"
            + " where ditem.item_data_id in (select idata.item_data_id from item_data idata"
            + " where idata.event_crf_id = :eventCrfId and idata.item_id in ("
            + " select distinct igm.item_id from item_group_metadata igm"
            + " where igm.item_group_id = :groupId and igm.crf_version_id = :crfVersionId and igm.item_id in ("
            + " select ifm.item_id from item_form_metadata ifm where ifm.show_item='false' and ifm.section_id = :sectionId"
            + " and ifm.crf_version_id = :crfVersionId))"
            + " and (idata.status_id != 5 and idata.status_id != 7) )"
            + " and ditem.show_item='true'";

        return queryForIDs(oracle, postgres, groupId, sectionId, eventCrfId, crfVersionId);
    }

    public List<Integer> findHideItemDataIdsForAGroupInSection(int groupId, int sectionId, int crfVersionId, int eventCrfId) {
        String oracle = "select ditem.item_data_id from dyn_item_form_metadata ditem"
            + " where ditem.item_data_id in (select idata.item_data_id from item_data idata"
            + " where idata.event_crf_id = :eventCrfId and idata.item_id in ("
            + " select distinct igm.item_id from item_group_metadata igm"
            + " where igm.item_group_id = :groupId and igm.crf_version_id = :crfVersionId and igm.item_id in ("
            + " select ifm.item_id from item_form_metadata ifm where ifm.show_item=0 and ifm.section_id = :sectionId"
            + " and ifm.crf_version_id = :crfVersionId))"
            + " and (idata.status_id != 5 and idata.status_id != 7) )"
            + " and ditem.show_item=0";

        String postgres = "select ditem.item_data_id from dyn_item_form_metadata ditem"
            + " where ditem.item_data_id in (select idata.item_data_id from item_data idata"
            + " where idata.event_crf_id = :eventCrfId and idata.item_id in ("
            + " select distinct igm.item_id from item_group_metadata igm"
            + " where igm.item_group_id = :groupId and igm.crf_version_id = :crfVersionId and igm.item_id in ("
            + " select ifm.item_id from item_form_metadata ifm where ifm.show_item='false' and ifm.section_id = :sectionId"
            + " and ifm.crf_version_id = :crfVersionId))"
            + " and (idata.status_id != 5 and idata.status_id != 7) )"
            + " and ditem.show_item='false'";

        return queryForIDs(oracle, postgres, groupId, sectionId, eventCrfId, crfVersionId);
    }

    public List<Integer> findShowItemDataIdsInSection(int sectionId, int crfVersionId, int eventCrfId) {
        String oracle = "select ditem.item_data_id from dyn_item_form_metadata ditem"
            + " where ditem.item_data_id in ( select idata.item_data_id from item_data idata"
            + " where idata.event_crf_id = :eventCrfId and idata.item_id in ("
            + " select ifm.item_id from item_form_metadata ifm where ifm.show_item=0 and ifm.section_id = :sectionId"
            + " and ifm.crf_version_id = :crfVersionId)"
            + " and (idata.status_id != 5 and idata.status_id != 7) )"
            + " and ditem.show_item=1";

        String postgres = "select ditem.item_data_id from dyn_item_form_metadata ditem"
            + " where ditem.item_data_id in ( select idata.item_data_id from item_data idata"
            + " where idata.event_crf_id = :eventCrfId and idata.item_id in ("
            + " select ifm.item_id from item_form_metadata ifm where ifm.show_item='false' and ifm.section_id = :sectionId"
            + " and ifm.crf_version_id = :crfVersionId)"
            + " and (idata.status_id != 5 and idata.status_id != 7) )"
            + " and ditem.show_item='true'";

        return queryForIDs(oracle, postgres, null, sectionId, eventCrfId, crfVersionId);
    }

    public Boolean hasShowingInSection(int sectionId, int crfVersionId, int eventCrfId) {
        String oracle = "select di.item_id from dyn_item_form_metadata di where di.item_data_id in ("
            + " select ida.item_data_id from item_data ida where ida.event_crf_id = :eventCrfId and ida.item_id in"
            + "       (select ifm.item_id from item_form_metadata ifm where ifm.section_id = :sectionId and ifm.crf_version_id = :crfVersionId"
            + "          and ifm.item_id not in  (select distinct igm.item_id from item_group_metadata igm where igm.crf_version_id = :crfVersionId"
            + "          and igm.show_group = 0"
            + "          and igm.item_id in (select im.item_id from item_form_metadata im where im.section_id = :sectionId and im.crf_version_id = :crfVersionId))"
            + "        )and (ida.status_id != 5 and ida.status_id != 7) ) and di.show_item = 1 and rownum = 1" ;
        String postgres = "select di.item_id from dyn_item_form_metadata di where di.item_data_id in ("
            + " select ida.item_data_id from item_data ida where ida.event_crf_id = :eventCrfId and ida.item_id in"
            + "       (select ifm.item_id from item_form_metadata ifm where ifm.section_id = :sectionId and ifm.crf_version_id = :crfVersionId"
            + "          and ifm.item_id not in  (select distinct igm.item_id from item_group_metadata igm where igm.crf_version_id = :crfVersionId"
            + "          and igm.show_group = 'false'"
            + "          and igm.item_id in (select im.item_id from item_form_metadata im where im.section_id = :sectionId and im.crf_version_id = :crfVersionId))"
            + "        )and (ida.status_id != 5 and ida.status_id != 7) ) and di.show_item = 'true' limit 1" ;

        return CollectionUtils.isNotEmpty(queryForIDs(oracle, postgres, null, sectionId, eventCrfId, crfVersionId));
    }

    /**
     * Executes a SQL query to retrieve a list of IDs
     *
     * @param oracleQuery The Oracle version of the query
     * @param postgresQuery The Postgres version of the query
     * @param groupId
     * @param sectionId
     * @param eventCrfId
     * @param crfVersionId
     * @return
     */
    protected List<Integer> queryForIDs(String oracleQuery, String postgresQuery, Integer groupId, Integer sectionId,
            Integer eventCrfId, Integer crfVersionId) {
        String query = "oracle".equalsIgnoreCase(CoreResources.getDBName()) ? oracleQuery : postgresQuery;
        Query q = getEntityManager().createNativeQuery(query);
        if (groupId != null) {
            q.setParameter("groupId", groupId);
        }
        if (sectionId != null) {
            q.setParameter("sectionId", sectionId);
        }
        if (eventCrfId != null) {
            q.setParameter("eventCrfId", eventCrfId);
        }
        if (crfVersionId != null) {
            q.setParameter("crfVersionId", crfVersionId);
        }
        return HibernateUtil.queryIDsList(q);
    }
    
    public  void delete(int eventCrfId){
        String query = " delete from " + getDomainClassName() +  "  where eventCrfId =:eventCrfId ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("eventCrfId", eventCrfId);
        q.executeUpdate();
    }

}
