package org.akaza.openclinica.domain.xform;

import java.io.Reader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import javax.xml.transform.sax.SAXSource;

import javax.sql.DataSource;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.domain.xform.dto.Html;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

public class XformParser {
    private DataSource dataSource = null;
    protected final Logger log = LoggerFactory.getLogger(XformParser.class);
    private CoreResources coreResources;

    public String marshall(Html html) throws Exception {
        StringWriter writer = new StringWriter();

        JAXBContext context = JAXBContext.newInstance(Html.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        
        // JAXB doesn't have setNamespaceMapping, we just marshal it.
        // It relies on package-info or annotations for namespace prefixes.
        marshaller.marshal(html, writer);
        String xform = writer.toString();
        return xform;
    }

    public Html unMarshall(String xml) throws Exception {
        JAXBContext context = JAXBContext.newInstance(Html.class);
        Reader reader = new StringReader(xml);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        XMLReader xmlReader = spf.newSAXParser().getXMLReader();
        InputSource inputSource = new InputSource(reader);
        SAXSource saxSource = new SAXSource(xmlReader, inputSource);
        
        Html html = (Html) unmarshaller.unmarshal(saxSource);
        reader.close();
        return html;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public CoreResources getCoreResources() {
        return coreResources;
    }

    public void setCoreResources(CoreResources coreResources) {
        this.coreResources = coreResources;
    }

}
