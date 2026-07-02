package org.akaza.openclinica.modern;

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
public class StudyStatusController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PutMapping("/{uniqueProtocolID}/status")
    @Transactional
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

        // Find study ID
        List<Integer> studyIds = jdbcTemplate.queryForList("SELECT study_id FROM study WHERE unique_identifier = ?", Integer.class, uniqueProtocolID);
        if (studyIds.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Integer studyId = studyIds.get(0);
        
        // Lock/Freeze/Unlock study
        jdbcTemplate.update("UPDATE study SET status_id = ? WHERE study_id = ?", statusId, studyId);

        // Find all event definition IDs for the study
        List<Integer> sedIds = jdbcTemplate.queryForList("SELECT study_event_definition_id FROM study_event_definition WHERE study_id = ?", Integer.class, studyId);
        
        if (!sedIds.isEmpty()) {
            for (Integer sedId : sedIds) {
                // Update study event definitions
                jdbcTemplate.update("UPDATE study_event_definition SET status_id = ? WHERE study_event_definition_id = ?", statusId, sedId);
                
                // Find and update event definition CRFs
                List<Integer> edcIds = jdbcTemplate.queryForList("SELECT event_definition_crf_id FROM event_definition_crf WHERE study_event_definition_id = ?", Integer.class, sedId);
                if (!edcIds.isEmpty()) {
                    for (Integer edcId : edcIds) {
                        jdbcTemplate.update("UPDATE event_definition_crf SET status_id = ? WHERE event_definition_crf_id = ?", statusId, edcId);
                    }
                }
                
                // Find study events
                List<Integer> seIds = jdbcTemplate.queryForList("SELECT study_event_id FROM study_event WHERE study_event_definition_id = ?", Integer.class, sedId);
                if (!seIds.isEmpty()) {
                    for (Integer seId : seIds) {
                        jdbcTemplate.update("UPDATE study_event SET subject_event_status_id = ? WHERE study_event_id = ?", statusId, seId);
                        
                        // Find event CRFs
                        List<Integer> ecIds = jdbcTemplate.queryForList("SELECT event_crf_id FROM event_crf WHERE study_event_id = ?", Integer.class, seId);
                        if (!ecIds.isEmpty()) {
                            for (Integer ecId : ecIds) {
                                jdbcTemplate.update("UPDATE event_crf SET status_id = ? WHERE event_crf_id = ?", statusId, ecId);
                                
                                // Find item data
                                List<Integer> itemDataIds = jdbcTemplate.queryForList("SELECT item_data_id FROM item_data WHERE event_crf_id = ?", Integer.class, ecId);
                                if (!itemDataIds.isEmpty()) {
                                    for (Integer id : itemDataIds) {
                                        jdbcTemplate.update("UPDATE item_data SET status_id = ? WHERE item_data_id = ?", statusId, id);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Study status successfully updated to " + newStatus);
        response.put("uniqueProtocolID", uniqueProtocolID);
        
        return ResponseEntity.ok(response);
    }
}
