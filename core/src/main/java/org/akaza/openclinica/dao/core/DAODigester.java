/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.core;

import org.akaza.openclinica.dao.core.xml.Queries;
import org.akaza.openclinica.dao.core.xml.Query;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * <P>
 * Parses SQL queries in XML and then
 * stores them in a hashmap, to be accessed later. Idea is to create one XML
 * file per Data Access Object, so that SQL syntax can be abstracted out of the
 * Java JDBC code.
 * </P>
 *
 * @author thickerson
 *
 * TODO
 */
public class DAODigester {

    private final HashMap<String, String> queries = new HashMap<String, String>();
    private InputStream fis;

    public void run() throws IOException, SAXException {
        try {
            JAXBContext context = JAXBContext.newInstance(Queries.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            XMLReader xmlReader = spf.newSAXParser().getXMLReader();
            InputSource inputSource = new InputSource(fis);
            SAXSource source = new SAXSource(xmlReader, inputSource);

            Queries parsedQueries = (Queries) unmarshaller.unmarshal(source);
            if (parsedQueries != null && parsedQueries.getQueryList() != null) {
                for (Query query : parsedQueries.getQueryList()) {
                    setQuery(query.getName(), query.getSql());
                }
            }
        } catch (JAXBException | ParserConfigurationException e) {
            throw new SAXException("Error parsing queries XML", e);
        }
    }

    public void setQuery(String name, String query) {
        queries.put(name, query);
    }

    public String getQuery(String name) {
        return queries.get(name);
    }

    public void setInputStream(InputStream fis) {
        this.fis = fis;
    }

}
