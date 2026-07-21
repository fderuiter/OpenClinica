package org.akaza.openclinica.modern;

import org.akaza.openclinica.web.filter.ApiSecurityFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class OpenApiSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    public FilterRegistrationBean<ApiSecurityFilter> apiSecurityFilterRegistration(ApiSecurityFilter apiSecurityFilter) {
        FilterRegistrationBean<ApiSecurityFilter> registration = new FilterRegistrationBean<>(apiSecurityFilter);
        registration.setEnabled(false);
        return registration;
    }
}
