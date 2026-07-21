package org.akaza.openclinica.i18n;

import org.junit.Test;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TranslationKeyUniquenessTest {

    @Test
    public void testNoDuplicateKeysInPropertiesFiles() throws IOException {
        File appRoot = new File(System.getProperty("user.dir"));
        File i18nDir = new File(appRoot, "src/main/resources/org/akaza/openclinica/i18n");
        if (!i18nDir.exists()) {
            i18nDir = new File(appRoot, "core/src/main/resources/org/akaza/openclinica/i18n");
        }
        
        assertTrue("i18n directory must exist at " + i18nDir.getAbsolutePath(), i18nDir.exists());
        assertTrue("i18n must be a directory", i18nDir.isDirectory());

        File[] files = i18nDir.listFiles((dir, name) -> name.endsWith(".properties"));
        if (files == null || files.length == 0) {
            fail("No properties files found in " + i18nDir.getAbsolutePath());
        }

        StringBuilder errorMessage = new StringBuilder();
        Pattern keyPattern = Pattern.compile("^([^=:]+?)\\s*[=:]");

        for (File file : files) {
            Set<String> seenKeys = new HashSet<>();
            Set<String> duplicateKeys = new HashSet<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                boolean isContinuation = false;
                
                while ((line = reader.readLine()) != null) {
                    String stripped = line.trim();
                    if (!isContinuation) {
                        if (stripped.isEmpty() || stripped.startsWith("#") || stripped.startsWith("!")) {
                            continue;
                        }
                        
                        Matcher matcher = keyPattern.matcher(stripped);
                        if (matcher.find()) {
                            String key = matcher.group(1).trim();
                            if (!seenKeys.add(key)) {
                                duplicateKeys.add(key);
                            }
                        }
                    }
                    isContinuation = stripped.endsWith("\\");
                }
            }

            if (!duplicateKeys.isEmpty()) {
                errorMessage.append("File: ").append(file.getName())
                        .append(" has duplicate keys: ").append(duplicateKeys).append("\n");
            }
        }

        if (errorMessage.length() > 0) {
            fail("Found duplicate keys in translation files:\n" + errorMessage.toString());
        }
    }
}
