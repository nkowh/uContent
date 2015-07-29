package com.nikoyo.odata;

import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.format.ODataFormat;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.processor.ErrorProcessor;
import org.apache.olingo.server.api.processor.MetadataProcessor;
import org.apache.olingo.server.api.processor.ServiceDocumentProcessor;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
public class DefaultProcessor implements MetadataProcessor, ServiceDocumentProcessor, ErrorProcessor {
    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Override
    public void init(final OData odata, final ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    public void readServiceDocument(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestedContentType) throws ODataApplicationException, SerializerException {
        ODataSerializer serializer = odata.createSerializer(ODataFormat.fromContentType(requestedContentType));
        response.setContent(serializer.serviceDocument(serviceMetadata.getEdm(), request.getRawBaseUri()).getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, requestedContentType.toContentTypeString());
    }

    @Override
    public void processError(ODataRequest request, ODataResponse response, ClientServerError serverError, ContentType requestedContentType) {
        try {
            ODataSerializer serializer = odata.createSerializer(ODataFormat.fromContentType(requestedContentType));
            if (serverError.getMessage() == null) serverError.setMessage(serverError.getException().toString());
            response.setContent(serializer.error(serverError).getContent());
            response.setStatusCode(serverError.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, requestedContentType.toContentTypeString());
        } catch (Exception e) {
            // This should never happen but to be sure we have this catch here to prevent sending a stacktrace to a client.
            String responseContent =
                    "{\"error\":{\"code\":null,\"message\":\"An unexpected exception occurred during " +
                            "error processing with message: " + e.getMessage() + "\"}}";
            response.setContent(new ByteArrayInputStream(responseContent.getBytes()));
            response.setStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_JSON.toContentTypeString());
        }
    }

    @Override
    public void readMetadata(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestedContentType) throws ODataApplicationException, SerializerException {
        ODataSerializer serializer = odata.createSerializer(ODataFormat.fromContentType(requestedContentType));
        response.setContent(serializer.metadataDocument(serviceMetadata).getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, requestedContentType.toContentTypeString());
    }
}
