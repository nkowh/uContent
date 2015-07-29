package com.nikoyo.odata;


import org.apache.commons.io.IOUtils;
import org.apache.olingo.commons.api.ODataException;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DefaultEdmProvider extends CsdlAbstractEdmProvider {

    private final List<CsdlSchema> schemas = new ArrayList<>();

    public DefaultEdmProvider(String metadata) throws ODataException {
        try {
            //schemas.addAll(DefaultEdmxParser.parse(DefaultEdmProvider.class.getClassLoader().getResourceAsStream("odata4.xml")));
            schemas.addAll(DefaultEdmxParser.parse(metadata));
        } catch (ParserConfigurationException e) {
            throw new ODataException(e);
        } catch (IOException e) {
            throw new ODataException(e);
        } catch (SAXException e) {
            throw new ODataException(e);
        }

    }

    @Override
    public CsdlEnumType getEnumType(FullQualifiedName enumTypeName) throws ODataException {
        for (CsdlSchema schema : schemas) {
            if (schema.getNamespace().equals(enumTypeName.getNamespace())) {
                return schema.getEnumType(enumTypeName.getName());
            }
        }
        return null;
    }


    @Override
    public CsdlComplexType getComplexType(FullQualifiedName complexTypeName) throws ODataException {
        for (CsdlSchema schema : schemas) {
            if (schema.getNamespace().equals(complexTypeName.getNamespace())) {
                return schema.getComplexType(complexTypeName.getName());
            }
        }
        return null;
    }

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {

        for (CsdlSchema schema : schemas) {
            if (schema.getNamespace().equals(entityTypeName.getNamespace())) {
                CsdlEntityType entityType = schema.getEntityType(entityTypeName.getName());
                if (entityType == null) {
                    entityType = new CsdlEntityType();
                    entityType.setName(entityTypeName.getName());
                }
                return entityType;
            }
        }

        return null;
    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) throws ODataException {

        for (CsdlSchema schema : schemas) {
            if (schema.getNamespace().equals(entityContainer.getNamespace())) {
                return schema.getEntityContainer().getEntitySet(entitySetName);
            }
        }

        return null;
    }

    @Override
    public CsdlEntityContainer getEntityContainer() throws ODataException {
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        for (CsdlSchema schema : schemas) {
            CsdlEntityContainer container = schema.getEntityContainer();
            entityContainer.getEntitySets().addAll(container.getEntitySets());
        }

        return entityContainer;
    }

    @Override
    public List<CsdlSchema> getSchemas() throws ODataException {
        return schemas;
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) throws ODataException {
        CsdlSchema schema = null;
        for (CsdlSchema s : schemas) {
            if (s.getEntityContainer() != null) {
                schema = s;
            }
        }

        CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
        entityContainerInfo.setContainerName(new FullQualifiedName(schema.getNamespace(), schema.getEntityContainer().getName()));
        return entityContainerInfo;
    }


}
