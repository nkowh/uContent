package com.nikoyo.odata;


import com.nikoyo.odata.persistence.JsonObj;
import com.nikoyo.odata.persistence.PersistenceDataService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.format.ODataFormat;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.processor.MediaEntityProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.*;
import org.apache.tika.detect.MagicDetector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;


@Service
public class DefaultEntityProcessor implements MediaEntityProcessor {

    private OData odata;

    private ServiceMetadata serviceMetadata;

    @Autowired
    private RequestContext requestContext;

    @Autowired
    private PersistenceDataService persistenceDataService;

    public DefaultEntityProcessor() {

    }

    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, SerializerException {
        EdmEntitySet edmEntitySet = UriInfoUtils.getEdmEntitySet(uriInfo);
        SelectOption selectOption = UriInfoUtils.getSelect(uriInfo);
        JsonObj entityObj = persistenceDataService.readEntity(uriInfo, odata, serviceMetadata);
        try {
            Entity respEntity = entityObj.toEntity(odata);
            EdmEntityType entityType = entityObj.getType();
            ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).suffix(ContextURL.Suffix.ENTITY).build();
            EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).select(selectOption).expand(uriInfo.getExpandOption()).build();

            ODataFormat oDataFormat = ODataFormat.fromContentType(responseFormat);
            ODataSerializer serializer = this.odata.createSerializer(oDataFormat);
            SerializerResult result = serializer.entity(serviceMetadata, entityType, respEntity, options);

            response.setContent(result.getContent());
            response.setStatusCode(HttpStatusCode.OK.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
        } catch (Exception e) {
            throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.getDefault(), e);
        }
    }

    public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, DeserializerException, SerializerException {

        EdmEntitySet edmEntitySet = UriInfoUtils.getEdmEntitySet(uriInfo);

        //写入数据
        JsonObj entityObj = persistenceDataService.createEntity(uriInfo, request, odata, serviceMetadata);

        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
        EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();

        ODataFormat oDataFormat = ODataFormat.fromContentType(responseFormat);
        ODataSerializer serializer = this.odata.createSerializer(oDataFormat);
        SerializerResult serializedResponse = serializer.entity(serviceMetadata, entityObj.getType(), entityObj.toEntity(odata), options);

        response.setContent(serializedResponse.getContent());
        response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }

    public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, DeserializerException, SerializerException {

        persistenceDataService.updateEntity(uriInfo, request, odata, serviceMetadata);
        response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());

    }

    public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException {
        persistenceDataService.deleteEntity(uriInfo, odata, serviceMetadata);
        response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
    }


    @Override
    public void readMediaEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, SerializerException {
        byte[] buffer = persistenceDataService.readMediaEntity(uriInfo, request, odata, serviceMetadata);
        try {
            MediaType mediaType = org.apache.tika.mime.MimeTypes.getDefaultMimeTypes().detect((new ByteArrayInputStream(buffer)), new Metadata());
            response.setContent(new AutoCloseInputStream(new ByteArrayInputStream(buffer)));
            response.setStatusCode(HttpStatusCode.OK.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, mediaType.toString());
        } catch (IOException e) {
            throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.getDefault(), e);
        }
    }

    @Override
    public void createMediaEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, DeserializerException, SerializerException {
        EdmEntitySet edmEntitySet = UriInfoUtils.getEdmEntitySet(uriInfo);
        //写入数据
        JsonObj entityObj = persistenceDataService.createMediaEntity(uriInfo, request, odata, serviceMetadata);


        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
        EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();

        ODataFormat oDataFormat = ODataFormat.fromContentType(responseFormat);
        ODataSerializer serializer = this.odata.createSerializer(oDataFormat);
        SerializerResult serializedResponse = serializer.entity(serviceMetadata, entityObj.getType(), entityObj.toEntity(odata), options);

        response.setContent(serializedResponse.getContent());
        response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }

    @Override
    public void updateMediaEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, DeserializerException, SerializerException {
        throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }
}
