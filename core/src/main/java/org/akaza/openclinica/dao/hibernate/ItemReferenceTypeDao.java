package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.ItemReferenceType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class ItemReferenceTypeDao extends AbstractDomainDao<ItemReferenceType> {

    @Override
    Class<ItemReferenceType> domainClass() {
        // TODO Auto-generated method stub
        return ItemReferenceType.class;
    }

    public ItemReferenceType findByItemReferenceTypeId(int item_reference_type_id) {
        String query = "from " + getDomainClassName() + " item_reference_type  where item_reference_type.itemReferenceTypeId = :itemreferencetypeid ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("itemreferencetypeid", item_reference_type_id);
        return (ItemReferenceType) q.getResultList().stream().findFirst().orElse(null);
    }

}
