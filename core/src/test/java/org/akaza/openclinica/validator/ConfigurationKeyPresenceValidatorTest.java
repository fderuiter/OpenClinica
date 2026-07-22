package org.akaza.openclinica.validator;

import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConfigurationKeyPresenceValidatorTest {

    @Test
    public void testAllConfigurationKeysAndTablesDocumented() throws Exception {
        // Find docs directory
        Path baseDir = Paths.get(".").toAbsolutePath().normalize();
        while (baseDir != null && !Files.exists(baseDir.resolve("docs"))) {
            baseDir = baseDir.getParent();
        }
        
        if (baseDir == null) {
            fail("Could not find docs directory");
        }

        Path docsDir = baseDir.resolve("docs");
        Path installationMd = docsDir.resolve("tutorials/installation.md");
        Path upgradeMd = docsDir.resolve("how-to/upgrade.md");

        assertTrue("Installation guide must exist", Files.exists(installationMd));
        assertTrue("Upgrade guide must exist", Files.exists(upgradeMd));

        String installContent = new String(Files.readAllBytes(installationMd));
        String upgradeContent = new String(Files.readAllBytes(upgradeMd));
        String combinedDocs = installContent + "\n" + upgradeContent;

        Set<String> keys = extractConfigurationKeys(baseDir);
        Set<String> tables = extractDatabaseTables(baseDir);

        List<String> undocumentedKeys = new ArrayList<>();
        for (String key : keys) {
            if (!combinedDocs.contains(key)) {
                undocumentedKeys.add(key);
            }
        }

        List<String> undocumentedTables = new ArrayList<>();
        for (String table : tables) {
            if (!combinedDocs.contains(table)) {
                undocumentedTables.add(table);
            }
        }

        if (!undocumentedKeys.isEmpty() || !undocumentedTables.isEmpty()) {
            StringBuilder sb = new StringBuilder("Undocumented configuration keys or database tables found:\n");
            if (!undocumentedKeys.isEmpty()) {
                sb.append("Missing configuration keys:\n");
                undocumentedKeys.forEach(k -> sb.append("- ").append(k).append("\n"));
            }
            if (!undocumentedTables.isEmpty()) {
                sb.append("Missing database tables:\n");
                undocumentedTables.forEach(t -> sb.append("- ").append(t).append("\n"));
            }
            fail(sb.toString());
        }
    }

    private Set<String> extractConfigurationKeys(Path baseDir) throws IOException {
        Set<String> keys = new TreeSet<>();
        try (Stream<Path> paths = Files.walk(baseDir)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".properties"))
                 .filter(p -> !p.toString().contains("/target/") && !p.toString().contains("/test/") && !p.toString().contains("node_modules"))
                 .filter(p -> {
                     String name = p.getFileName().toString();
                     return name.equals("datainfo.properties") || name.equals("application.properties") || name.equals("extract.properties");
                 })
                 .forEach(p -> {
                     try {
                         List<String> lines = Files.readAllLines(p);
                         for (String line : lines) {
                             line = line.trim();
                             if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
                                 String key = line.split("=", 2)[0].trim();
                                 // Ignore private/internal keys
                                 if (!key.isEmpty() && !key.startsWith("spring.") && !key.startsWith("server.") && !key.startsWith("management.")) {
                                     keys.add(key);
                                 }
                             }
                         }
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 });
        }
        return keys;
    }

    private Set<String> extractDatabaseTables(Path baseDir) throws IOException {
        Set<String> tables = new TreeSet<>();
        Pattern tablePattern = Pattern.compile("CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?([a-zA-Z0-9_]+)", Pattern.CASE_INSENSITIVE);
        try (Stream<Path> paths = Files.walk(baseDir)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".sql"))
                 .filter(p -> !p.toString().contains("/target/") && !p.toString().contains("/test/") && !p.toString().contains("node_modules"))
                 .forEach(p -> {
                     try {
                         String content = new String(Files.readAllBytes(p));
                         Matcher matcher = tablePattern.matcher(content);
                         while (matcher.find()) {
                             String table = matcher.group(1);
                             // Ignore internal Spring tables
                             if (!table.toUpperCase().startsWith("SPRING_")) {
                                 tables.add(table);
                             }
                         }
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 });
        }
        return tables;
    }
}
