package org.akaza.openclinica.modern.advice;

import org.akaza.openclinica.modern.dto.ApiResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.NoHandlerFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ApiResponseAdviceTest {

    private ApiResponseAdvice advice;

    @Before
    public void setUp() {
        advice = new ApiResponseAdvice();
    }

    @Test
    public void testHandleAllExceptions() {
        Exception ex = new RuntimeException("Test exception");
        ResponseEntity<ApiResponse<Object>> response = advice.handleAllExceptions(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("500", response.getBody().getErrors().get(0).getCode());
        assertEquals("Internal Server Error: Test exception", response.getBody().getErrors().get(0).getMessage());
    }

    @Test
    public void testHandleNotFound() {
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/api/test", null);
        ResponseEntity<ApiResponse<Object>> response = advice.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("404", response.getBody().getErrors().get(0).getCode());
        assertEquals("Not Found", response.getBody().getErrors().get(0).getMessage());
    }
}

