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
