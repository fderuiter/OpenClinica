package org.akaza.openclinica.repository;

import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class ItemRepository {

    private final StudyEventDAO studyEventDaoJdbc;
    private final StudyEventDefinitionDAO studyEventDefinitionDaoJdbc;
    private final ItemDAO itemDaoJdbc;
    private final ItemDataDAO itemDataDaoJdbc;
    private final ItemFormMetadataDAO itemFormMetadataDaoJdbc; 

    @Autowired
    public ItemRepository(DataSource dataSource) {
        this.studyEventDaoJdbc = new StudyEventDAO(dataSource);
        this.studyEventDefinitionDaoJdbc = new StudyEventDefinitionDAO(dataSource);
        this.itemDaoJdbc = new ItemDAO(dataSource);
        this.itemDataDaoJdbc = new ItemDataDAO(dataSource);
        this.itemFormMetadataDaoJdbc = new ItemFormMetadataDAO(dataSource);
    }

    public StudyEventBean getStudyEventBean(Integer id) {
        return (StudyEventBean) studyEventDaoJdbc.findByPK(id);
    }

    public List<StudyEventBean> findAllStudyEventsByDefinition(Integer defId) {
        return (List<StudyEventBean>) studyEventDaoJdbc.findAllByDefinition(defId);
    }

    public List<StudyEventBean> findAllStudyEventsByStudyEventDefinitionAndCrfOids(String defOid, String crfOid) {
        return studyEventDaoJdbc.findAllByStudyEventDefinitionAndCrfOids(defOid, crfOid);
    }

    public ItemBean getItemBean(Integer id) {
        return (ItemBean) itemDaoJdbc.findByPK(id);
    }

    public ItemFormMetadataBean getItemFormMetadataBeanByItemIdAndCRFVersionId(Integer itemId, Integer crfVersionId) {
        return itemFormMetadataDaoJdbc.findByItemIdAndCRFVersionId(itemId, crfVersionId);
    }

    public StudyEventDefinitionBean getStudyEventDefinitionBean(Integer id) {
        return (StudyEventDefinitionBean) studyEventDefinitionDaoJdbc.findByPK(id);
    }

    public StudyEventDefinitionBean getStudyEventDefinitionBeanByOid(String oid) {
        return studyEventDefinitionDaoJdbc.findByOid(oid);
    }

    public ItemDataBean getItemDataBeanByItemIdAndEventCRFId(Integer itemId, Integer eventCrfId) {
        return itemDataDaoJdbc.findByItemIdAndEventCRFId(itemId, eventCrfId);
    }

    public List<StudyEventDefinitionBean> findAllStudyEventDefinitionsByCrf(org.akaza.openclinica.bean.admin.CRFBean crf) {
        return (List<StudyEventDefinitionBean>) studyEventDefinitionDaoJdbc.findAllByCrf(crf);
    }

    public java.util.HashMap findItemDataCountByStudyEventAndOIDs(Integer studyId, String itemOid, String itemGroupOid) {
        return itemDataDaoJdbc.findCountByStudyEventAndOIDs(studyId, itemOid, itemGroupOid);
    }
}
