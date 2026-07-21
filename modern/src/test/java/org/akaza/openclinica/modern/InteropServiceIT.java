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
        jdbcTemplate.execute("DROP TABLE IF EXISTS study CASCADE; CREATE TABLE study (study_id INT PRIMARY KEY)");
        jdbcTemplate.execute("DROP TABLE IF EXISTS study_subject CASCADE; CREATE TABLE study_subject (study_subject_id INT PRIMARY KEY, subject_id VARCHAR(255), study_id INT, label VARCHAR(255), secondary_label VARCHAR(255), status_id INT, date_created TIMESTAMP, date_updated TIMESTAMP, enrollment_date TIMESTAMP, oc_oid VARCHAR(255), owner_id INT, update_id INT)");
        jdbcTemplate.execute("DROP SEQUENCE IF EXISTS study_subject_study_subject_id_seq; CREATE SEQUENCE study_subject_study_subject_id_seq");
        jdbcTemplate.execute("DROP SEQUENCE IF EXISTS subject_subject_id_seq; CREATE SEQUENCE subject_subject_id_seq");
        jdbcTemplate.execute("DROP SEQUENCE IF EXISTS study_event_study_event_id_seq; CREATE SEQUENCE study_event_study_event_id_seq");
        jdbcTemplate.execute("DROP SEQUENCE IF EXISTS event_crf_event_crf_id_seq; CREATE SEQUENCE event_crf_event_crf_id_seq");
        jdbcTemplate.execute("DROP SEQUENCE IF EXISTS item_data_item_data_id_seq; CREATE SEQUENCE item_data_item_data_id_seq");
        jdbcTemplate.execute("DROP TABLE IF EXISTS study_event_definition CASCADE; CREATE TABLE study_event_definition (study_event_definition_id INT PRIMARY KEY, oc_oid VARCHAR(255))");
        jdbcTemplate.execute("DROP TABLE IF EXISTS study_event CASCADE; CREATE TABLE study_event (study_event_id INT PRIMARY KEY, study_event_definition_id INT, study_subject_id INT, status_id INT, owner_id INT, date_created TIMESTAMP, date_updated TIMESTAMP, location VARCHAR(255), sample_ordinal INT, date_start TIMESTAMP, date_end TIMESTAMP, update_id INT, subject_event_status_id INT, start_time_flag BOOLEAN, end_time_flag BOOLEAN)");
        jdbcTemplate.execute("DROP TABLE IF EXISTS event_crf CASCADE; CREATE TABLE event_crf (event_crf_id INT PRIMARY KEY, study_event_id INT, study_subject_id INT, crf_version_id INT, completion_status_id INT, status_id INT, owner_id INT, date_created TIMESTAMP, date_updated TIMESTAMP, sdv_status BOOLEAN, date_interviewed DATE, annotations VARCHAR(255), date_completed TIMESTAMP, validator_id INT, date_validate TIMESTAMP, date_validate_completed TIMESTAMP, validator_annotations VARCHAR(255), validate_string VARCHAR(255), electronic_signature_id INT, update_id INT)");
        jdbcTemplate.execute("DROP TABLE IF EXISTS item_data CASCADE; CREATE TABLE item_data (item_data_id INT PRIMARY KEY, event_crf_id INT, item_id INT, status_id INT, value VARCHAR(255), owner_id INT, date_created TIMESTAMP, date_updated TIMESTAMP, ordinal INT, update_id INT)");

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
