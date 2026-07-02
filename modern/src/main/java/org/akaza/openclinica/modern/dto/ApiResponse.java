package org.akaza.openclinica.modern.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiResponse<T> {
    private T data;
    private List<ApiError> errors = new ArrayList<>();
    private Map<String, Object> meta = new HashMap<>();

    public ApiResponse() {}

    public ApiResponse(T data) {
        this.data = data;
    }

    public ApiResponse(List<ApiError> errors) {
        this.errors = errors;
    }

    public ApiResponse(T data, Map<String, Object> meta) {
        this.data = data;
        this.meta = meta;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<ApiError> getErrors() {
        return errors;
    }

    public void setErrors(List<ApiError> errors) {
        this.errors = errors;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }
}
