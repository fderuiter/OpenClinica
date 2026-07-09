package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.bean.oid.CrfVersionOidGenerator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.bean.oid.OidGenerator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class CrfVersionDao extends AbstractDomainDao<CrfVersion> {

    @Override
    Class<CrfVersion> domainClass() {
        // TODO Auto-generated method stub
        return CrfVersion.class;
    }

    public CrfVersion findByCrfVersionId(int crf_version_id) {
        String query = "from " + getDomainClassName() + " crf_version  where crf_version.crfVersionId = :crfversionid ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("crfversionid", crf_version_id);
        return (CrfVersion) q.getResultList().stream().findFirst().orElse(null);
    }

    public CrfVersion findByOcOID(String OCOID) {
        
        String query = "from " + getDomainClassName() + " do  where do.ocOid = :OCOID";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("OCOID", OCOID);
        return (CrfVersion) q.getResultList().stream().findFirst().orElse(null);
    }

    public CrfVersion findByNameCrfId(String name, Integer crfId) {
        String query = "select distinct cv.* from crf_version cv,crf c " + "where c.crf_id = " + crfId + " and cv.name = '" + name
                + "' and cv.crf_id = c.crf_id";
        jakarta.persistence.Query q = getEntityManager().createNativeQuery(query, CrfVersion.class);
        return ((CrfVersion) q.getResultList().stream().findFirst().orElse(null));
    }
    
    private String getOid(CrfVersion crfVersion, String crfName, String crfVersionName) {
        OidGenerator oidGenerator = new CrfVersionOidGenerator();
        String oid;
        try {
            oid = crfVersion.getOcOid() != null ? crfVersion.getOcOid() : oidGenerator.generateOid(crfName, crfVersionName);
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    public String getValidOid(CrfVersion crfVersion, String crfName, String crfVersionName) {
        OidGenerator oidGenerator = new CrfVersionOidGenerator();
        String oid = getOid(crfVersion, crfName, crfVersionName);
        String oidPreRandomization = oid;
        while (findByOcOID(oid) != null) {
            oid = oidGenerator.randomizeOid(oidPreRandomization);
        }
        return oid;

    }


}
