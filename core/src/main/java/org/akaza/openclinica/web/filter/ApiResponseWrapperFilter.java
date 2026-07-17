package org.akaza.openclinica.web.filter;

import org.akaza.openclinica.sdk.dto.ApiError;
import org.akaza.openclinica.sdk.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

public class ApiResponseWrapperFilter implements Filter {

    private ObjectMapper mapper;

    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String uri = req.getRequestURI();

        // Check if the request is an API call
        if (isApiRequest(uri)) {
            ApiErrorResponseWrapper wrapper = new ApiErrorResponseWrapper(res);
            try {
                chain.doFilter(request, wrapper);
                
                // If sendError was called, it already wrote the response directly to the original stream.
                // We only need to wrap successful or normal responses that wrote to our capture buffer.
                if (!wrapper.errorSent) {
                    byte[] responseData = wrapper.getCaptureAsBytes();
                    int status = wrapper.getTrackedStatus();
                    
                    if (status >= 200 && status < 400) {
                        if (responseData.length > 0) {
                            String contentType = wrapper.getContentType();
                            if (contentType != null && contentType.contains("application/json")) {
                                // Parse JSON and wrap
                                com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(new java.io.ByteArrayInputStream(responseData));
                                // Avoid double wrapping if it already has "data" or "errors"
                                if (jsonNode.isObject() && (jsonNode.has("data") || jsonNode.has("errors"))) {
                                    writeToOriginal(res, responseData);
                                } else {
                                    ApiResponse<Object> apiResponse = new ApiResponse<>(jsonNode);
                                    writeToOriginal(res, mapper.writeValueAsBytes(apiResponse));
                                }
                            } else {
                                // Non-JSON response, just wrap it as a string
                                String text = new String(responseData, wrapper.getCharacterEncoding() != null ? wrapper.getCharacterEncoding() : "UTF-8");
                                ApiResponse<Object> apiResponse = new ApiResponse<>(text);
                                res.setContentType("application/json;charset=UTF-8");
                                writeToOriginal(res, mapper.writeValueAsBytes(apiResponse));
                            }
                        } else {
                            ApiResponse<Object> apiResponse = new ApiResponse<>(null);
                            res.setContentType("application/json;charset=UTF-8");
                            writeToOriginal(res, mapper.writeValueAsBytes(apiResponse));
                        }
                    } else if (status >= 400) {
                        if (responseData.length > 0) {
                            // It's an error status but they wrote directly to the output stream (didn't use sendError)
                            String contentType = wrapper.getContentType();
                            if (contentType != null && contentType.contains("application/json")) {
                                com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(new java.io.ByteArrayInputStream(responseData));
                                if (jsonNode.isObject() && (jsonNode.has("data") || jsonNode.has("errors"))) {
                                    writeToOriginal(res, responseData);
                                } else {
                                    ApiError error = new ApiError(String.valueOf(status), jsonNode.toString());
                                    ApiResponse<Object> apiResponse = new ApiResponse<>(Collections.singletonList(error));
                                    writeToOriginal(res, mapper.writeValueAsBytes(apiResponse));
                                }
                            } else {
                                String text = new String(responseData, wrapper.getCharacterEncoding() != null ? wrapper.getCharacterEncoding() : "UTF-8");
                                ApiError error = new ApiError(String.valueOf(status), text);
                                ApiResponse<Object> apiResponse = new ApiResponse<>(Collections.singletonList(error));
                                res.setContentType("application/json;charset=UTF-8");
                                writeToOriginal(res, mapper.writeValueAsBytes(apiResponse));
                            }
                        } else {
                            ApiError error = new ApiError(String.valueOf(status), "Error " + status);
                            ApiResponse<Object> apiResponse = new ApiResponse<>(Collections.singletonList(error));
                            res.setContentType("application/json;charset=UTF-8");
                            writeToOriginal(res, mapper.writeValueAsBytes(apiResponse));
                        }
                    } else {
                        writeToOriginal(res, responseData);
                    }
                }
            } catch (Throwable t) {
                // Catch unhandled exceptions
                wrapper.sendError(500, "Internal Server Error: " + t.getMessage());
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private void writeToOriginal(HttpServletResponse response, byte[] data) throws IOException {
        response.setContentLength(data.length);
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(data);
        outputStream.flush();
    }

    private boolean isApiRequest(String uri) {
        return uri.contains("/api/") || uri.contains("/rest/") || uri.endsWith("/api") || uri.endsWith("/rest");
    }

    @Override
    public void destroy() {
    }

    private class ApiErrorResponseWrapper extends HttpServletResponseWrapper {
        private boolean errorSent = false;
        private HttpServletResponse original;
        private java.io.ByteArrayOutputStream capture;
        private ServletOutputStream output;
        private PrintWriter writer;
        private int status = 200;

        public ApiErrorResponseWrapper(HttpServletResponse response) {
            super(response);
            this.original = response;
            this.capture = new java.io.ByteArrayOutputStream();
        }

        @Override
        public void setStatus(int sc) {
            super.setStatus(sc);
            this.status = sc;
        }


        public int getTrackedStatus() {
            return this.status;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (writer != null) {
                throw new IllegalStateException("getWriter() has already been called on this response.");
            }

            if (output == null) {
                output = new ServletOutputStream() {
                    @Override
                    public void write(int b) throws IOException {
                        capture.write(b);
                    }
                    @Override
                    public void setWriteListener(jakarta.servlet.WriteListener writeListener) {}
                    @Override
                    public boolean isReady() { return true; }

                    @Override
                    public void write(byte[] b, int off, int len) throws IOException {
                        capture.write(b, off, len);
                    }
                };
            }
            return output;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (output != null) {
                throw new IllegalStateException("getOutputStream() has already been called on this response.");
            }

            if (writer == null) {
                String encoding = getResponse().getCharacterEncoding();
                if (encoding == null) {
                    encoding = "UTF-8";
                }
                writer = new PrintWriter(new java.io.OutputStreamWriter(capture, encoding));
            }
            return writer;
        }

        public byte[] getCaptureAsBytes() throws IOException {
            if (writer != null) {
                writer.flush();
            }
            if (output != null) {
                output.flush();
            }
            return capture.toByteArray();
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            if (errorSent) return;
            errorSent = true;
            writeErrorResponse(sc, msg);
        }

        @Override
        public void sendError(int sc) throws IOException {
            if (errorSent) return;
            errorSent = true;
            writeErrorResponse(sc, "Error " + sc);
        }

        private void writeErrorResponse(int sc, String msg) throws IOException {
            original.setStatus(sc);
            original.setContentType("application/json;charset=UTF-8");
            ApiError error = new ApiError(String.valueOf(sc), msg);
            ApiResponse<Object> apiResponse = new ApiResponse<>(Collections.singletonList(error));
            PrintWriter writer = original.getWriter();
            mapper.writeValue(writer, apiResponse);
            writer.flush();
        }
    }
}
