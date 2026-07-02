package org.akaza.openclinica.modern;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.akaza.openclinica.bean.login.StudyStatusTransitionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/studies")
@Tag(name = "Study Status", description = "Operations pertaining to Study Status in OpenClinica Modern API")
public class StudyStatusController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PutMapping("/{uniqueProtocolID}/status")
    @Transactional
    @Operation(summary = "Update Study Status", description = "Safely executes relational updates across study, study_event, and item_data tables.")
    public ResponseEntity<Object> updateStatus(
            @PathVariable("uniqueProtocolID") String uniqueProtocolID,
            @Validated @RequestBody StudyStatusTransitionDTO statusDTO) {
        
        String newStatus = statusDTO.getStatus();
        if (newStatus == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Status is required"));
        }
        
        int statusId;
        if (newStatus.equalsIgnoreCase("locked")) {
            statusId = 3;
        } else if (newStatus.equalsIgnoreCase("frozen")) {
            statusId = 8;
        } else if (newStatus.equalsIgnoreCase("available")) {
            statusId = 1;
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid status. Must be locked, frozen, or available."));
        }

        // Find study ID and old status
        List<Map<String, Object>> studyInfo = jdbcTemplate.queryForList("SELECT study_id, status_id FROM study WHERE unique_identifier = ?", uniqueProtocolID);
        if (studyInfo.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Integer studyId = ((Number) studyInfo.get(0).get("study_id")).intValue();
        
        // Lock/Freeze/Unlock study and its sites (preserve old_status_id)
        jdbcTemplate.update("UPDATE study SET old_status_id = status_id, status_id = ? WHERE study_id = ? OR parent_study_id = ?", statusId, studyId, studyId);

        // Update study event definitions (only applies if locking a parent study)
        jdbcTemplate.update("UPDATE study_event_definition SET status_id = ? WHERE study_id = ?", statusId, studyId);
        
        // Update event definition CRFs (only applies if locking a parent study)
        jdbcTemplate.update("UPDATE event_definition_crf SET status_id = ? WHERE study_event_definition_id IN (SELECT study_event_definition_id FROM study_event_definition WHERE study_id = ?)", statusId, studyId);
        
        // Update study subjects for this study (and its sites, if this is a parent study)
        jdbcTemplate.update("UPDATE study_subject SET status_id = ? WHERE study_id = ? OR study_id IN (SELECT study_id FROM study WHERE parent_study_id = ?)", statusId, studyId, studyId);

        // Update study events linked to these subjects
        jdbcTemplate.update("UPDATE study_event SET status_id = ? WHERE study_subject_id IN (SELECT study_subject_id FROM study_subject WHERE study_id = ? OR study_id IN (SELECT study_id FROM study WHERE parent_study_id = ?))", statusId, studyId, studyId);
        
        // Update event CRFs linked to these study events
        jdbcTemplate.update("UPDATE event_crf SET status_id = ? WHERE study_event_id IN (SELECT study_event_id FROM study_event WHERE study_subject_id IN (SELECT study_subject_id FROM study_subject WHERE study_id = ? OR study_id IN (SELECT study_id FROM study WHERE parent_study_id = ?)))", statusId, studyId, studyId);
        
        // Update item data linked to these event CRFs
        jdbcTemplate.update("UPDATE item_data SET status_id = ? WHERE event_crf_id IN (SELECT event_crf_id FROM event_crf WHERE study_event_id IN (SELECT study_event_id FROM study_event WHERE study_subject_id IN (SELECT study_subject_id FROM study_subject WHERE study_id = ? OR study_id IN (SELECT study_id FROM study WHERE parent_study_id = ?))))", statusId, studyId, studyId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Study status successfully updated to " + newStatus);
        response.put("uniqueProtocolID", uniqueProtocolID);
        
        return ResponseEntity.ok(response);
    }
}
