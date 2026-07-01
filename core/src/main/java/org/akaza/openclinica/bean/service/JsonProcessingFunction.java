package org.akaza.openclinica.bean.service;

import java.io.*;
import javax.xml.stream.*;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

public class JsonProcessingFunction extends ProcessingFunction {

    public JsonProcessingFunction() {
        fileType = "json";
    }

    public ProcessingResultType run() {
        FileReader fileReader = null;
        FileWriter fileWriter = null;
        XMLStreamReader reader = null;
        JsonGenerator jg = null;
        try {
            File xmlFile = new File(getODMXMLFileName());
            File jsonFile = new File(getTransformFileName());

            XMLInputFactory xmlif = XMLInputFactory.newInstance();
            fileReader = new FileReader(xmlFile);
            reader = xmlif.createXMLStreamReader(fileReader);

            JsonFactory jf = new JsonFactory();
            fileWriter = new FileWriter(jsonFile);
            jg = jf.createJsonGenerator(fileWriter);
            jg.useDefaultPrettyPrinter();

            jg.writeStartObject();
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    jg.writeFieldName(reader.getLocalName());
                    parseElement(reader, jg);
                }
            }
            jg.writeEndObject();
            jg.close();
            reader.close();
            fileReader.close();
            fileWriter.close();

            setArchivedFileName(jsonFile.getName());

            return ProcessingResultType.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ProcessingResultType.FAIL;
        } finally {
            try { if (jg != null) jg.close(); } catch (Exception e) {}
            try { if (reader != null) reader.close(); } catch (Exception e) {}
            try { if (fileReader != null) fileReader.close(); } catch (Exception e) {}
            try { if (fileWriter != null) fileWriter.close(); } catch (Exception e) {}
        }
    }

    private void parseElement(XMLStreamReader reader, JsonGenerator jg) throws Exception {
        jg.writeStartObject();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            jg.writeStringField("@" + reader.getAttributeLocalName(i), reader.getAttributeValue(i));
        }

        String currentArrayTag = null;
        StringBuilder textBuffer = new StringBuilder();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (textBuffer.toString().trim().length() > 0) {
                    jg.writeStringField("value", textBuffer.toString().trim());
                    textBuffer.setLength(0);
                }
                String tagName = reader.getLocalName();
                if (currentArrayTag == null || !currentArrayTag.equals(tagName)) {
                    if (currentArrayTag != null) {
                        jg.writeEndArray();
                    }
                    jg.writeArrayFieldStart(tagName);
                    currentArrayTag = tagName;
                }
                parseElement(reader, jg);
            } else if (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) {
                textBuffer.append(reader.getText());
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if (currentArrayTag != null) {
                    jg.writeEndArray();
                }
                if (textBuffer.toString().trim().length() > 0) {
                    jg.writeStringField("value", textBuffer.toString().trim());
                }
                jg.writeEndObject();
                return;
            }
        }
    }
}
