package org.akaza.openclinica.repository;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.hibernate.CrfDao;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class FormRepository {

    private final CrfDao crfDaoHibernate;
    private final EventCrfDao eventCrfDaoHibernate;
    private final CRFDAO crfDaoJdbc;
    private final CRFVersionDAO crfVersionDaoJdbc;

    @Autowired
    public FormRepository(DataSource dataSource, CrfDao crfDaoHibernate, EventCrfDao eventCrfDaoHibernate) {
        this.crfDaoHibernate = crfDaoHibernate;
        this.eventCrfDaoHibernate = eventCrfDaoHibernate;
        this.crfDaoJdbc = new CRFDAO(dataSource);
        this.crfVersionDaoJdbc = new CRFVersionDAO(dataSource);
    }

    public CRFBean getCrfBeanByVersionId(Integer versionId) {
        return crfDaoJdbc.findByVersionId(versionId);
    }

    public CRFBean getCrfBean(Integer id) {
        return (CRFBean) crfDaoJdbc.findByPK(id);
    }

    public CRFBean getCrfBeanByItemOid(String itemOid) {
        return crfDaoJdbc.findByItemOid(itemOid);
    }

    public CRFVersionBean getCrfVersionBean(Integer id) {
        return (CRFVersionBean) crfVersionDaoJdbc.findByPK(id);
    }

    public List<CRFVersionBean> findAllCrfVersionsByCrf(Integer crfId) {
        return (List<CRFVersionBean>) crfVersionDaoJdbc.findAllByCRF(crfId);
    }

    @Transactional
    public CRFBean save(CRFBean bean) {
        org.akaza.openclinica.domain.datamap.CrfBean entity = new org.akaza.openclinica.domain.datamap.CrfBean();
        if (bean.getId() > 0) {
            entity.setCrfId(bean.getId());
        }
        entity.setName(bean.getName());
        entity.setDescription(bean.getDescription());
        entity.setOcOid(bean.getOid());
        
        entity = crfDaoHibernate.saveOrUpdate(entity);
        crfDaoHibernate.getEntityManager().flush();
        crfDaoHibernate.getEntityManager().clear();
        bean.setId(entity.getCrfId());
        return bean;
    }

    @Transactional
    public EventCRFBean save(EventCRFBean bean) {
        EventCrf entity = new EventCrf();
        if (bean.getId() > 0) {
            entity.setEventCrfId(bean.getId());
        }
        
        entity = eventCrfDaoHibernate.saveOrUpdate(entity);
        eventCrfDaoHibernate.getEntityManager().flush();
        eventCrfDaoHibernate.getEntityManager().clear();
        bean.setId(entity.getEventCrfId());
        return bean;
    }
}
