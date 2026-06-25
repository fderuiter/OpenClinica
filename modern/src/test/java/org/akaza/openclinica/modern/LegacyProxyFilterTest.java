package org.akaza.openclinica.modern;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LegacyProxyFilterTest {

    @Test
    public void testDoFilter() throws Exception {
        LegacyProxyFilter filter = new LegacyProxyFilter();
        
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain chain = Mockito.mock(FilterChain.class);
        
        when(request.getRequestURI()).thenReturn("/OpenClinica/legacy");
        
        filter.doFilter(request, response, chain);
        
        verify(chain).doFilter(request, response);
    }
}
