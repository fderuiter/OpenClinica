package org.akaza.openclinica.modern;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.akaza.openclinica.bean.login.StudyStatusTransitionDTO;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.modern.service.StudyStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/studies")
@Tag(name = "Study Status", description = "Operations pertaining to Study Status in OpenClinica Modern API")
public class StudyStatusController {

    @Autowired
    private StudyStatusService studyStatusService;

    @PutMapping("/{uniqueProtocolID}/status")
    @Operation(summary = "Update Study Status", description = "Safely executes relational updates across study, study_event, and item_data tables.")
    public ResponseEntity<Object> updateStatus(
            @PathVariable("uniqueProtocolID") String uniqueProtocolID,
            @Validated @RequestBody StudyStatusTransitionDTO statusDTO,
            HttpSession session) {
        
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

        UserAccountBean userBean = (UserAccountBean) session.getAttribute("userBean");
        if (userBean == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            studyStatusService.updateStudyStatus(uniqueProtocolID, statusId, newStatus, userBean);
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Forbidden: Insufficient privileges."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (org.akaza.openclinica.service.clinical.exception.ClinicalWorkflowException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "Status Update Blocked",
                "details", e.getMessage()
            ));
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Study status successfully updated to " + newStatus);
        response.put("uniqueProtocolID", uniqueProtocolID);
        
        return ResponseEntity.ok(response);
    }
}
