package org.akaza.openclinica.web.restful;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        LOGGER.error("REST API Exception", exception);

        // Standardized error response object
        JSONObject errorObj = new JSONObject();
        errorObj.put("status", "error");
        errorObj.put("code", 500);
        errorObj.put("message", exception.getMessage() != null ? exception.getMessage() : "An unexpected system error occurred.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorObj.toString())
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
