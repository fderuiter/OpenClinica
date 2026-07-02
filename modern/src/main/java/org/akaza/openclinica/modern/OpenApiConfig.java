package org.akaza.openclinica.modern;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import java.nio.charset.StandardCharsets;
import io.swagger.v3.core.util.Json;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        OpenAPI openAPI = new OpenAPI();
        try {
            ClassPathResource resource = new ClassPathResource("static/openapi.json");
            if (resource.exists()) {
                String legacyJson = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                OpenAPI legacyOpenAPI = Json.mapper().readValue(legacyJson, OpenAPI.class);
                
                if (legacyOpenAPI.getPaths() != null) {
                    openAPI.setPaths(legacyOpenAPI.getPaths());
                }
                if (legacyOpenAPI.getComponents() != null) {
                    openAPI.setComponents(legacyOpenAPI.getComponents());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return openAPI;
    }
}
