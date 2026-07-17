package org.akaza.openclinica.modern;

import org.akaza.openclinica.modern.dto.ConfigurationDraftRequest;
import org.akaza.openclinica.modern.model.ConfigurationDraft;
import org.akaza.openclinica.sdk.dto.ApiResponse;
import org.akaza.openclinica.modern.service.ConfigurationDraftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/config")
public class ConfigurationDraftController {

    private final ConfigurationDraftService draftService;

    @Autowired
    public ConfigurationDraftController(ConfigurationDraftService draftService) {
        this.draftService = draftService;
    }

    @PostMapping("/draft")
    public ResponseEntity<ApiResponse<ConfigurationDraft>> createDraft(@Valid @RequestBody ConfigurationDraftRequest payload) {
        String userName = payload.getUserName() != null ? payload.getUserName() : "system";
        String draftData = payload.getDraftData() != null ? payload.getDraftData() : "{}";
        ConfigurationDraft draft = draftService.saveDraft(userName, "DATASET", draftData);
        return ResponseEntity.ok(new ApiResponse<>(draft));
    }

    @GetMapping("/draft/{id}")
    public ResponseEntity<ApiResponse<ConfigurationDraft>> getDraft(@PathVariable String id) {
        ConfigurationDraft draft = draftService.getDraft(id);
        return ResponseEntity.ok(new ApiResponse<>(draft));
    }

    @PutMapping("/draft/{id}")
    public ResponseEntity<Void> updateDraft(@PathVariable String id, @Valid @RequestBody ConfigurationDraftRequest payload) {
        String draftData = payload.getDraftData() != null ? payload.getDraftData() : "{}";
        draftService.updateDraft(id, draftData);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/draft/{id}/commit")
    public ResponseEntity<ApiResponse<String>> commitDraft(@PathVariable String id) {
        ConfigurationDraft draft = draftService.getDraft(id);
        // Normally would convert draftData to DatasetBean and save it using dataset DAOs
        // For the headless foundation, deleting the draft signifies a successful commit
        draftService.deleteDraft(id);
        return ResponseEntity.ok(new ApiResponse<>("Dataset successfully created and committed."));
    }
}
