package org.akaza.openclinica.web.restful;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionMapper.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Response toResponse(Exception exception) {
        LOGGER.error("REST API Exception", exception);

        // Standardized error response object
        ObjectNode errorObj = mapper.createObjectNode();
        errorObj.put("status", "error");
        errorObj.put("code", 500);
        errorObj.put("message", exception.getMessage() != null ? exception.getMessage() : "An unexpected system error occurred.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorObj.toString())
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
