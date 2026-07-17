package org.akaza.openclinica.modern;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;

@Component
public class LegacyProxyFilter implements Filter {

    private RestTemplate restTemplate;

    public LegacyProxyFilter() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setBufferRequestBody(false);
        this.restTemplate = new RestTemplate(factory);
    }

    public LegacyProxyFilter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String uri = req.getRequestURI();

        if (uri.startsWith("/DataEntry") || uri.startsWith("/CRF") || uri.startsWith("/interop") || uri.startsWith("/api") || 
            uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui") ||
            uri.startsWith("/ListUserAccounts") || uri.startsWith("/CreateUserAccount") || 
            uri.startsWith("/EditUserAccount") || uri.startsWith("/ViewUserAccount") || 
            uri.startsWith("/DeleteUser")) {
            chain.doFilter(request, response);
            return;
        }

        String targetUrl = "http://localhost:8080" + uri;
        if (req.getQueryString() != null) {
            targetUrl += "?" + req.getQueryString();
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = req.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (headerName.equalsIgnoreCase("host") || headerName.equalsIgnoreCase("content-length")) {
                    continue;
                }
                Enumeration<String> values = req.getHeaders(headerName);
                while (values.hasMoreElements()) {
                    headers.add(headerName, values.nextElement());
                }
            }
            
            if (req.getRemoteUser() != null) {
                headers.add("REMOTE_USER", req.getRemoteUser());
            }

            RequestCallback requestCallback = requestMessage -> {
                requestMessage.getHeaders().putAll(headers);
                StreamUtils.copy(req.getInputStream(), requestMessage.getBody());
            };

            ResponseExtractor<Void> responseExtractor = responseMessage -> {
                res.setStatus(responseMessage.getStatusCode().value());
                responseMessage.getHeaders().forEach((headerName, headerValues) -> {
                    if (headerName.equalsIgnoreCase("Transfer-Encoding")) return;
                    for (String headerValue : headerValues) {
                        res.addHeader(headerName, headerValue);
                    }
                });
                StreamUtils.copy(responseMessage.getBody(), res.getOutputStream());
                return null;
            };

            restTemplate.execute(URI.create(targetUrl), HttpMethod.valueOf(req.getMethod()), requestCallback, responseExtractor);
        } catch (Exception e) {
            e.printStackTrace();
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Proxy error");
        }
    }
}
