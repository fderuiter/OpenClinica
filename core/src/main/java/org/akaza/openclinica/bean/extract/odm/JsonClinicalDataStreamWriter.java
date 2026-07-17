package org.akaza.openclinica.bean.extract.odm;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.akaza.openclinica.bean.submit.crfdata.ExportSubjectDataBean;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Locale;

public class JsonClinicalDataStreamWriter implements ClinicalDataStreamWriter {
    private final JsonGenerator jsonGenerator;
    private final String odmVersion;
    private final Locale locale;
    private final ObjectMapper mapper = new ObjectMapper();
    private final XmlMapper xmlMapper;
    
    private int subjectCount = 0;
    private JsonNode bufferedFirstSubject = null;
    private final JsonPostProcessor postProcessor;

    public JsonClinicalDataStreamWriter(OutputStream os, String odmVersion, Locale locale, int totalSubjects, JsonPostProcessor postProcessor) throws Exception {
        JsonFactory factory = new JsonFactory();
        this.jsonGenerator = factory.createGenerator(os);
        this.jsonGenerator.setCodec(mapper);
        this.odmVersion = odmVersion;
        this.locale = locale;
        this.postProcessor = postProcessor;
        
        javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
        xmlInputFactory.setProperty(javax.xml.stream.XMLInputFactory.IS_NAMESPACE_AWARE, false);
        this.xmlMapper = new XmlMapper(new com.fasterxml.jackson.dataformat.xml.XmlFactory(xmlInputFactory, null));
    }

    @Override
    public void writeStartDocument(String studyOID, String metaDataVersionOID, String metadataXml) throws Exception {
        JsonNode odmNode = xmlMapper.readTree(metadataXml + "</ODM>");
        if (postProcessor != null) {
            postProcessor.process(odmNode);
        }
        
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectFieldStart("ODM");
        
        if (odmNode != null) {
            java.util.Iterator<java.util.Map.Entry<String, JsonNode>> fields = odmNode.fields();
            while (fields.hasNext()) {
                java.util.Map.Entry<String, JsonNode> field = fields.next();
                if (!"ClinicalData".equals(field.getKey())) {
                    jsonGenerator.writeObjectField(field.getKey(), field.getValue());
                }
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
        
        byte[] xmlBytes = baos.toByteArray();
        JsonNode tree = xmlMapper.readTree(xmlBytes);
        if (postProcessor != null) {
            postProcessor.process(tree);
        }
        
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
