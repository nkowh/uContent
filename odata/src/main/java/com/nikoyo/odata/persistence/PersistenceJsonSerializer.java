package com.nikoyo.odata.persistence;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.*;
import org.apache.olingo.commons.api.edm.*;
import org.apache.olingo.commons.core.edm.primitivetype.EdmPrimitiveTypeFactory;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.core.serializer.utils.CircleStreamBuffer;
import org.apache.olingo.server.core.serializer.utils.ExpandSelectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PersistenceJsonSerializer {

    private static final Logger log = LoggerFactory.getLogger(PersistenceJsonSerializer.class);


    public Map getKeyPredicates(final EdmEntityType entityType, final Entity entity) {

        Map<String, Object> map = new HashMap<>();
        for (EdmKeyPropertyRef edmKeyPropertyRef : entityType.getKeyPropertyRefs()) {
            Property property = entity.getProperty(edmKeyPropertyRef.getProperty().getName());
            map.put(edmKeyPropertyRef.getProperty().getName(), property.getValue());
        }

        return map;
    }

    public InputStream entity(final ServiceMetadata metadata, final EdmEntityType entityType,
                              final Entity entity, final EntitySerializerOptions options) throws SerializerException {
        CircleStreamBuffer buffer = new CircleStreamBuffer();
        try {
            JsonGenerator json = new JsonFactory().createGenerator(buffer.getOutputStream());
            writeEntity(metadata, entityType, entity,
                    options == null ? null : options.getExpand(),
                    options == null ? null : options.getSelect(),
                    options == null ? false : options.onlyReferences(), json);
            json.close();
        } catch (final IOException e) {
            throw new SerializerException("An I/O exception occurred.", e,
                    SerializerException.MessageKeys.IO_EXCEPTION);
        }

        return buffer.getInputStream();
    }


    private void writeEntitySet(final ServiceMetadata metadata, final EdmEntityType entityType,
                                final EntityCollection entitySet, final ExpandOption expand, final SelectOption select,
                                final boolean onlyReference, final JsonGenerator json) throws IOException,
            SerializerException {
        json.writeStartArray();
        for (final Entity entity : entitySet.getEntities()) {
            if (onlyReference) {
                json.writeStartObject();
                json.writeStringField(Constants.JSON_ID, entity.getId().toASCIIString());
                json.writeEndObject();
            } else {
                writeEntity(metadata, entityType, entity, expand, select, false, json);
            }
        }
        json.writeEndArray();
    }

    protected void writeEntity(final ServiceMetadata metadata, final EdmEntityType entityType,
                               final Entity entity, final ExpandOption expand,
                               final SelectOption select, final boolean onlyReference, final JsonGenerator json)
            throws IOException, SerializerException {
        json.writeStartObject();
        if (entity.getETag() != null) {
            json.writeStringField(Constants.JSON_ETAG, entity.getETag());
        }
        if (entityType.hasStream()) {
            if (entity.getMediaETag() != null) {
                json.writeStringField(Constants.JSON_MEDIA_ETAG, entity.getMediaETag());
            }
            if (entity.getMediaContentType() != null) {
                json.writeStringField(Constants.JSON_MEDIA_CONTENT_TYPE, entity.getMediaContentType());
            }
            if (entity.getMediaContentSource() != null) {
                json.writeStringField(Constants.JSON_MEDIA_READ_LINK, entity.getMediaContentSource().toString());
            }
            if (entity.getMediaEditLinks() != null && !entity.getMediaEditLinks().isEmpty()) {
                json.writeStringField(Constants.JSON_MEDIA_EDIT_LINK, entity.getMediaEditLinks().get(0).getHref());
            }

        }
        if (onlyReference) {
            json.writeStringField(Constants.JSON_ID, entity.getId().toASCIIString());
        } else {
            EdmEntityType resolvedType = resolveEntityType(metadata, entityType, entity.getType());
            if (!resolvedType.equals(entityType)) {
                json.writeStringField(Constants.JSON_TYPE, "#" + entity.getType());
            }
            writeProperties(resolvedType, entity.getProperties(), select, json);
            writeNavigationProperties(metadata, resolvedType, entity, expand, json);
            json.writeEndObject();
        }
    }

    protected EdmEntityType resolveEntityType(final ServiceMetadata metadata, final EdmEntityType baseType,
                                              final String derivedTypeName) throws SerializerException {
        if (derivedTypeName == null ||
                baseType.getFullQualifiedName().getFullQualifiedNameAsString().equals(derivedTypeName)) {
            return baseType;
        }
        EdmEntityType derivedType = metadata.getEdm().getEntityType(new FullQualifiedName(derivedTypeName));
        if (derivedType == null) {
            throw new SerializerException("EntityType not found",
                    SerializerException.MessageKeys.UNKNOWN_TYPE, derivedTypeName);
        }
        EdmEntityType type = derivedType.getBaseType();
        while (type != null) {
            if (type.getFullQualifiedName().getFullQualifiedNameAsString()
                    .equals(baseType.getFullQualifiedName().getFullQualifiedNameAsString())) {
                return derivedType;
            }
            type = type.getBaseType();
        }
        throw new SerializerException("Wrong base type",
                SerializerException.MessageKeys.WRONG_BASE_TYPE, derivedTypeName, baseType
                .getFullQualifiedName().getFullQualifiedNameAsString());
    }

    protected EdmComplexType resolveComplexType(final ServiceMetadata metadata, final EdmComplexType baseType,
                                                final String derivedTypeName) throws SerializerException {
        if (derivedTypeName == null ||
                baseType.getFullQualifiedName().getFullQualifiedNameAsString().equals(derivedTypeName)) {
            return baseType;
        }
        EdmComplexType derivedType = metadata.getEdm().getComplexType(new FullQualifiedName(derivedTypeName));
        if (derivedType == null) {
            throw new SerializerException("Complex Type not found",
                    SerializerException.MessageKeys.UNKNOWN_TYPE, derivedTypeName);
        }
        EdmComplexType type = derivedType.getBaseType();
        while (type != null) {
            if (type.getFullQualifiedName().getFullQualifiedNameAsString()
                    .equals(baseType.getFullQualifiedName().getFullQualifiedNameAsString())) {
                return derivedType;
            }
            type = type.getBaseType();
        }
        throw new SerializerException("Wrong base type",
                SerializerException.MessageKeys.WRONG_BASE_TYPE, derivedTypeName, baseType
                .getFullQualifiedName().getFullQualifiedNameAsString());
    }

    protected void writeProperties(final EdmStructuredType type, final List<Property> properties,
                                   final SelectOption select, final JsonGenerator json) throws IOException, SerializerException {
        final boolean all = ExpandSelectHelper.isAll(select);
        final Set<String> selected = all ? null :
                ExpandSelectHelper.getSelectedPropertyNames(select.getSelectItems());
        for (final String propertyName : type.getPropertyNames()) {
            if (all || selected.contains(propertyName)) {
                final EdmProperty edmProperty = type.getStructuralProperty(propertyName);
                final Property property = findProperty(propertyName, properties);
                final Set<List<String>> selectedPaths = all || edmProperty.isPrimitive() ? null :
                        ExpandSelectHelper.getSelectedPaths(select.getSelectItems(), propertyName);
                writeProperty(edmProperty, property, selectedPaths, json);
            }
        }
    }

    protected void writeNavigationProperties(final ServiceMetadata metadata,
                                             final EdmStructuredType type, final Linked linked, final ExpandOption expand,
                                             final JsonGenerator json) throws SerializerException, IOException {
        if (ExpandSelectHelper.hasExpand(expand)) {
            final boolean expandAll = ExpandSelectHelper.isExpandAll(expand);
            final Set<String> expanded = expandAll ? null :
                    ExpandSelectHelper.getExpandedPropertyNames(expand.getExpandItems());
            for (final String propertyName : type.getNavigationPropertyNames()) {
                if (expandAll || expanded.contains(propertyName)) {
                    final EdmNavigationProperty property = type.getNavigationProperty(propertyName);
                    final Link navigationLink = linked.getNavigationLink(property.getName());
                    final ExpandItem innerOptions = expandAll ? null :
                            ExpandSelectHelper.getExpandItem(expand.getExpandItems(), propertyName);
                    if (innerOptions != null && (innerOptions.isRef() || innerOptions.getLevelsOption() != null)) {
                        throw new SerializerException("Expand options $ref and $levels are not supported.",
                                SerializerException.MessageKeys.NOT_IMPLEMENTED);
                    }
                    writeExpandedNavigationProperty(metadata, property, navigationLink,
                            innerOptions == null ? null : innerOptions.getExpandOption(),
                            innerOptions == null ? null : innerOptions.getSelectOption(),
                            json);
                }
            }
        }
    }

    protected void writeExpandedNavigationProperty(final ServiceMetadata metadata,
                                                   final EdmNavigationProperty property, final Link navigationLink,
                                                   final ExpandOption innerExpand, final SelectOption innerSelect, final JsonGenerator json)
            throws IOException, SerializerException {
        json.writeFieldName(property.getName());
        if (property.isCollection()) {
            if (navigationLink == null || navigationLink.getInlineEntitySet() == null) {
                json.writeStartArray();
                json.writeEndArray();
            } else {
                writeEntitySet(metadata, property.getType(), navigationLink.getInlineEntitySet(), innerExpand,
                        innerSelect, false, json);
            }
        } else {
            if (navigationLink == null || navigationLink.getInlineEntity() == null) {
                json.writeNull();
            } else {
                writeEntity(metadata, property.getType(), navigationLink.getInlineEntity(), innerExpand, innerSelect, false, json);
            }
        }
    }

    protected void writeProperty(final EdmProperty edmProperty, final Property property,
                                 final Set<List<String>> selectedPaths, final JsonGenerator json) throws IOException, SerializerException {
        json.writeFieldName(edmProperty.getName());
        if (property == null || property.isNull()) {
            if (edmProperty.isNullable() == Boolean.FALSE) {
                throw new SerializerException("Non-nullable property not present!",
                        SerializerException.MessageKeys.MISSING_PROPERTY, edmProperty.getName());
            } else {
                json.writeNull();
            }
        } else {
            writePropertyValue(edmProperty, property, selectedPaths, json);
        }
    }

    private void writePropertyValue(final EdmProperty edmProperty,
                                    final Property property, final Set<List<String>> selectedPaths,
                                    final JsonGenerator json) throws IOException, SerializerException {
        try {
            if (edmProperty.isPrimitive()) {
                if (edmProperty.isCollection()) {
                    writePrimitiveCollection((EdmPrimitiveType) edmProperty.getType(), property,
                            edmProperty.isNullable(), edmProperty.getMaxLength(),
                            edmProperty.getPrecision(), edmProperty.getScale(), edmProperty.isUnicode(),
                            json);
                } else {
                    writePrimitive((EdmPrimitiveType) edmProperty.getType(), property,
                            edmProperty.isNullable(), edmProperty.getMaxLength(),
                            edmProperty.getPrecision(), edmProperty.getScale(), edmProperty.isUnicode(),
                            json);
                }
            } else if (edmProperty.isCollection()) {
                writeComplexCollection((EdmComplexType) edmProperty.getType(), property, selectedPaths, json);
            } else if (property.isComplex()) {
                writeComplexValue((EdmComplexType) edmProperty.getType(), property.asComplex().getValue(),
                        selectedPaths, json);
            } else if (property.isEnum()) {
                writePrimitive((EdmPrimitiveType) edmProperty.getType(), property,
                        edmProperty.isNullable(), edmProperty.getMaxLength(),
                        edmProperty.getPrecision(), edmProperty.getScale(), edmProperty.isUnicode(),
                        json);
            } else {
                throw new SerializerException("Property type not yet supported!",
                        SerializerException.MessageKeys.UNSUPPORTED_PROPERTY_TYPE, edmProperty.getName());
            }
        } catch (final EdmPrimitiveTypeException e) {
            throw new SerializerException("Wrong value for property!", e,
                    SerializerException.MessageKeys.WRONG_PROPERTY_VALUE,
                    edmProperty.getName(), property.getValue().toString());
        }
    }

    private void writePrimitiveCollection(final EdmPrimitiveType type, final Property property,
                                          final Boolean isNullable, final Integer maxLength, final Integer precision, final Integer scale,
                                          final Boolean isUnicode,
                                          final JsonGenerator json) throws IOException, EdmPrimitiveTypeException, SerializerException {
        json.writeStartArray();
        for (Object value : property.asCollection()) {
            switch (property.getValueType()) {
                case COLLECTION_PRIMITIVE:
                    writePrimitiveValue(type, value, isNullable, maxLength, precision, scale, isUnicode, json);
                    break;
                case COLLECTION_GEOSPATIAL:
                    throw new SerializerException("Property type not yet supported!",
                            SerializerException.MessageKeys.UNSUPPORTED_PROPERTY_TYPE, property.getName());
                case COLLECTION_ENUM:
                    json.writeString(value.toString());
                    break;
                default:
                    throw new SerializerException("Property type not yet supported!",
                            SerializerException.MessageKeys.UNSUPPORTED_PROPERTY_TYPE, property.getName());
            }
        }
        json.writeEndArray();
    }

    private void writeComplexCollection(final EdmComplexType type, final Property property,
                                        final Set<List<String>> selectedPaths, final JsonGenerator json)
            throws IOException, EdmPrimitiveTypeException, SerializerException {
        json.writeStartArray();
        for (Object value : property.asCollection()) {
            switch (property.getValueType()) {
                case COLLECTION_COMPLEX:
                    writeComplexValue(type, ((ComplexValue) value).getValue(), selectedPaths, json);
                    break;
                default:
                    throw new SerializerException("Property type not yet supported!",
                            SerializerException.MessageKeys.UNSUPPORTED_PROPERTY_TYPE, property.getName());
            }
        }
        json.writeEndArray();
    }

    private void writePrimitive(final EdmPrimitiveType type, final Property property,
                                final Boolean isNullable, final Integer maxLength, final Integer precision, final Integer scale,
                                final Boolean isUnicode, final JsonGenerator json)
            throws EdmPrimitiveTypeException, IOException, SerializerException {
        if (property.isPrimitive()) {
            writePrimitiveValue(type, property.asPrimitive(),
                    isNullable, maxLength, precision, scale, isUnicode, json);
        } else if (property.isGeospatial()) {
            throw new SerializerException("Property type not yet supported!",
                    SerializerException.MessageKeys.UNSUPPORTED_PROPERTY_TYPE, property.getName());
        } else if (property.isEnum()) {
            writePrimitiveValue(type, property.asEnum(),
                    isNullable, maxLength, precision, scale, isUnicode, json);
        } else {
            throw new SerializerException("Inconsistent property type!",
                    SerializerException.MessageKeys.INCONSISTENT_PROPERTY_TYPE, property.getName());
        }
    }

    protected void writePrimitiveValue(final EdmPrimitiveType type, final Object primitiveValue,
                                       final Boolean isNullable, final Integer maxLength, final Integer precision, final Integer scale,
                                       final Boolean isUnicode,
                                       final JsonGenerator json) throws EdmPrimitiveTypeException, IOException {
        final String value = type.valueToString(primitiveValue, isNullable, maxLength, precision, scale, isUnicode);

        if (type == EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Boolean)) {
            json.writeBoolean(Boolean.parseBoolean(value));
        } else if (type == EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Byte)
                || type == EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Decimal)
                || type == EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Double)
                || type == EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Int16)
                || type == EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Int32)
                || type == EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Int64)
                || type == EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.SByte)
                || type == EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Single)) {
            json.writeNumber(value);
        }else if(type instanceof EdmEnumType){
            json.writeString(primitiveValue.toString());
        } else {
            json.writeString(value);
        }
    }

    protected void writeComplexValue(final EdmComplexType type, final List<Property> properties,
                                     final Set<List<String>> selectedPaths, final JsonGenerator json)
            throws IOException, EdmPrimitiveTypeException, SerializerException {
        json.writeStartObject();
        for (final String propertyName : type.getPropertyNames()) {
            final Property property = findProperty(propertyName, properties);
            if (selectedPaths == null || ExpandSelectHelper.isSelected(selectedPaths, propertyName)) {
                writeProperty((EdmProperty) type.getProperty(propertyName), property,
                        selectedPaths == null ? null : ExpandSelectHelper.getReducedSelectedPaths(selectedPaths, propertyName),
                        json);
            }
        }
        json.writeEndObject();
    }

    private Property findProperty(final String propertyName, final List<Property> properties) {
        for (final Property property : properties) {
            if (propertyName.equals(property.getName())) {
                return property;
            }
        }
        return null;
    }


}
