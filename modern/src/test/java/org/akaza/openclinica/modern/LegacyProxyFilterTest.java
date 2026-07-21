package org.akaza.openclinica.modern;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import java.net.SocketTimeoutException;

import java.net.URI;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LegacyProxyFilterTest {

    @Test
    public void testDoFilter_Success() throws Exception {
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

    @Test
    public void testDoFilter_Timeout() throws Exception {
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        LegacyProxyFilter filter = new LegacyProxyFilter(restTemplate);
        
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain chain = Mockito.mock(FilterChain.class);
        
        when(request.getRequestURI()).thenReturn("/OpenClinica/legacy");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        
        when(restTemplate.execute(any(), any(), any(), any()))
            .thenThrow(new ResourceAccessException("I/O error on GET request for \"http://localhost:8080/OpenClinica/legacy\": Read timed out", new SocketTimeoutException("Read timed out")));
        
        filter.doFilter(request, response, chain);
        
        verify(response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Proxy error");
    }

    @Test
    public void testDoFilter_ConnectionRecovery() throws Exception {
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        LegacyProxyFilter filter = new LegacyProxyFilter(restTemplate);
        
        HttpServletRequest request1 = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response1 = Mockito.mock(HttpServletResponse.class);
        FilterChain chain = Mockito.mock(FilterChain.class);
        
        when(request1.getRequestURI()).thenReturn("/OpenClinica/legacy");
        when(request1.getMethod()).thenReturn("GET");
        when(request1.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        
        when(restTemplate.execute(any(), any(), any(), any()))
            .thenThrow(new ResourceAccessException("Timeout"))
            .thenReturn(null); // Success on second call
            
        // First request times out
        filter.doFilter(request1, response1, chain);
        verify(response1).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Proxy error");
        
        // Second request succeeds
        HttpServletRequest request2 = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response2 = Mockito.mock(HttpServletResponse.class);
        
        when(request2.getRequestURI()).thenReturn("/OpenClinica/legacy");
        when(request2.getMethod()).thenReturn("GET");
        when(request2.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        
        filter.doFilter(request2, response2, chain);
        
        verify(restTemplate, Mockito.times(2)).execute(
            eq(URI.create("http://localhost:8080/OpenClinica/legacy")),
            eq(HttpMethod.GET),
            any(),
            any()
        );
    }
}
