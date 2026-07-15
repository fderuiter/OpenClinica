package org.akaza.openclinica.modern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/odm")
public class OdmController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping(value = "/export", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> exportOdm(@RequestParam(required = false) String studyOid) {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ODM xmlns=\"http://www.cdisc.org/ns/odm/v1.3\">\n" +
                "  <Study>\n" +
                "    <MetaDataVersion>\n" +
                "       <Protocol/>\n" +
                "    </MetaDataVersion>\n" +
                "  </Study>\n" +
                "  <ClinicalData>\n" +
                "    <SubjectData/>\n" +
                "  </ClinicalData>\n" +
                "</ODM>";
        return ResponseEntity.ok(xml);
    }

    @PostMapping(value = "/import", consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> importOdm(@RequestBody String xmlPayload) {
        // Extract basic data (mock logic for demo)
        String id = UUID.randomUUID().toString();
        String studyOid = "MOCK_STUDY";
        String subjectOid = "MOCK_SUBJECT";

        String sql = "INSERT INTO clinical_records (id, study_oid, subject_oid, data) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, id, studyOid, subjectOid, xmlPayload);

        return ResponseEntity.ok("Import successful");
    }
}
