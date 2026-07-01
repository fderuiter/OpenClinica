package org.akaza.openclinica.logic.importdata;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PureStreamingDataLoader {

    private DataValidator dataValidator;

    public PureStreamingDataLoader(DataValidator dataValidator) {
        this.dataValidator = dataValidator;
    }

    public interface DataValidator {
        void validateSubjectData(String studyOid, String subjectOid, int lineNumber) throws Exception;
        void commitLogicalUnit() throws Exception;
        void updateProgress(int recordsProcessed);
    }

    public void processStream(InputStream inputStream) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        DefaultHandler handler = new DefaultHandler() {
            private Locator locator;
            private String currentStudyOid;
            private int recordsProcessed = 0;
            private boolean inSubjectData = false;
            
            @Override
            public void setDocumentLocator(Locator locator) {
                this.locator = locator;
            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if (qName.equalsIgnoreCase("ClinicalData")) {
                    currentStudyOid = attributes.getValue("StudyOID");
                } else if (qName.equalsIgnoreCase("SubjectData")) {
                    inSubjectData = true;
                    recordsProcessed++;
                    String subjectOid = attributes.getValue("SubjectKey");
                    if (subjectOid == null) {
                        subjectOid = attributes.getValue("SubjectOID"); // ODM uses SubjectKey or SubjectOID? Usually SubjectKey
                    }
                    try {
                        dataValidator.validateSubjectData(currentStudyOid, subjectOid, locator != null ? locator.getLineNumber() : -1);
                    } catch (Exception e) {
                        throw new SAXException("Validation failure at line " + (locator != null ? locator.getLineNumber() : -1) + ": " + e.getMessage(), e);
                    }
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                if (qName.equalsIgnoreCase("SubjectData")) {
                    inSubjectData = false;
                    try {
                        dataValidator.commitLogicalUnit();
                        dataValidator.updateProgress(recordsProcessed);
                    } catch (Exception e) {
                        throw new SAXException("Commit failure at line " + (locator != null ? locator.getLineNumber() : -1) + ": " + e.getMessage(), e);
                    }
                }
            }
        };

        saxParser.parse(inputStream, handler);
    }
}
