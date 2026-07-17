package org.akaza.openclinica.modern;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;

import org.springframework.context.annotation.ImportResource;

@SpringBootApplication(exclude = {LiquibaseAutoConfiguration.class, SessionAutoConfiguration.class, QuartzAutoConfiguration.class})
public class ModernApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModernApplication.class, args);
    }
}
