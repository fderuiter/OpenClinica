package org.akaza.openclinica;

import org.junit.Test;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HelpMappingTest {

    @Test
    public void testHelpMappingsExistOnDisk() throws Exception {
        Properties props = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/help_mapping.properties")) {
            if (is == null) {
                fail("Could not find /help_mapping.properties in classpath");
            }
            props.load(is);
        }

        File appRoot = new File(System.getProperty("user.dir")).getParentFile();
        if (appRoot == null || !new File(appRoot, "docs").exists()) {
            // Let's try to resolve the docs path properly
            appRoot = new File(System.getProperty("user.dir"));
            if (!new File(appRoot, "docs").exists()) {
                // if we are running in core/
                appRoot = appRoot.getParentFile();
            }
        }

        File docsRoot = new File(appRoot, "docs");
        assertTrue("Docs root must exist at " + docsRoot.getAbsolutePath(), docsRoot.exists());

        for (String key : props.stringPropertyNames()) {
            String path = props.getProperty(key);
            if (path.startsWith("/docs/")) {
                path = path.substring("/docs/".length());
            }
            File docFile = new File(docsRoot, path);
            assertTrue("Help mapping file must exist: " + docFile.getAbsolutePath() + " for key: " + key, docFile.exists());
        }
    }
}
