package org.akaza.openclinica.modern;

import org.akaza.openclinica.modern.service.ReportingSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

public class ReportingIntegrationTests extends AbstractIntegrationTest {

    @Autowired
    private ReportingSyncService reportingSyncService;

    @BeforeEach
    public void setUp() {
        // Clear reporting tables before each test to guarantee isolated runs
        jdbcTemplate.execute("DELETE FROM reporting_clinical_records");
        jdbcTemplate.execute("DELETE FROM clinical_records");
    }

    @Test
    public void testDecoupledReportingSyncWorkflow() throws Exception {
        // 1. Initially reporting schema should be empty
        Long countBefore = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reporting_clinical_records", Long.class);
        assertEquals(0L, countBefore);

        // 2. Insert transactional records with distinct study segment boundaries
        String recordId1 = UUID.randomUUID().toString();
        String recordId2 = UUID.randomUUID().toString();
        String recordId3 = UUID.randomUUID().toString();

        jdbcTemplate.update(
            "INSERT INTO clinical_records (id, study_oid, subject_oid, data) VALUES (?, ?, ?, ?)",
            recordId1, "STUDY_A", "SUBJ_101", "<Data>Subject A 101</Data>"
        );
        jdbcTemplate.update(
            "INSERT INTO clinical_records (id, study_oid, subject_oid, data) VALUES (?, ?, ?, ?)",
            recordId2, "STUDY_A", "SUBJ_102", "<Data>Subject A 102</Data>"
        );
        jdbcTemplate.update(
            "INSERT INTO clinical_records (id, study_oid, subject_oid, data) VALUES (?, ?, ?, ?)",
            recordId3, "STUDY_B", "SUBJ_201", "<Data>Subject B 201</Data>"
        );

        // 3. Trigger decoupled reporting background sync
        reportingSyncService.syncReportingData();

        // 4. Verify that data has been correctly synchronized to reporting table
        Long countAfter = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reporting_clinical_records", Long.class);
        assertEquals(3L, countAfter);

        // 5. Query the analytical reporting endpoint for STUDY_A and verify segmentation/access controls
        mockMvc.perform(get("/api/v1/reporting/records")
                .param("studyOid", "STUDY_A"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String content = result.getResponse().getContentAsString();
                    assertTrue(content.contains("SUBJ_101"));
                    assertTrue(content.contains("SUBJ_102"));
                    assertFalse(content.contains("SUBJ_201")); // Ensure study boundary isolation
                });

        // 6. Query the analytical reporting endpoint for STUDY_B
        mockMvc.perform(get("/api/v1/reporting/records")
                .param("studyOid", "STUDY_B"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String content = result.getResponse().getContentAsString();
                    assertFalse(content.contains("SUBJ_101"));
                    assertFalse(content.contains("SUBJ_102"));
                    assertTrue(content.contains("SUBJ_201"));
                });

        // 7. Verify freshness status API reports positive sync time
        mockMvc.perform(get("/api/v1/reporting/freshness"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String content = result.getResponse().getContentAsString();
                    assertTrue(content.contains("status"));
                    assertTrue(content.contains("synced"));
                    assertTrue(reportingSyncService.getLastSyncTime() > 0);
                });

        // 8. Test reporting schema read-only/decoupled principle: 
        // Active clinical transactions remain completely untouched and unmodified.
        Long transactionalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM clinical_records", Long.class);
        assertEquals(3L, transactionalCount);
    }
}
