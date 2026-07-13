package org.akaza.openclinica.modern.advice;

import org.akaza.openclinica.sdk.dto.ApiError;
import org.akaza.openclinica.sdk.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Collections;

@RestControllerAdvice
public class ApiResponseAdvice {

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
