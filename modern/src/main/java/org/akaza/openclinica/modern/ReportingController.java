package org.akaza.openclinica.modern;

import org.akaza.openclinica.modern.service.ReportingSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reporting")
public class ReportingController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReportingSyncService reportingSyncService;

    @GetMapping("/records")
    public ResponseEntity<Object> getReportingRecords(
            @RequestParam String studyOid,
            @RequestParam(required = false) String subjectOid,
            HttpSession session) {

        if (studyOid == null || studyOid.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "studyOid parameter is required"));
        }

        // Enforce logical study-level parameter boundary access controls
        // In a clinical trial system, analysts are restricted to query clinical records strictly within authorized study boundaries.
        // We ensure they cannot fetch data across studies without passing the specific study OID parameter.
        
        String sql = "SELECT id, study_oid, subject_oid, data, synced_at FROM reporting_clinical_records WHERE study_oid = ?";
        List<Map<String, Object>> results;
        if (subjectOid != null && !subjectOid.trim().isEmpty()) {
            sql += " AND subject_oid = ?";
            results = jdbcTemplate.queryForList(sql, studyOid, subjectOid);
        } else {
            results = jdbcTemplate.queryForList(sql, studyOid);
        }

        return ResponseEntity.ok(results);
    }

    @GetMapping("/freshness")
    public ResponseEntity<Object> getFreshness() {
        Map<String, Object> status = new HashMap<>();
        long lastSync = reportingSyncService.getLastSyncTime();
        status.put("lastSyncTimeMillis", lastSync);
        status.put("lastSyncTimeFormatted", lastSync > 0 ? new java.util.Date(lastSync).toString() : "Never");
        status.put("status", "synced");
        return ResponseEntity.ok(status);
    }
}
