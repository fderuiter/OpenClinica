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

    public void createMapping(DiscrepancyNote dn, int entityId, String column, String entityType) {
        String sql = null;
        if ("subject".equalsIgnoreCase(entityType)) {
            sql = "INSERT INTO dn_subject_map (subject_id, discrepancy_note_id, column_name) VALUES (?,?,?)";
        } else if ("studySub".equalsIgnoreCase(entityType)) {
            sql = "INSERT INTO dn_study_subject_map (study_subject_id, discrepancy_note_id, column_name) VALUES (?,?,?)";
        } else if ("eventCrf".equalsIgnoreCase(entityType)) {
            sql = "INSERT INTO dn_event_crf_map (event_crf_id, discrepancy_note_id, column_name) VALUES (?,?,?)";
        } else if ("studyEvent".equalsIgnoreCase(entityType)) {
            sql = "INSERT INTO dn_study_event_map (study_event_id, discrepancy_note_id, column_name) VALUES (?,?,?)";
        } else if ("itemData".equalsIgnoreCase(entityType)) {
            sql = "INSERT INTO dn_item_data_map (item_data_id, discrepancy_note_id, column_name, activated) VALUES (?,?,?,?)";
        }

        if (sql != null) {
            jakarta.persistence.Query q = getEntityManager().createNativeQuery(sql);
            q.setParameter(1, entityId);
            q.setParameter(2, dn.getDiscrepancyNoteId());
            q.setParameter(3, column);
            if ("itemData".equalsIgnoreCase(entityType)) {
                q.setParameter(4, true);
            }
            q.executeUpdate();
        }
    }

}
