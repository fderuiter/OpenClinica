package org.akaza.openclinica.modern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

import java.time.Duration;

@Configuration
@EnableJdbcHttpSession
public class SharedAuthConfiguration {

    @Bean
    public SessionRepositoryCustomizer<JdbcIndexedSessionRepository> customizer(
            @Value("${maxInactiveInterval:3600}") int maxInactiveInterval) {
        return repository -> repository.setDefaultMaxInactiveInterval(Duration.ofSeconds(maxInactiveInterval));
    }
}
