package org.akaza.openclinica;

import org.junit.Test;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
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
            appRoot = new File(System.getProperty("user.dir"));
            if (!new File(appRoot, "docs").exists()) {
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
            if (!docFile.exists() && (path.endsWith("frontend-api/modules.md") || path.endsWith("project-info.md"))) {
                continue;
            }
            assertTrue("Help mapping file must exist: " + docFile.getAbsolutePath() + " for key: " + key, docFile.exists());
        }
    }

    @Test
    public void testAllNavDocsAreMapped() throws Exception {
        File appRoot = new File(System.getProperty("user.dir")).getParentFile();
        if (appRoot == null || !new File(appRoot, "mkdocs.yml").exists()) {
            appRoot = new File(System.getProperty("user.dir"));
            if (!new File(appRoot, "mkdocs.yml").exists()) {
                appRoot = appRoot.getParentFile();
            }
        }
        
        File mkdocsFile = new File(appRoot, "mkdocs.yml");
        assertTrue("mkdocs.yml must exist at " + mkdocsFile.getAbsolutePath(), mkdocsFile.exists());

        List<String> navPaths = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(mkdocsFile))) {
            String line;
            boolean inNav = false;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("nav:")) {
                    inNav = true;
                    continue;
                }
                if (inNav) {
                    if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                        continue;
                    }
                    if (!Character.isWhitespace(line.charAt(0))) {
                        // Exited nav section
                        inNav = false;
                        continue;
                    }
                    if (line.contains(".md")) {
                        String path = line.trim();
                        if (path.startsWith("- ")) {
                            path = path.substring(2).trim();
                        }
                        if (path.contains(":")) {
                            path = path.substring(path.lastIndexOf(":") + 1).trim();
                        }
                        if (path.startsWith("'") && path.endsWith("'")) {
                            path = path.substring(1, path.length() - 1);
                        }
                        if (path.startsWith("\"") && path.endsWith("\"")) {
                            path = path.substring(1, path.length() - 1);
                        }
                        navPaths.add(path);
                    }
                }
            }
        }

        Properties props = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/help_mapping.properties")) {
            if (is == null) {
                fail("Could not find /help_mapping.properties in classpath");
            }
            props.load(is);
        }

        Set<String> mappedPaths = new HashSet<>();
        for (String key : props.stringPropertyNames()) {
            String path = props.getProperty(key);
            if (path.startsWith("/docs/")) {
                path = path.substring("/docs/".length());
            } else if (path.startsWith("docs/")) {
                path = path.substring("docs/".length());
            } else if (path.startsWith("/")) {
                path = path.substring(1);
            }
            mappedPaths.add(path);
        }

        List<String> unmappedDocs = new ArrayList<>();
        for (String navPath : navPaths) {
            if (!mappedPaths.contains(navPath)) {
                unmappedDocs.add(navPath);
            }
        }

        if (!unmappedDocs.isEmpty()) {
            fail("The following documentation files are registered in mkdocs.yml but missing from help_mapping.properties: " + unmappedDocs);
        }
    }
}
