package com.nikoyo.odata.persistence;

import com.nikoyo.odata.RequestContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.olingo.commons.api.ODataException;
import org.apache.olingo.commons.api.edm.*;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.commons.core.edm.EdmNavigationPropertyImpl;
import org.apache.olingo.commons.core.edm.EdmPropertyImpl;
import org.apache.olingo.server.api.ODataApplicationException;
import org.elasticsearch.common.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Administrator on 2015/7/31.
 */
@Service
public class ValidateUtils {

    @Autowired
    RequestContext reqctx;


    public void checkEntityType(PersistenceDataService.UriInfoContext ctx, Map<String, Object> source) throws ODataException {
        EdmEntityType type = ctx.getType();
        List<String> propertyNames = type.getPropertyNames();
        List<String> list = checkFieldName(propertyNames, new ArrayList<String>(source.keySet()));
        if (!list.isEmpty()) {
            throw new ODataApplicationException(String.format("Field: %s has not defined in %s", list.toArray(), type.getName()), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
        }
        for(String name : propertyNames){
            EdmElement property = type.getProperty(name);
            if(property instanceof EdmProperty){
                Object value = source.get(property.getName());
                EdmProperty prop = (EdmProperty) property;
                EdmTypeKind kind = prop.getType().getKind();
                if(kind == EdmTypeKind.PRIMITIVE){
                    checkPrimitiveType(prop, value);
                }else if(kind == EdmTypeKind.COMPLEX){
                    checkEdmComplexType(prop, value);
                }
            }else if(property instanceof EdmNavigationProperty){

            }else if(property instanceof EdmParameter){

            }

        }

    }

    private List<String> checkFieldName(List<String> list1, List<String> list2){
        list2.removeAll(list1);
        return list2;
    }

    private void checkEdmComplexType(EdmProperty property, Object value) throws ODataApplicationException {
        EdmComplexType complexType = reqctx.getServiceMetadata().getEdm().getComplexType(property.getType().getFullQualifiedName());
        List<String> propertyNames = complexType.getPropertyNames();
        if(value != null){
            if(property.isCollection()){
                List<Map<String, Object>> list = (List<Map<String, Object>>) value;
                for(String name : propertyNames){
                    EdmProperty prop = (EdmProperty) complexType.getProperty(name);
                    EdmTypeKind kind = prop.getType().getKind();
                    if (kind == EdmTypeKind.PRIMITIVE) {
                        for(Map<String, Object> map : list){
                            List<String> l = checkFieldName(propertyNames, new ArrayList<String>(map.keySet()));
                            if (!l.isEmpty()) {
                                throw new ODataApplicationException(String.format("Field: %s has not defined yet in %s", l.toString(), complexType.getName()), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
                            }
                            this.checkPrimitiveType(prop, map.get(prop.getName()));
                        }
                    }else if(kind == EdmTypeKind.COMPLEX){
                        for(Map<String, Object> map : list){
                            this.checkEdmComplexType(prop, map.get(prop.getName()));
                        }
                    }
                }

            }else{
                Map<String, Object> map = (Map<String, Object>) value;
                List<String> list = checkFieldName(propertyNames, new ArrayList<String>(map.keySet()));
                if (!list.isEmpty()) {
                    throw new ODataApplicationException(String.format("Field: %s has not defined yet in %s", list.toArray(), complexType.getName()), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
                }
                for(String name : propertyNames){
                    EdmProperty prop = (EdmProperty) complexType.getProperty(name);
                    EdmTypeKind kind = prop.getType().getKind();
                    if (kind == EdmTypeKind.PRIMITIVE) {
                        this.checkPrimitiveType(prop, map.get(prop.getName()));
                    }else if(kind == EdmTypeKind.COMPLEX){
                        this.checkEdmComplexType(prop, map.get(prop.getName()));
                    }
                }

            }

        }else{
            if (!property.isNullable() && property.getDefaultValue() == null) {
                throw new ODataApplicationException(String.format("%s value can not be null", property.getName()), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
            }
        }
    }

    private void checkPrimitiveType(EdmProperty property, Object value) throws ODataApplicationException {
        if(value != null){
            if (property.isCollection()){
                List<Object> list = (List<Object>)value;
                for(Object o : list){
                    isTypeCorrect(property.getType().getName(), o);
                    isFacetsCorrect(property, value);
                }
            }else{
                isTypeCorrect(property.getType().getName(), value);
                isFacetsCorrect(property, value);
            }
        }else{
            if (!property.isNullable() && property.getDefaultValue() == null) {
                throw new ODataApplicationException(String.format("%s value can not be null", property.getName()), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
            }
        }
    }

    private static void isFacetsCorrect(EdmProperty property, Object value) throws ODataApplicationException {
        if (value == null) {
            return;
        }
        String typeName = property.getType().getName();
        if(property.getMaxLength() != null){
            if(typeName.equals("String")){
                if(value.toString().length() > property.getMaxLength()){
                    throw new ODataApplicationException(String.format("%s value length is more than %s", property.getName(), property.getMaxLength()), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
                }
            }
        }

        if(property.getPrecision() != null){
            if (typeName.equals("DateTimeOffset")) {
                DateTime dataTime = DateTime.parse(value.toString());
                int length = String.valueOf(dataTime.getMillisOfSecond()).toString().length();
                if(length > property.getPrecision()){
                    throw new ODataApplicationException(String.format("%s value precision is more than %s", property.getName(), property.getPrecision()), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
                }
            }
            if(typeName.equals("Decimal")){
                String valueString = value.toString();
                int i = valueString.lastIndexOf(".");
                if (valueString.substring(i + 1).length() > property.getPrecision()) {
                    throw new ODataApplicationException(String.format("%s value precision is more than %s", property.getName(), property.getPrecision()), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
                }
            }
        }
    }


    private static void isTypeCorrect(String type, Object value){
        if(value == null){
            return;
        }
        if(type.equals("Int16")){
            Short.parseShort(value.toString());
        }else if(type.equals("Int32")){
            Integer.parseInt(value.toString());
        }else if(type.equals("Int64")){
            Long.parseLong(value.toString());
        }else if(type.equals("Single")){
            Float.parseFloat(value.toString());
        }else if(type.equals("Double")){
            Double.parseDouble(value.toString());
        }else if(type.equals("Decimal")){
            BigDecimal.valueOf(Long.valueOf(value.toString()));
        }else if(type.equals("Boolean")){
            Boolean.parseBoolean(value.toString());
        }else if(type.equals("Date")){
            Date.parse(value.toString());
        }else if(type.equals("DateTimeOffset")){
            DateTime.parse(value.toString());
        }
    }

}
