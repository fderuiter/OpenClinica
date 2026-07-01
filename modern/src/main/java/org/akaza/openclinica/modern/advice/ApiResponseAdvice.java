package org.akaza.openclinica.modern.advice;

import org.akaza.openclinica.modern.dto.ApiError;
import org.akaza.openclinica.modern.dto.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Collections;

@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Apply to all controllers in our package
        return returnType.getDeclaringClass().getPackage().getName().startsWith("org.akaza.openclinica.modern");
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        
        // Don't double wrap
        if (body instanceof ApiResponse) {
            return body;
        }

        // If the return type is String, Spring uses StringHttpMessageConverter which casts the response to String.
        // Returning ApiResponse would cause a ClassCastException unless handled properly, but since our controllers 
        // return ResponseEntity<Object> or DTOs and we have Jackson, it will wrap them.
        // For strings, we wrap it in a string.
        if (body instanceof String) {
            return "{\"data\":\"" + body + "\"}"; // Simplified for string responses
        }

        return new ApiResponse<>(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAllExceptions(Exception ex) {
        ApiError error = new ApiError("500", "Internal Server Error: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(Collections.singletonList(error)));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(NoHandlerFoundException ex) {
        ApiError error = new ApiError("404", "Not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(Collections.singletonList(error)));
    }
}
