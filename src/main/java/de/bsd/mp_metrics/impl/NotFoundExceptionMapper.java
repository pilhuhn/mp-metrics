package de.bsd.mp_metrics.impl;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author Ken Finnigan
 */
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {
    @Override
    public Response toResponse(NotFoundException e) {
        return Response
                .status(Response.Status.NOT_FOUND)
                .entity("<h1>This is our exception page!</h1><h2>" + e.getMessage() + " </h2>")
                .type(MediaType.TEXT_HTML_TYPE)
                .build();
    }
}
