package org.akaza.openclinica.dao.hibernate;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.akaza.openclinica.domain.datamap.DiscrepancyNote;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class DiscrepancyNoteDao extends AbstractDomainDao<DiscrepancyNote> {

    @Override
    Class<DiscrepancyNote> domainClass() {
        return DiscrepancyNote.class;
    }

    public List<DiscrepancyNote> findParentNotesByItemData(Integer itemDataId) {
        String query = "select dn.* from discrepancy_note dn, dn_item_data_map didm where didm.item_data_id=" + itemDataId + " AND dn.parent_dn_id isnull " + 
            "AND dn.discrepancy_note_id=didm.discrepancy_note_id";
        jakarta.persistence.Query q = getEntityManager().createNativeQuery(query, DiscrepancyNote.class);
        return ((List<DiscrepancyNote>) q.getResultList());
    }

    public DiscrepancyNote findByDiscrepancyNoteId(int discrepancyNoteId) {
        String query = "from " + getDomainClassName() + " do where do.discrepancyNoteId = :discrepancynoteid ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("discrepancynoteid", discrepancyNoteId);
        return (DiscrepancyNote) q.getResultList().stream().findFirst().orElse(null);
    }


}
