package org.akaza.openclinica.modern;

import org.akaza.openclinica.modern.service.ConfigurationDraftService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class InteropServiceIT extends AbstractIntegrationTest {

    @Autowired
    private InteropService interopService;

    @Autowired
    private ConfigurationDraftService draftService;

    @Test
    public void testUpsertSyntaxExecution() {
        // Manually create tables required by UnifiedWorkflowEnforcementService
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS study (study_id INT PRIMARY KEY)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS study_subject (study_subject_id INT PRIMARY KEY, subject_id VARCHAR(255), study_id INT, label VARCHAR(255), status_id INT, date_created TIMESTAMP, owner_id INT)");
        jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS study_subject_study_subject_id_seq");
        jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS study_event_study_event_id_seq");
        jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS event_crf_event_crf_id_seq");
        jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS item_data_item_data_id_seq");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS study_event_definition (study_event_definition_id INT PRIMARY KEY, oc_oid VARCHAR(255))");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS study_event (study_event_id INT PRIMARY KEY, study_event_definition_id INT, study_subject_id INT, status_id INT, owner_id INT, date_created TIMESTAMP)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS event_crf (event_crf_id INT PRIMARY KEY, study_event_id INT, study_subject_id INT, crf_version_id INT, completion_status_id INT, status_id INT, owner_id INT, date_created TIMESTAMP, sdv_status BOOLEAN)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS item_data (item_data_id INT PRIMARY KEY, event_crf_id INT, item_id INT, status_id INT, value VARCHAR(255), owner_id INT, date_created TIMESTAMP)");

        // We will insert a dummy record into the staging map and then call commit
        // This validates the PostgreSQL-specific ON CONFLICT DO NOTHING upsert syntax
        
        String recordId = "test-record-1";
        String payload = "{\"subject_id\":\"sub1\",\"event_id\":\"ev1\",\"item_value\":\"val1\"}";
        
        interopService.validate(recordId, payload);
        
        // Before committing, we must have active field mappings.
        draftService.saveDraftWithId("field-mappings-singleton", "system", "MAPPINGS", "{\"subject_id\":\"/subject_id\",\"event_id\":\"/event_id\",\"item_value\":\"/item_value\"}");
        
        assertDoesNotThrow(() -> {
            interopService.commit(recordId);
        });
    }
}
