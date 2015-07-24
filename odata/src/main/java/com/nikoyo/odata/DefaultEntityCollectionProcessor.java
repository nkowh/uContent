package com.nikoyo.odata;

import com.nikoyo.odata.persistence.JsonCollection;
import com.nikoyo.odata.persistence.PersistenceDataService;
import org.apache.commons.io.IOUtils;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.format.ODataFormat;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.processor.CountEntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class DefaultEntityCollectionProcessor implements EntityCollectionProcessor, CountEntityCollectionProcessor {

    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Autowired
    private PersistenceDataService persistenceDataService;

    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, SerializerException {

        EdmEntitySet edmEntitySet = UriInfoUtils.getEdmEntitySet(uriInfo);

        JsonCollection jsonCollection = persistenceDataService.readEntityCollection(uriInfo, odata, serviceMetadata);
        ODataFormat format = ODataFormat.fromContentType(responseFormat);
        ODataSerializer serializer = odata.createSerializer(format);
        // EdmEntityType edmEntityType = entitySet.getEntities().size() > 0 ? entitySet.getEntities().get(0).get: edmEntitySet.getType();

        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).serviceRoot(URI.create(request.getRawBaseUri() + "/")).build();

        EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with()
                .contextURL(contextUrl)
                .expand(uriInfo.asUriInfoResource().getExpandOption())
                .select(uriInfo.asUriInfoResource().getSelectOption())
                .build();
        SerializerResult serializedContent = serializer.entityCollection(serviceMetadata, jsonCollection.getType(), jsonCollection.toEntityCollection(odata), opts);

        response.setContent(serializedContent.getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }


    @Override
    public void countEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, SerializerException {

        int count = persistenceDataService.countEntityCollection(uriInfo);
        response.setContent(IOUtils.toInputStream(String.valueOf(count)));
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
    }
}
