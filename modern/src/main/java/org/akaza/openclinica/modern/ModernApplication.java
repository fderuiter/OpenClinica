package org.akaza.openclinica.modern;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(exclude = {LiquibaseAutoConfiguration.class, QuartzAutoConfiguration.class})
@ImportResource("classpath*:org/akaza/openclinica/applicationContext-core-*.xml")
@ComponentScan({"org.akaza.openclinica.modern", "org.akaza.openclinica.repository"})
@EntityScan({"org.akaza.openclinica.domain", "org.akaza.openclinica.modern"})
public class ModernApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModernApplication.class, args);
    }
}
