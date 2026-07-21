package org.akaza.openclinica.modern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntegrationTests extends AbstractIntegrationTest {

    @Test
    public void testOdmExport() throws Exception {
        mockMvc.perform(get("/api/odm/export").param("studyOid", "S1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(result -> {
                    String content = result.getResponse().getContentAsString();
                    assertTrue(content.contains("<ODM"));
                    assertTrue(content.contains("<Study>"));
                    assertTrue(content.contains("<ClinicalData>"));
                });
    }

    @Test
    public void testOdmImportWritesToDatabase() throws Exception {
        String xmlPayload = "<?xml version=\"1.0\"?><ODM><ClinicalData><SubjectData/></ClinicalData></ODM>";
        
        long countBefore = getCount("clinical_records");
        
        mockMvc.perform(post("/api/odm/import")
                .contentType(MediaType.APPLICATION_XML)
                .content(xmlPayload))
                .andExpect(status().isOk());
                
        long countAfter = getCount("clinical_records");
        assertEquals(countBefore + 1, countAfter, "Clinical records count should increase by 1");
    }

    @Test
    public void testDdeValidationWorkflow() throws Exception {
        String subjectOid = "SUBJ_1";
        String itemOid = "ITEM_1";
        
        // 1. Initial entry
        mockMvc.perform(post("/api/dde/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"subjectOid\":\"" + subjectOid + "\", \"itemOid\":\"" + itemOid + "\", \"value\":\"A\"}"))
                .andExpect(status().isOk());
                
        // 2. Mismatched double entry without override
        mockMvc.perform(post("/api/dde/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"subjectOid\":\"" + subjectOid + "\", \"itemOid\":\"" + itemOid + "\", \"value\":\"B\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Mismatch detected. Provide override to force save."));
                
        // 3. Mismatched double entry with override
        mockMvc.perform(post("/api/dde/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"subjectOid\":\"" + subjectOid + "\", \"itemOid\":\"" + itemOid + "\", \"value\":\"B\", \"override\":\"true\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Verification complete"));
                
        // 4. Matched double entry
        String subjectOid2 = "SUBJ_2";
        mockMvc.perform(post("/api/dde/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"subjectOid\":\"" + subjectOid2 + "\", \"itemOid\":\"" + itemOid + "\", \"value\":\"C\"}"))
                .andExpect(status().isOk());
                
        mockMvc.perform(post("/api/dde/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"subjectOid\":\"" + subjectOid2 + "\", \"itemOid\":\"" + itemOid + "\", \"value\":\"C\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Verification complete"));
    }

    private long getCount(String tableName) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
        return count != null ? count : 0;
    }
}
