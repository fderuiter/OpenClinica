package org.akaza.openclinica.logic.importdata;

import org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.Iterator;

public class SubjectDataIterator implements Iterator<SubjectDataBean>, AutoCloseable {

    private XMLStreamReader reader;
    private Unmarshaller unmarshaller;
    private SubjectDataBean nextBean;
    private String studyOid;

    public SubjectDataIterator(InputStream is) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        this.reader = factory.createXMLStreamReader(is);
        JAXBContext jc = JAXBContext.newInstance(SubjectDataBean.class);
        this.unmarshaller = jc.createUnmarshaller();
        advance();
    }

    public String getStudyOid() {
        return studyOid;
    }

    private void advance() {
        nextBean = null;
        try {
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamReader.START_ELEMENT) {
                    if ("ClinicalData".equals(reader.getLocalName())) {
                        studyOid = reader.getAttributeValue(null, "StudyOID");
                    } else if ("SubjectData".equals(reader.getLocalName())) {
                        try {
                            nextBean = unmarshaller.unmarshal(reader, SubjectDataBean.class).getValue();
                            return;
                        } catch (JAXBException e) {
                            int line = reader.getLocation().getLineNumber();
                            throw new RuntimeException("Validation failure at line " + line + ": " + e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException("Error advancing stream: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean hasNext() {
        return nextBean != null;
    }

    @Override
    public SubjectDataBean next() {
        SubjectDataBean bean = nextBean;
        advance();
        return bean;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws Exception {
        if (reader != null) {
            reader.close();
        }
    }
}
