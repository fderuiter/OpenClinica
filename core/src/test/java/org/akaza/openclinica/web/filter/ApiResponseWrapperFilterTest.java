package org.akaza.openclinica.web.filter;

import org.akaza.openclinica.bean.api.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.Assert.*;

public class ApiResponseWrapperFilterTest {

    private ApiResponseWrapperFilter filter;
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        filter = new ApiResponseWrapperFilter();
        mapper = new ObjectMapper();
    }

    @Test
    public void testSuccessfulResponseWrapping() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(javax.servlet.ServletRequest req, javax.servlet.ServletResponse res) throws IOException, ServletException {
                res.setContentType("application/json");
                res.getWriter().write("{\"message\":\"success\"}");
            }
        };

        filter.doFilter(request, response, chain);

        String content = response.getContentAsString();
        assertTrue(content.contains("\"data\":{\"message\":\"success\"}"));
    }

    @Test
    public void testUnhandledExceptionWrapping() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(javax.servlet.ServletRequest req, javax.servlet.ServletResponse res) throws IOException, ServletException {
                throw new RuntimeException("Test Exception");
            }
        };

        filter.doFilter(request, response, chain);

        String content = response.getContentAsString();
        assertEquals(500, response.getStatus());
        assertTrue(content.contains("\"errors\":["));
        assertTrue(content.contains("\"message\":\"Internal Server Error: Test Exception\""));
    }

    @Test
    public void testSendErrorWrapping() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(javax.servlet.ServletRequest req, javax.servlet.ServletResponse res) throws IOException, ServletException {
                ((javax.servlet.http.HttpServletResponse) res).sendError(404, "Not Found");
            }
        };

        filter.doFilter(request, response, chain);

        String content = response.getContentAsString();
        assertEquals(404, response.getStatus());
        assertTrue(content.contains("\"errors\":["));
        assertTrue(content.contains("\"message\":\"Not Found\""));
        assertTrue(content.contains("\"code\":\"404\""));
    }
}
