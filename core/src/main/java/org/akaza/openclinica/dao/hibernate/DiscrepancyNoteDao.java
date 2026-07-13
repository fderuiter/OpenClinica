package org.akaza.openclinica.dao.hibernate;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Transactional;

import org.akaza.openclinica.domain.datamap.DiscrepancyNote;
import org.akaza.openclinica.domain.datamap.DnItemDataMap;
import org.akaza.openclinica.domain.datamap.DnItemDataMapId;
import org.akaza.openclinica.domain.datamap.DnSubjectMap;
import org.akaza.openclinica.domain.datamap.DnSubjectMapId;
import org.akaza.openclinica.domain.datamap.DnStudySubjectMap;
import org.akaza.openclinica.domain.datamap.DnStudySubjectMapId;
import org.akaza.openclinica.domain.datamap.DnEventCrfMap;
import org.akaza.openclinica.domain.datamap.DnEventCrfMapId;
import org.akaza.openclinica.domain.datamap.DnStudyEventMap;
import org.akaza.openclinica.domain.datamap.DnStudyEventMapId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class DiscrepancyNoteDao extends AbstractDomainDao<DiscrepancyNote> {

    @Override
    Class<DiscrepancyNote> domainClass() {
        return DiscrepancyNote.class;
    }

    @Transactional
    public List<DiscrepancyNote> findParentNotesByItemData(Integer itemDataId) {
        String query = "select dn.* from discrepancy_note dn, dn_item_data_map didm where didm.item_data_id=" + itemDataId + " AND dn.parent_dn_id isnull " + 
            "AND dn.discrepancy_note_id=didm.discrepancy_note_id";
        jakarta.persistence.Query q = getEntityManager().createNativeQuery(query, DiscrepancyNote.class);
        return ((List<DiscrepancyNote>) q.getResultList());
    }

    @Transactional
    public DiscrepancyNote findByDiscrepancyNoteId(int discrepancyNoteId) {
        String query = "from " + getDomainClassName() + " do where do.discrepancyNoteId = :discrepancynoteid ";
        jakarta.persistence.Query q = getEntityManager().createQuery(query);
        q.setParameter("discrepancynoteid", discrepancyNoteId);
        return (DiscrepancyNote) q.getResultList().stream().findFirst().orElse(null);
    }

    @Transactional
    public void createMapping(DiscrepancyNote dn, int entityId, String column, String entityType) {
        if ("subject".equalsIgnoreCase(entityType)) {
            DnSubjectMap map = new DnSubjectMap();
            DnSubjectMapId id = new DnSubjectMapId();
            id.setSubjectId(entityId);
            id.setDiscrepancyNoteId(dn.getDiscrepancyNoteId());
            id.setColumnName(column);
            map.setDnSubjectMapId(id);
            getEntityManager().persist(map);
        } else if ("studySub".equalsIgnoreCase(entityType)) {
            DnStudySubjectMap map = new DnStudySubjectMap();
            DnStudySubjectMapId id = new DnStudySubjectMapId();
            id.setStudySubjectId(entityId);
            id.setDiscrepancyNoteId(dn.getDiscrepancyNoteId());
            id.setColumnName(column);
            map.setDnStudySubjectMapId(id);
            getEntityManager().persist(map);
        } else if ("eventCrf".equalsIgnoreCase(entityType)) {
            DnEventCrfMap map = new DnEventCrfMap();
            DnEventCrfMapId id = new DnEventCrfMapId();
            id.setEventCrfId(entityId);
            id.setDiscrepancyNoteId(dn.getDiscrepancyNoteId());
            id.setColumnName(column);
            map.setDnEventCrfMapId(id);
            getEntityManager().persist(map);
        } else if ("studyEvent".equalsIgnoreCase(entityType)) {
            DnStudyEventMap map = new DnStudyEventMap();
            DnStudyEventMapId id = new DnStudyEventMapId();
            id.setStudyEventId(entityId);
            id.setDiscrepancyNoteId(dn.getDiscrepancyNoteId());
            id.setColumnName(column);
            map.setDnStudyEventMapId(id);
            getEntityManager().persist(map);
        } else if ("itemData".equalsIgnoreCase(entityType)) {
            DnItemDataMap map = new DnItemDataMap();
            DnItemDataMapId id = new DnItemDataMapId();
            id.setItemDataId(entityId);
            id.setDiscrepancyNoteId(dn.getDiscrepancyNoteId());
            id.setColumnName(column);
            map.setDnItemDataMapId(id);
            map.setActivated(true);
            getEntityManager().persist(map);
        }
    }

}
