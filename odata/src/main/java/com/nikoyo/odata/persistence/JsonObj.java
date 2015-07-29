package com.nikoyo.odata.persistence;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.api.format.ODataFormat;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.uri.UriParameter;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.SQLException;
import java.util.*;

public class JsonObj extends HashMap<String, Object> {

    final static ObjectMapper mapper = new ObjectMapper();

    public static JsonObj parse(byte[] bytes, EdmEntityType edmEntityType) throws ODataApplicationException {
        try {
            JsonObj jsonObj = mapper.readValue(bytes, JsonObj.class);
            jsonObj.setEdmEntityType(edmEntityType);
            return jsonObj;
        } catch (IOException e) {
            throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.getDefault(), e);
        }
    }

    public static JsonObj parse(Map<String, Object> source, EdmEntityType edmEntityType) throws ODataApplicationException {
        JsonObj jsonObj = new JsonObj();
        jsonObj.putAll(source);
        jsonObj.setEdmEntityType(edmEntityType);
        return jsonObj;
    }

    public static JsonObj parse(String jsongString, EdmEntityType edmEntityType) throws ODataApplicationException {
        try {
            JsonObj jsonObj = mapper.readValue(jsongString, JsonObj.class);
            jsonObj.setEdmEntityType(edmEntityType);
            return jsonObj;
        } catch (IOException e) {
            throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.getDefault(), e);
        }
    }

    public static JsonObj parse(List<UriParameter> parameters, EdmEntityType edmEntityType) {
        JsonObj jsonObj = new JsonObj(edmEntityType);
        for (UriParameter parameter : parameters) {
            if (!parameter.getText().startsWith("'"))
                jsonObj.put(parameter.getName(), Integer.valueOf(parameter.getText()));
            else
                jsonObj.put(parameter.getName(), parameter.getText().replaceAll("'", ""));
        }
        return jsonObj;
    }

    public static JsonObj simple() {
        return new JsonObj();
    }

    private EdmEntityType edmEntityType;

    JsonObj() {

    }

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JsonObj(EdmEntityType edmEntityType) {
        this.edmEntityType = edmEntityType;
    }

    public EdmEntityType getType() {
        return edmEntityType;
    }

    private JsonObj setEdmEntityType(EdmEntityType edmEntityType) {
        this.edmEntityType = edmEntityType;
        return this;
    }

    public JsonObj getKeyObj() {
        JsonObj keys = new JsonObj(edmEntityType);
        for (String s : edmEntityType.getKeyPredicateNames()) {
            keys.put(s, this.get(s));
        }
        keys.setId(this.getId());
        return keys;
    }

    public String toBase64() throws ODataApplicationException {

        try {
            return new String(Base64.encodeBase64(mapper.writeValueAsBytes(this)));
        } catch (JsonProcessingException e) {
            throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.getDefault(), e);
        }
    }

    public BoolQueryBuilder toQuery() {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for (String key : this.keySet()) {
            boolQueryBuilder.must(QueryBuilders.termQuery(key, this.get(key)));
        }

        return boolQueryBuilder;
    }

    private String toJson() throws ODataApplicationException {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.getDefault());
        }
    }

    public Entity toEntity(OData odata) throws ODataApplicationException {
        try {
            Object $stream = this.remove("$stream");

            ODataDeserializer deserializer = odata.createDeserializer(ODataFormat.JSON);
            Entity entity = deserializer.entity(toInputStream(), edmEntityType).getEntity();
            entity.setType(this.getType().getFullQualifiedName().getFullQualifiedNameAsString());
            if ($stream != null) {
                entity.setMediaContentSource(URI.create("$value"));
            }
            return entity;
        } catch (Exception e) {
            throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.getDefault());
        }

    }

    public boolean isKey(String propertyName) {
        List<EdmKeyPropertyRef> keyPropertyRefs = edmEntityType.getKeyPropertyRefs();
        for (EdmKeyPropertyRef propRef : keyPropertyRefs) {
            String keyPropertyName = propRef.getName();
            if (keyPropertyName.equals(propertyName)) {
                return true;
            }
        }
        return false;
    }


    public InputStream toInputStream() throws ODataApplicationException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(mapper.writeValueAsBytes(this));
            return new AutoCloseInputStream(inputStream);
        } catch (JsonProcessingException e) {
            throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.getDefault(), e);
        }
    }
}
