package com.nikoyo.odata.persistence;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;

import java.util.ArrayList;


public class JsonCollection extends ArrayList<JsonObj> {

    EdmEntityType type;

    public EntityCollection toEntityCollection(OData oData) throws ODataApplicationException {
        EntityCollection entityCollection = new EntityCollection();
        for (JsonObj jsonObj : this) {
            entityCollection.getEntities().add(jsonObj.toEntity(oData));
        }

        return entityCollection;
    }

    public EdmEntityType getType() {
        return type;
    }

    public void setType(EdmEntityType type) {
        this.type = type;
    }
}
