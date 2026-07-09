package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.bean.oid.CrfOidGenerator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.bean.oid.OidGenerator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.CrfBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class CrfDao extends AbstractDomainDao<CrfBean> {

    @Override
    Class<CrfBean> domainClass() {
        // TODO Auto-generated method stub
        return CrfBean.class;
    }

    public CrfBean findByName(String crfName) {
        String query = "from " + getDomainClassName() + " crf  where crf.name = :crfName ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("crfName", crfName);
        return (CrfBean) q.getResultList().stream().findFirst().orElse(null);
    }

    public CrfBean findByOcOID(String OCOID) {
        
        String query = "from " + getDomainClassName() + " do  where do.ocOid = :OCOID";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("OCOID", OCOID);
        return (CrfBean) q.getResultList().stream().findFirst().orElse(null);
    }

    public CrfBean findByCrfId(Integer crfId) {
        String query = "from " + getDomainClassName() + " crf  where crf.crfId = :crfId ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("crfId", crfId);
        return (CrfBean) q.getResultList().stream().findFirst().orElse(null);
    }
    
    private String getOid(CrfBean crf, String crfName) {
        OidGenerator oidGenerator = new CrfOidGenerator();
        String oid;
        try {
            oid = crf.getOcOid() != null ? crf.getOcOid() : oidGenerator.generateOid(crfName);
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    public String getValidOid(CrfBean crfBean, String crfName) {
        OidGenerator oidGenerator = new CrfOidGenerator();
        String oid = getOid(crfBean, crfName);
        String oidPreRandomization = oid;
        while (findByOcOID(oid) != null) {
            oid = oidGenerator.randomizeOid(oidPreRandomization);
        }
        return oid;
    }

}
