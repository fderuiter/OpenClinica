package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.akaza.openclinica.bean.oid.ItemOidGenerator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.bean.oid.OidGenerator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.akaza.openclinica.domain.datamap.Item;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Query;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class ItemDao extends AbstractDomainDao<Item> {

    @Override
    Class<Item> domainClass() {
        // TODO Auto-generated method stub
        return Item.class;
    }

    public Item findByOcOID(String OCOID) {
        String query = "from " + getDomainClassName() + " item  where item.ocOid = :ocoid ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("ocoid", OCOID);
        return (Item) q.getResultList().stream().findFirst().orElse(null);
    }

    public Item findByNameCrfId(String name, Integer crfId) {
        String query = "select distinct i.* from item i, item_form_metadata ifm,crf_version cv " + "where i.name= '" + name + "' and i.item_id= ifm.item_id "
                + "and ifm.crf_version_id=cv.crf_version_id " + "and cv.crf_id=" + crfId;
        jakarta.persistence.Query q = getEntityManager().createNativeQuery(query, Item.class);
        return ((Item) q.getResultList().stream().findFirst().orElse(null));
    }
    
  public static final String findAllByCrfVersionIdQuery = "select distinct i.* from item i, item_form_metadata ifm " + "where i.item_id= ifm.item_id "
          + "and ifm.crf_version_id = :crfversionid";

  @SuppressWarnings("unchecked")
  public List<Item> findAllByCrfVersionId(Integer crfVersionId) {
      Query q = getEntityManager().createNativeQuery(findAllByCrfVersionIdQuery, Item.class);
      q.setParameter("crfversionid", crfVersionId.intValue());
      return (List<Item>) q.getResultList();
  }

    public int getItemDataTypeId(Item item) {
        String query = "select item_data_type_id from item where item_id = " + item.getItemId();
        jakarta.persistence.Query q = getEntityManager().createNativeQuery(query);
        return ((Number) q.getResultList().stream().findFirst().orElse(null)).intValue();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Item> findByItemGroupCrfVersionOrdered(Integer itemGroupId, Integer crfVersionId) {
        String query = "select distinct i.* from item i, item_group fg, item_group_metadata fgim " + " where fg.item_group_id= " + String.valueOf(itemGroupId)
                + " and fg.item_group_id=fgim.item_group_id and fgim.crf_version_id= " + String.valueOf(crfVersionId)
                + " and fgim.item_id=i.item_id order by i.item_id";
        jakarta.persistence.Query q = getEntityManager().createNativeQuery(query, Item.class);
        return (ArrayList<Item>) q.getResultList();
    }

    public String getValidOid(Item item, String crfName, String itemLabel, ArrayList<String> oidList) {
    OidGenerator oidGenerator = new ItemOidGenerator();
        String oid = getOid(item, crfName, itemLabel);
        String oidPreRandomization = oid;
        while (findByOcOID(oid) != null || oidList.contains(oid)) {
            oid = oidGenerator.randomizeOid(oidPreRandomization);
        }
        return oid;
    }

    private String getOid(Item item, String crfName, String itemLabel) {
        OidGenerator oidGenerator = new ItemOidGenerator();
        String oid;
        try {
            oid = item.getOcOid() != null ? item.getOcOid() : oidGenerator.generateOid(crfName, itemLabel);
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

}