package org.akaza.openclinica.repository;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.domain.datamap.Study;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@Repository
public class StudyRepository {

    private final StudyDao studyDaoHibernate;
    private final StudyDAO studyDaoJdbc;

    @Autowired
    public StudyRepository(DataSource dataSource, StudyDao studyDaoHibernate) {
        this.studyDaoHibernate = studyDaoHibernate;
        this.studyDaoJdbc = new StudyDAO(dataSource);
    }

    public StudyBean getStudyBean(Integer id) {
        return (StudyBean) studyDaoJdbc.findByPK(id);
    }

    public Study getStudyEntity(Integer id) {
        return studyDaoHibernate.findById(id);
    }

    public StudyBean getStudyBeanByUniqueIdentifier(String uniqueId) {
        return studyDaoJdbc.findByUniqueIdentifier(uniqueId);
    }

    public StudyBean getSiteBeanByUniqueIdentifier(String studyUniqueId, String siteUniqueId) {
        return studyDaoJdbc.findSiteByUniqueIdentifier(studyUniqueId, siteUniqueId);
    }

    @Transactional
    public StudyBean save(StudyBean bean) {
        Study entity = new Study();
        if (bean.getId() > 0) {
            entity.setStudyId(bean.getId());
        }
        
        entity.setName(bean.getName());
        entity.setUniqueIdentifier(bean.getIdentifier());
        entity.setSecondaryIdentifier(bean.getSecondaryIdentifier());
        
        entity = studyDaoHibernate.saveOrUpdate(entity);
        studyDaoHibernate.getEntityManager().flush();
        studyDaoHibernate.getEntityManager().clear();
        bean.setId(entity.getStudyId());
        return bean;
    }
}
