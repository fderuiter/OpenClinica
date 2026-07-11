package org.akaza.openclinica.modern;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;

import java.io.File;

@SpringBootApplication(exclude = {LiquibaseAutoConfiguration.class, SessionAutoConfiguration.class, QuartzAutoConfiguration.class})
public class ModernApplication {

    private static String getExtConfigDirectory() {
        String dir = System.getenv("OPENCLINICA_CONF_DIR");
        if (dir == null) dir = System.getProperty("OPENCLINICA_CONF_DIR");
        if (dir == null) {
            String catalina = System.getenv("CATALINA_HOME");
            if (catalina == null) catalina = System.getProperty("catalina.home");
            if (catalina != null) {
                dir = catalina + "/openclinica.config";
            }
        }
        if (dir == null || dir.isEmpty()) {
            throw new RuntimeException("CRITICAL ERROR: External configuration directory is not configured.");
        }
        File f = new File(dir);
        if (!f.exists() || !f.isDirectory()) {
            throw new RuntimeException("CRITICAL ERROR: External configuration directory is missing or invalid: " + dir);
        }
        return dir;
    }

    public static void main(String[] args) {
        String configDir = getExtConfigDirectory();
        System.setProperty("spring.config.additional-location", "optional:file:" + configDir + "/");
        SpringApplication.run(ModernApplication.class, args);
    }
}
