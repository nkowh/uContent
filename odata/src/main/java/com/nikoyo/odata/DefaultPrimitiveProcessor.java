package com.nikoyo.odata;

import com.nikoyo.odata.persistence.JsonCollection;
import com.nikoyo.odata.persistence.JsonObj;
import com.nikoyo.odata.persistence.PersistenceDataService;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.*;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.format.ODataFormat;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.processor.ComplexCollectionProcessor;
import org.apache.olingo.server.api.processor.ComplexProcessor;
import org.apache.olingo.server.api.processor.PrimitiveCollectionProcessor;
import org.apache.olingo.server.api.processor.PrimitiveProcessor;
import org.apache.olingo.server.api.serializer.*;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.core.uri.UriInfoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Locale;

@Service
public class DefaultPrimitiveProcessor implements PrimitiveProcessor, PrimitiveCollectionProcessor, ComplexProcessor, ComplexCollectionProcessor {

    private OData odata;

    private ServiceMetadata serviceMetadata;

    @Autowired
    private PersistenceDataService persistenceDataService;

    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    public void readPrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, SerializerException {
        read(response, uriInfo, responseFormat, false, false);
    }

    public void updatePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat)
            throws ODataApplicationException, DeserializerException, SerializerException {
        throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

    public void deletePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException {
        throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

    @Override
    public void readPrimitiveCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, SerializerException {
        read(response, uriInfo, responseFormat, false, true);
    }

    @Override
    public void updatePrimitiveCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, DeserializerException, SerializerException {
        throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

    @Override
    public void deletePrimitiveCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException {
        throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

    @Override
    public void readComplexCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, SerializerException {
        read(response, uriInfo, responseFormat, true, true);
    }

    @Override
    public void updateComplexCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, DeserializerException, SerializerException {
        throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

    @Override
    public void deleteComplexCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException {
        throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

    @Override
    public void readComplex(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, SerializerException {
        read(response, uriInfo, responseFormat, true, false);
    }

    @Override
    public void updateComplex(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, DeserializerException, SerializerException {
        throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

    @Override
    public void deleteComplex(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException {
        throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }


    private void read(ODataResponse response, UriInfo uriInfo, ContentType responseFormat, boolean isComplex, boolean isCollection) throws ODataApplicationException, SerializerException {

        EdmEntitySet edmEntitySet = UriInfoUtils.getEdmEntitySet(uriInfo.asUriInfoResource());

        UriResourceProperty uriResourceProperty = (UriResourceProperty) ((UriInfoImpl) uriInfo).getLastResourcePart();
        EdmProperty edmProperty = uriResourceProperty.getProperty();
        EdmType edmPropertyType = edmProperty.getType();

        JsonObj entityObj = persistenceDataService.readEntity(uriInfo, odata, serviceMetadata);
        Entity entity = entityObj.toEntity(odata);
        if (entity == null)
            throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);


        Property property = entity.getProperty(edmProperty.getName());
        if (property == null)
            throw new ODataApplicationException("Property not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);


        Object value = property.getValue();
        if (value == null)
            response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());


        ODataFormat format = ODataFormat.fromContentType(responseFormat);
        ODataSerializer serializer = odata.createSerializer(format);

        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).navOrPropertyPath(edmProperty.getName()).build();
        SerializerResult serializerResult;
        if (isComplex) {
            ComplexSerializerOptions options = ComplexSerializerOptions.with().contextURL(contextUrl).build();
            serializerResult = isCollection
                    ? serializer.complexCollection(serviceMetadata, (EdmComplexType) edmPropertyType, property, options)
                    : serializer.complex(serviceMetadata, (EdmComplexType) edmPropertyType, property, options);
        } else {
            PrimitiveSerializerOptions options = PrimitiveSerializerOptions.with().contextURL(contextUrl).build();
            serializerResult = isCollection
                    ? serializer.primitiveCollection((EdmPrimitiveType) edmPropertyType, property, options)
                    : serializer.primitive((EdmPrimitiveType) edmPropertyType, property, options);
        }


        InputStream propertyStream = serializerResult.getContent();
        response.setContent(propertyStream);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());

    }
}
