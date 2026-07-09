package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.akaza.openclinica.bean.oid.ItemGroupOidGenerator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.bean.oid.OidGenerator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.CrfBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.ItemGroup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Query;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class ItemGroupDao extends AbstractDomainDao<ItemGroup> {

    @Override
    Class<ItemGroup> domainClass() {
        return ItemGroup.class;
    }

    public ItemGroup findByOcOID(String OCOID) {
        
        String query = "from " + getDomainClassName() + " do  where do.ocOid = :OCOID";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("OCOID", OCOID);
        return (ItemGroup) q.getResultList().stream().findFirst().orElse(null);
    }

    public ItemGroup findByNameCrfId(String groupName, CrfBean crf) {
        
        String query = "from " + getDomainClassName() + " do  where do.name = :groupName and do.crf = :crf";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("groupName", groupName);
        q.setParameter("crf", crf);
        return (ItemGroup) q.getResultList().stream().findFirst().orElse(null);
    }

    public static final String findAllByCrfVersionIdQuery = "select distinct ig.* from item_group ig, item_group_metadata igm"
            + " where igm.crf_version_id = :crfversionid and ig.item_group_id = igm.item_group_id";

    @SuppressWarnings("unchecked")
    public ArrayList<ItemGroup> findByCrfVersionId(Integer crfVersionId) {
        Query q = getEntityManager().createNativeQuery(findAllByCrfVersionIdQuery, ItemGroup.class);
        q.setParameter("crfversionid", crfVersionId.intValue());
        return (ArrayList<ItemGroup>) q.getResultList();
    }

    public String getValidOid(ItemGroup itemGroup, String crfName, String itemGroupLabel, ArrayList<String> oidList) {
    OidGenerator oidGenerator = new ItemGroupOidGenerator();
        String oid = getOid(itemGroup, crfName, itemGroupLabel);
        String oidPreRandomization = oid;
        while (findByOcOID(oid) != null || oidList.contains(oid)) {
            oid = oidGenerator.randomizeOid(oidPreRandomization);
        }
        return oid;
    }

    private String getOid(ItemGroup itemGroup, String crfName, String itemGroupLabel) {
        OidGenerator oidGenerator = new ItemGroupOidGenerator();
        String oid;
        try {
            oid = itemGroup.getOcOid() != null ? itemGroup.getOcOid() : oidGenerator.generateOid(crfName, itemGroupLabel);
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }
}
