package org.akaza.openclinica.bean.extract.odm;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.akaza.openclinica.bean.submit.crfdata.ExportSubjectDataBean;
import org.json.JSONObject;
import org.json.XML;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Locale;

public class JsonClinicalDataStreamWriter implements ClinicalDataStreamWriter {
    private final JsonGenerator jsonGenerator;
    private final String odmVersion;
    private final Locale locale;
    private final ObjectMapper mapper = new ObjectMapper();
    
    private int subjectCount = 0;
    private com.fasterxml.jackson.databind.JsonNode bufferedFirstSubject = null;
    private final JsonPostProcessor postProcessor;

    public JsonClinicalDataStreamWriter(OutputStream os, String odmVersion, Locale locale, int totalSubjects, JsonPostProcessor postProcessor) throws Exception {
        JsonFactory factory = new JsonFactory();
        this.jsonGenerator = factory.createGenerator(os);
        this.jsonGenerator.setCodec(mapper);
        this.odmVersion = odmVersion;
        this.locale = locale;
        this.postProcessor = postProcessor;
    }

    @Override
    public void writeStartDocument(String studyOID, String metaDataVersionOID, String metadataXml) throws Exception {
        JSONObject metadataJson = XML.toJSONObject(metadataXml + "</ODM>");
        if (postProcessor != null) {
            postProcessor.process(metadataJson);
        }
        JSONObject odmObj = metadataJson.optJSONObject("ODM");
        if (odmObj == null) odmObj = new JSONObject();

        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectFieldStart("ODM");
        
        com.fasterxml.jackson.databind.JsonNode odmNode = mapper.readTree(odmObj.toString());
        java.util.Iterator<java.util.Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> fields = odmNode.fields();
        while (fields.hasNext()) {
            java.util.Map.Entry<String, com.fasterxml.jackson.databind.JsonNode> field = fields.next();
            if (!"ClinicalData".equals(field.getKey())) {
                jsonGenerator.writeObjectField(field.getKey(), field.getValue());
            }
        }

        jsonGenerator.writeObjectFieldStart("ClinicalData");
        
        if (studyOID != null && !studyOID.isEmpty()) {
            jsonGenerator.writeStringField("StudyOID", studyOID);
        }
        if (metaDataVersionOID != null && !metaDataVersionOID.isEmpty()) {
            jsonGenerator.writeStringField("MetaDataVersionOID", metaDataVersionOID);
        }
    }

    @Override
    public void writeSubjectData(ExportSubjectDataBean sub) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlClinicalDataStreamWriter tempXmlWriter = new XmlClinicalDataStreamWriter(baos, odmVersion);
        tempXmlWriter.writeSubjectData(sub);
        tempXmlWriter.close();
        
        String xml = new String(baos.toByteArray(), "UTF-8");
        JSONObject jsonObject = XML.toJSONObject(xml);
        if (postProcessor != null) {
            postProcessor.process(jsonObject);
        }
        JSONObject subjectDataJson = jsonObject.optJSONObject("SubjectData");
        if (subjectDataJson == null) {
            subjectDataJson = new JSONObject();
        }

        com.fasterxml.jackson.databind.JsonNode tree = mapper.readTree(subjectDataJson.toString());
        
        subjectCount++;
        
        if (subjectCount == 1) {
            bufferedFirstSubject = tree;
        } else if (subjectCount == 2) {
            jsonGenerator.writeArrayFieldStart("SubjectData");
            jsonGenerator.writeTree(bufferedFirstSubject);
            jsonGenerator.writeTree(tree);
            bufferedFirstSubject = null;
            jsonGenerator.flush();
        } else {
            jsonGenerator.writeTree(tree);
            jsonGenerator.flush();
        }
    }

    @Override
    public void writeEndDocument() throws Exception {
        if (subjectCount == 1) {
            jsonGenerator.writeFieldName("SubjectData");
            jsonGenerator.writeTree(bufferedFirstSubject);
        } else if (subjectCount > 1) {
            jsonGenerator.writeEndArray(); // End SubjectData array
        }
        
        jsonGenerator.writeEndObject(); // End ClinicalData
        jsonGenerator.writeEndObject(); // End ODM
        jsonGenerator.writeEndObject(); // End Root
        jsonGenerator.flush();
        jsonGenerator.close();
    }

    @Override
    public void close() throws Exception {
        if (!jsonGenerator.isClosed()) {
            jsonGenerator.close();
        }
    }
}
