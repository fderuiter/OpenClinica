package org.akaza.openclinica.service.rule;

import org.junit.Test;
import org.xml.sax.SAXException;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RuleDocumentationValidatorTest {

    @Test
    public void testDocumentationSnippets() {
        File xsdFile = new File("src/main/resources/properties/rules.xsd");
        assertTrue("XSD file must exist", xsdFile.exists());

        File mdFile = new File("../../docs/diataxis/references/rules.md");
        if (!mdFile.exists()) {
            // Depending on where tests are run (from core/ or from root)
            mdFile = new File("../docs/diataxis/references/rules.md");
            if (!mdFile.exists()) {
                mdFile = new File("docs/diataxis/references/rules.md");
            }
        }
        assertTrue("Documentation file must exist", mdFile.exists());

        try {
            String content = new String(Files.readAllBytes(mdFile.toPath()));
            List<String> snippets = extractXmlSnippets(content);
            assertTrue("Should find at least one XML snippet", snippets.size() > 0);

            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(xsdFile);

            for (int i = 0; i < snippets.size(); i++) {
                String snippet = snippets.get(i);
                
                Validator validator = schema.newValidator();
                try {
                    validator.validate(new StreamSource(new StringReader(snippet)));
                } catch (SAXException e) {
                    fail("Snippet " + i + " failed validation: " + e.getMessage() + "\nSnippet:\n" + snippet);
                }
            }

        } catch (IOException e) {
            fail("Failed to read documentation file: " + e.getMessage());
        } catch (SAXException e) {
            fail("Failed to parse XSD schema: " + e.getMessage());
        }
    }

    private List<String> extractXmlSnippets(String content) {
        List<String> snippets = new ArrayList<>();
        Pattern pattern = Pattern.compile("```xml\\s+(.*?)\\s+```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            snippets.add(matcher.group(1));
        }
        return snippets;
    }
}
