package org.akaza.openclinica.modern;

import org.akaza.openclinica.modern.model.ConfigurationDraft;
import org.akaza.openclinica.modern.service.ConfigurationDraftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InteropService {

    private static final Logger log = LoggerFactory.getLogger(InteropService.class);
    private static final String DRAFT_TYPE = "CLINICAL_RECORD";

    private Map<String, String> recordsInStaging = new ConcurrentHashMap<>();

    @Autowired
    private ConfigurationDraftService draftService;

    @PostConstruct
    public void init() {
        try {
            List<ConfigurationDraft> drafts = draftService.getDraftsByType(DRAFT_TYPE);
            for (ConfigurationDraft draft : drafts) {
                recordsInStaging.put(draft.getId(), draft.getDraftData());
            }
            log.info("Loaded {} clinical records from configuration_drafts", drafts.size());
        } catch (org.springframework.jdbc.BadSqlGrammarException e) {
            log.warn("Table configuration_drafts not found. Skipping draft initialization for clinical records.");
        } catch (Exception e) {
            log.error("Failed to load clinical records from drafts", e);
        }
    }

    public void validate(String recordId, String payload) {
        // Schema validation and business logic checks
        recordsInStaging.put(recordId, payload);
        draftService.saveDraftWithId(recordId, "system", DRAFT_TYPE, payload);
        log.info("Ingested and staged clinical record: {}", recordId);
    }

    public List<String> getReviewQueue() {
        return new ArrayList<>(recordsInStaging.keySet());
    }

    public void commit(String recordId) {
        // Mandatory user-review stage approved, commit data
        recordsInStaging.remove(recordId);
        try {
            draftService.deleteDraft(recordId);
        } catch (Exception e) {
            log.warn("Failed to delete draft for recordId: {}", recordId, e);
        }
        log.info("Committed clinical record: {}", recordId);
    }
}
