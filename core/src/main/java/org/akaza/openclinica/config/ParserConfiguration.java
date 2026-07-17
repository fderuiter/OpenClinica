package org.akaza.openclinica.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class ParserConfiguration {

    @Bean(name = "standardObjectMapper")
    @Primary
    public ObjectMapper standardObjectMapper() {
        return new ObjectMapper();
    }

    @Bean(name = "permissiveObjectMapper")
    public ObjectMapper permissiveObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}
