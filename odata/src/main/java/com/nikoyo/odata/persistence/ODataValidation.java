package com.nikoyo.odata.persistence;


import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ODataFormat;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

public abstract class ODataValidation {

    static public void entity(OData odata, EdmEntityType edmEntityType, byte[] buffer) throws ODataApplicationException {
        try {
            ODataDeserializer deserializer = odata.createDeserializer(ODataFormat.JSON);
            //deserializer.entity(wrap(buffer), edmEntityType);
        } catch (DeserializerException e) {
            throw new ODataApplicationException(e.getMessage(), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault(), e);
        }
    }

    static private InputStream wrap(byte[] buffer) {
        return new AutoCloseInputStream(new ByteArrayInputStream(buffer));
    }
}
