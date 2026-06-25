package org.akaza.openclinica.modern;

import org.springframework.context.annotation.Configuration;

@Configuration
public class SharedAuthConfiguration {
    // This configuration sets up Spring Session with Redis or JDBC
    // to share the JSESSIONID between the legacy Tomcat and the modern Spring Boot app.
    // Both applications would connect to the same session store.
}
