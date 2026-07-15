package org.akaza.openclinica.modern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dde")
public class DdeController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/validate")
    public ResponseEntity<String> validateDde(@RequestBody Map<String, String> payload) {
        String subjectOid = payload.get("subjectOid");
        String itemOid = payload.get("itemOid");
        String value = payload.get("value");
        boolean override = Boolean.parseBoolean(payload.getOrDefault("override", "false"));

        // Check if record exists
        String checkSql = "SELECT * FROM dde_records WHERE subject_oid = ? AND item_oid = ?";
        List<Map<String, Object>> records = jdbcTemplate.queryForList(checkSql, subjectOid, itemOid);

        if (records.isEmpty()) {
            // First entry (initial entry) - we store it as reference
            String insertSql = "INSERT INTO dde_records (id, subject_oid, item_oid, first_value, submission_count) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(insertSql, UUID.randomUUID().toString(), subjectOid, itemOid, value, 1);
            return ResponseEntity.ok("First entry saved");
        } else {
            // Second entry (verification)
            Map<String, Object> record = records.get(0);
            String firstValue = (String) record.get("first_value");
            
            if (!firstValue.equals(value) && !override) {
                // Mismatch on first submission of double entry
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Mismatch detected. Provide override to force save.");
            }

            // Successful double entry or overridden
            String updateSql = "UPDATE dde_records SET submission_count = submission_count + 1 WHERE id = ?";
            jdbcTemplate.update(updateSql, record.get("id"));
            return ResponseEntity.ok("Verification complete");
        }
    }
}
