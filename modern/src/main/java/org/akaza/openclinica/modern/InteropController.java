package org.akaza.openclinica.modern;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/interop")
public class InteropController {

    private FhirContext fhirContext = FhirContext.forR4();
    private HapiContext hl7Context = new DefaultHapiContext();
    private Map<String, String> mappings = new ConcurrentHashMap<>();

    @Autowired
    private InteropService interopService;

    @PostMapping("/fhir")
    public ResponseEntity<String> ingestFhir(@RequestBody String payload) {
        try {
            IParser parser = fhirContext.newJsonParser();
            Patient patient = parser.parseResource(Patient.class, payload);
            interopService.validate(patient.getIdBase(), payload);
            return ResponseEntity.ok("FHIR R4 resource received: " + patient.getIdBase());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid FHIR payload");
        }
    }

    @PostMapping("/hl7")
    public ResponseEntity<String> ingestHl7(@RequestBody String payload) {
        try {
            PipeParser parser = hl7Context.getPipeParser();
            Message message = parser.parse(payload);
            interopService.validate(message.getName(), payload);
            return ResponseEntity.ok("HL7 v2 message parsed: " + message.getName());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid HL7 payload");
        }
    }

    @GetMapping("/mapping/data")
    public ResponseEntity<Map<String, String>> getMappingInterface() {
        return ResponseEntity.ok(mappings);
    }

    @PostMapping("/mapping/data")
    public ResponseEntity<String> saveMapping(@RequestBody Map<String, String> newMappings) {
        mappings.putAll(newMappings);
        return ResponseEntity.ok("Mapping saved");
    }
    
    @GetMapping("/pipeline/review")
    public ResponseEntity<List<String>> pipelineReview() {
        return ResponseEntity.ok(interopService.getReviewQueue());
    }

    @PostMapping("/pipeline/commit")
    public ResponseEntity<String> pipelineCommit(@RequestParam String recordId) {
        interopService.commit(recordId);
        return ResponseEntity.ok("Data committed");
    }
}
