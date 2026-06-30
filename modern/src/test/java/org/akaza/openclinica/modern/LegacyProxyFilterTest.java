package org.akaza.openclinica.modern;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LegacyProxyFilterTest {

    @Test
    public void testDoFilter() throws Exception {
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        LegacyProxyFilter filter = new LegacyProxyFilter(restTemplate);
        
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain chain = Mockito.mock(FilterChain.class);
        
        when(request.getRequestURI()).thenReturn("/OpenClinica/legacy");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        
        filter.doFilter(request, response, chain);
        
        verify(restTemplate).execute(
            eq(URI.create("http://localhost:8080/OpenClinica/legacy")),
            eq(HttpMethod.GET),
            any(),
            any()
        );
    }
}
