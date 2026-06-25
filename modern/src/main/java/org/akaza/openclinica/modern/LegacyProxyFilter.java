package org.akaza.openclinica.modern;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LegacyProxyFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String uri = req.getRequestURI();
        
        // If not a modern route, you would forward this to the legacy app (e.g. via RestTemplate or Apache HttpClient)
        // For demonstration, we simply log and continue the chain.
        // A full implementation would pipe the request to http://localhost:8080/OpenClinica/
        
        chain.doFilter(request, response);
    }
}
