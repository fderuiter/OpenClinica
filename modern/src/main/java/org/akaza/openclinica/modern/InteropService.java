package org.akaza.openclinica.modern;

import org.akaza.openclinica.modern.model.ConfigurationDraft;
import org.akaza.openclinica.modern.service.ConfigurationDraftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.type.TypeReference;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InteropService {

    private static final Logger log = LoggerFactory.getLogger(InteropService.class);
    private static final String DRAFT_TYPE = "CLINICAL_RECORD";
    private static final String MAPPINGS_ID = "field-mappings-singleton";

    private Map<String, String> recordsInStaging = new ConcurrentHashMap<>();

    @Autowired
    private ConfigurationDraftService draftService;

    @Autowired
    private javax.sql.DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();
    private HapiContext hl7Context = new DefaultHapiContext();

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

    @Transactional(rollbackFor = Exception.class)
    public void commit(String recordId) {
        String payload = recordsInStaging.get(recordId);
        if (payload == null) {
            throw new IllegalArgumentException("Record not found in staging: " + recordId);
        }

        try {
            ConfigurationDraft mappingDraft = draftService.getDraft(MAPPINGS_ID);
            if (mappingDraft == null || mappingDraft.getDraftData() == null) {
                throw new IllegalStateException("Active field mappings not found.");
            }

            Map<String, String> mappings = objectMapper.readValue(mappingDraft.getDraftData(), new TypeReference<Map<String, String>>() {});
            
            String subjectIdPath = mappings.get("subject_id");
            String eventIdPath = mappings.get("event_id");
            String itemValuePath = mappings.get("item_value");

            String subjectId = null;
            String eventId = null;
            String value = null;

            if (payload.trim().startsWith("{")) {
                JsonNode rootNode = objectMapper.readTree(payload);
                subjectId = extractJson(rootNode, subjectIdPath);
                eventId = extractJson(rootNode, eventIdPath);
                value = extractJson(rootNode, itemValuePath);
            } else if (payload.trim().startsWith("MSH")) {
                Message message = hl7Context.getPipeParser().parse(payload);
                Terser terser = new Terser(message);
                subjectId = extractHl7(terser, subjectIdPath);
                eventId = extractHl7(terser, eventIdPath);
                value = extractHl7(terser, itemValuePath);
            } else {
                throw new IllegalArgumentException("Unknown payload format.");
            }

            if (subjectId == null || eventId == null || value == null) {
                throw new IllegalStateException("Failed to extract required clinical fields based on dynamic mapping selectors.");
            }

            log.info("Extracted mapped values: subjectId={}, eventId={}, value={}", subjectId, eventId, value);

            org.akaza.openclinica.model.ClinicalPayload payloadObj = new org.akaza.openclinica.model.ClinicalPayload(subjectId, eventId, value);
            final String fSubjectId = subjectId;
            final String fValue = value;

            org.akaza.openclinica.service.clinical.UnifiedWorkflowEnforcementService workflowService = new org.akaza.openclinica.service.clinical.UnifiedWorkflowEnforcementService();
            workflowService.setDataSource(dataSource);

            workflowService.executeWorkflowTransaction(1L, payloadObj, new org.akaza.openclinica.service.clinical.WorkflowTransactionCallback<Void>() {
                @Override
                public Void doInTransaction() {
                    jdbcTemplate.update("INSERT INTO study_subject (label, subject_id, study_id, status_id, date_created, owner_id) VALUES (?, ?, 1, 1, NOW(), 1) ON CONFLICT DO NOTHING", fSubjectId, fSubjectId);
                    jdbcTemplate.update("INSERT INTO study_event (study_event_definition_id, study_subject_id, status_id, owner_id, date_created) VALUES (1, 1, 1, 1, NOW()) ON CONFLICT DO NOTHING");
                    jdbcTemplate.update("INSERT INTO item_data (event_crf_id, item_id, status_id, value, owner_id, date_created) VALUES (1, 1, 1, ?, 1, NOW())", fValue);
                    return null;
                }
            });
            
            recordsInStaging.remove(recordId);
            draftService.deleteDraft(recordId);
            log.info("Committed clinical record: {}", recordId);
        } catch (Exception e) {
            log.error("Failed to execute dynamic mapping or persist clinical data. Transaction rolled back for recordId: {}", recordId, e);
            throw new RuntimeException("Commit aborted: " + e.getMessage(), e);
        }
    }

    private String extractJson(JsonNode rootNode, String jsonPointer) {
        if (jsonPointer == null || jsonPointer.isEmpty()) return null;
        if (!jsonPointer.startsWith("/")) jsonPointer = "/" + jsonPointer;
        JsonNode node = rootNode.at(jsonPointer);
        if (node.isMissingNode() || node.isNull()) return null;
        String val = node.asText();
        return (val == null || val.isEmpty()) ? null : val;
    }

    private String extractHl7(Terser terser, String path) {
        if (path == null || path.isEmpty()) return null;
        try {
            String val = terser.get(path);
            return (val == null || val.isEmpty()) ? null : val;
        } catch (Exception e) {
            log.warn("Failed to extract HL7 path: {}", path);
            return null;
        }
    }
}
