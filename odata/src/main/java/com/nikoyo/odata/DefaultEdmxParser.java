package com.nikoyo.odata;

import org.apache.commons.io.IOUtils;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.*;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DefaultEdmxParser {


    public static List<CsdlSchema> parse(InputStream metadata) throws ParserConfigurationException, IOException, SAXException {
        try {
            return parse(IOUtils.toString(metadata));
        } finally {
            IOUtils.closeQuietly(metadata);
        }
    }


    public static List<CsdlSchema> parse(String metadata) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(IOUtils.toInputStream(metadata));
        NodeList schemaNodeList = document.getElementsByTagName("Schema");

        List<CsdlSchema> schemaList = new ArrayList<CsdlSchema>();

        for (int i = 0; i < schemaNodeList.getLength(); i++) {
            Node schemaNode = schemaNodeList.item(i);
            XmlAttributes attributes = new XmlAttributes(schemaNode.getAttributes());

            CsdlSchema schema = parseSchema(schemaNode);
            schema.setNamespace(attributes.getString("Namespace"));
            schema.setAlias(attributes.getString("Alias"));

            schemaList.add(schema);
        }

        return schemaList;

    }

    private static CsdlSchema parseSchema(Node schemaNode) {
        CsdlSchema schema = new CsdlSchema();
        for (int i = 0; i < schemaNode.getChildNodes().getLength(); i++) {
            Node child = schemaNode.getChildNodes().item(i);
            if ("ComplexType".equals(child.getNodeName())) {
                schema.getComplexTypes().add(parseComplexType(child));
            }
            if ("EnumType".equals(child.getNodeName())) {
                schema.getEnumTypes().add(parseEnumType(child));
            }
            if ("EntityType".equals(child.getNodeName())) {
                schema.getEntityTypes().add(parseEntityType(child));
            }
            if ("Action".equals(child.getNodeName())) {
                schema.getActions().add(parseAction(child));
            }
            if ("Function".equals(child.getNodeName())) {
                schema.getFunctions().add(parseFunction(child));
            }
            if ("EntityContainer".equals(child.getNodeName())) {
                schema.setEntityContainer(parseEntityContainer(child));
            }
            if ("Term".equals(child.getNodeName())) {
                schema.getTerms().add(parseTerm(child));
            }
        }
        return schema;
    }

    private static CsdlComplexType parseComplexType(Node node) {
        CsdlComplexType complexType = new CsdlComplexType();
        XmlAttributes attributes = new XmlAttributes(node.getAttributes());
        complexType.setName(attributes.getString("Name"));
        if (attributes.getString("BaseType") != null)
            complexType.setBaseType(attributes.getString("BaseType"));
        complexType.setOpenType(attributes.getBoolean("OpenType"));
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node child = node.getChildNodes().item(i);
            if (child.getNodeName().equals("Property")) complexType.getProperties().add(parseProperty(child));
        }

        return complexType;
    }

    private static CsdlTerm parseTerm(Node node) {
        CsdlTerm term = new CsdlTerm();
        XmlAttributes attributes = new XmlAttributes(node.getAttributes());
        term.setName(attributes.getString("Name"));
        setType(attributes.getString("Type"), term);
        return term;
    }

    private static CsdlEnumType parseEnumType(Node node) {
        CsdlEnumType enumType = new CsdlEnumType();
        XmlAttributes attributes = new XmlAttributes(node.getAttributes());
        enumType.setFlags(attributes.getBoolean("IsFlags"));
        enumType.setName(attributes.getString("Name"));

        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node child = node.getChildNodes().item(i);
            if (child.getNodeName().equals("Member"))
                enumType.getMembers().add(parseEnumMember(child, (int) Math.pow(2, i)));
        }
        if (attributes.getString("UnderlyingType") == null)
            enumType.setUnderlyingType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
        else
            enumType.setUnderlyingType(attributes.getString("UnderlyingType"));

        return enumType;
    }

    private static CsdlEnumMember parseEnumMember(Node node, int defaultValue) {
        CsdlEnumMember enumMember = new CsdlEnumMember();
        final XmlAttributes attributes = new XmlAttributes(node.getAttributes());
        enumMember.setName(attributes.getString("Name"));
        if (attributes.getString("Value") != null)
            enumMember.setValue(attributes.getString("Value"));
        else
            enumMember.setValue(String.valueOf(defaultValue));
        return enumMember;
    }

    private static CsdlEntityType parseEntityType(Node node) {
        CsdlEntityType entityType = new CsdlEntityType();
        XmlAttributes attributes = new XmlAttributes(node.getAttributes());
        entityType.setName(attributes.getString("Name"));
        if (attributes.getString("BaseType") != null)
            entityType.setBaseType(attributes.getString("BaseType"));
        entityType.setHasStream(attributes.getBoolean("HasStream"));
        entityType.setOpenType(attributes.getBoolean("OpenType"));
        entityType.setAbstract(attributes.getBoolean("Abstract"));
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node child = node.getChildNodes().item(i);
            if (child.getNodeName().equals("Property")) entityType.getProperties().add(parseProperty(child));
            else if (child.getNodeName().equals("Key")) entityType.setKey(parseEntityKeys(child));
            else if (child.getNodeName().equals("NavigationProperty"))
                entityType.getNavigationProperties().add(parseNavigationProperty(child));
        }
        return entityType;
    }

    private static List<CsdlPropertyRef> parseEntityKeys(Node node) {
        List<CsdlPropertyRef> keys = new ArrayList<CsdlPropertyRef>();
        for (int j = 0; j < node.getChildNodes().getLength(); j++) {
            Node keyNode = node.getChildNodes().item(j);
            if (keyNode.getNodeName().equals("PropertyRef")) keys.add(parsePropertyRef(keyNode));
        }
        return keys;
    }

    private static CsdlPropertyRef parsePropertyRef(Node node) {
        CsdlPropertyRef ref = new CsdlPropertyRef();
        XmlAttributes attributes = new XmlAttributes(node.getAttributes());
        ref.setName(attributes.getString("Name"));
        ref.setAlias(attributes.getString("Alias"));
        return ref;
    }

    private static CsdlProperty parseProperty(Node node) {
        XmlAttributes attributes = new XmlAttributes(node.getAttributes());
        CsdlProperty p = new CsdlProperty();
        p.setName(attributes.getString("Name"));
        p.setDefaultValue(attributes.getString("DefaultValue"));
        setType(attributes.getString("Type"), p);
        p.setNullable(attributes.getBoolean("Nullable", true));
        return p;
    }

    private static CsdlNavigationProperty parseNavigationProperty(Node node) {
        XmlAttributes attributes = new XmlAttributes(node.getAttributes());
        CsdlNavigationProperty p = new CsdlNavigationProperty();
        p.setName(attributes.getString("Name"));
        setType(attributes.getString("Type"), p);
        p.setPartner(attributes.getString("Partner"));
        p.setContainsTarget(attributes.getBoolean("ContainsTarget"));
        p.setNullable(attributes.getBoolean("Nullable", true));
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node child = node.getChildNodes().item(i);
            if (child.getNodeName().equals("ReferentialConstraint"))
                p.getReferentialConstraints().add(parseReferentialConstraint(child));
        }
        return p;
    }


    private static CsdlReferentialConstraint parseReferentialConstraint(Node node) {
        CsdlReferentialConstraint referentialConstraint = new CsdlReferentialConstraint();
        XmlAttributes attributes = new XmlAttributes(node.getAttributes());
        referentialConstraint.setProperty(attributes.getString("Property"));
        referentialConstraint.setReferencedProperty(attributes.getString("ReferencedProperty"));
        return referentialConstraint;
    }

    private static CsdlAction parseAction(Node node) {
        CsdlAction action = new CsdlAction();
        XmlAttributes attributes = new XmlAttributes(node.getAttributes());
        action.setName(attributes.getString("Name"));
        action.setBound(attributes.getBoolean("IsBound"));
        action.setEntitySetPath(attributes.getString("EntitySetPath"));
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node child = node.getChildNodes().item(i);
            if (child.getNodeName().equals("Parameter")) action.getParameters().add(parseParameter(child));
            else if (child.getNodeName().equals("ReturnType")) action.setReturnType(parseReturnType(child));
        }
        return action;
    }

    private static CsdlFunction parseFunction(Node node) {
        CsdlFunction function = new CsdlFunction();
        XmlAttributes attributes = new XmlAttributes(node.getAttributes());
        function.setName(attributes.getString("Name"));
        function.setBound(attributes.getBoolean("IsBound"));
        function.setEntitySetPath(attributes.getString("EntitySetPath"));
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node child = node.getChildNodes().item(i);
            if (child.getNodeName().equals("Parameter")) function.getParameters().add(parseParameter(child));
            else if (child.getNodeName().equals("ReturnType")) function.setReturnType(parseReturnType(child));
        }
        return function;
    }

    private static CsdlParameter parseParameter(Node node) {
        CsdlParameter parameter = new CsdlParameter();
        XmlAttributes attributes = new XmlAttributes(node.getAttributes());
        parameter.setName(attributes.getString("Name"));
        setType(attributes.getString("Type"), parameter);
        parameter.setNullable(attributes.getBoolean("Nullable", true));
        return parameter;
    }

    private static CsdlReturnType parseReturnType(Node node) {
        CsdlReturnType returnType = new CsdlReturnType();
        XmlAttributes attributes = new XmlAttributes(node.getAttributes());
        setType(attributes.getString("Type"), returnType);
        returnType.setNullable(attributes.getBoolean("Nullable", true));
        return returnType;
    }


    private static CsdlEntityContainer parseEntityContainer(Node node) {
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        XmlAttributes attributes = new XmlAttributes(node.getAttributes());
        entityContainer.setName(attributes.getString("Name"));
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node child = node.getChildNodes().item(i);
            if (child.getNodeName().equals("Singleton")) entityContainer.getSingletons().add(parseSingleton(child));
            else if (child.getNodeName().equals("EntitySet"))
                entityContainer.getEntitySets().add(parseEntitySet(child));
            else if (child.getNodeName().equals("ActionImport"))
                entityContainer.getActionImports().add(parseActionImport(child));
            else if (child.getNodeName().equals("FunctionImport"))
                entityContainer.getFunctionImports().add(parseFunctionImport(child));
        }

        return entityContainer;
    }

    private static CsdlEntitySet parseEntitySet(Node node) {
        CsdlEntitySet entitySet = new CsdlEntitySet();
        XmlAttributes attributes = new XmlAttributes(node.getAttributes());
        entitySet.setName(attributes.getString("Name"));
        entitySet.setType(attributes.getString("EntityType"));
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node child = node.getChildNodes().item(i);
            if (child.getNodeName().equals("NavigationPropertyBinding"))
                entitySet.getNavigationPropertyBindings().add(parseNavigationPropertyBinding(child));
            else if (child.getNodeName().equals("Annotation")) entitySet.getAnnotations().add(parseAnnotation(child));
        }
        return entitySet;
    }

    private static CsdlNavigationPropertyBinding parseNavigationPropertyBinding(Node node) {
        CsdlNavigationPropertyBinding navigationPropertyBinding = new CsdlNavigationPropertyBinding();
        XmlAttributes attributes = new XmlAttributes(node.getAttributes());
        navigationPropertyBinding.setPath(attributes.getString("Path"));
        navigationPropertyBinding.setTarget(attributes.getString("Target"));
        return navigationPropertyBinding;
    }

    private static CsdlAnnotation parseAnnotation(Node node) {
        CsdlAnnotation annotation = new CsdlAnnotation();
        XmlAttributes attributes = new XmlAttributes(node.getAttributes());
        annotation.setTerm(attributes.getString("Term"));
        //todo 没实现完全
//        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
//            Node child = node.getChildNodes().item(i);
//
//            if (child.getNodeName().equals("Record"))            annotation.getNavigationPropertyBindings().add(parseNavigationPropertyBinding(child));
//        }
        return annotation;
    }

    private static CsdlSingleton parseSingleton(Node node) {
        CsdlSingleton singleton = new CsdlSingleton();
        XmlAttributes attributes = new XmlAttributes(node.getAttributes());
        singleton.setName(attributes.getString("Name"));
        singleton.setType(attributes.getString("Type"));
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node child = node.getChildNodes().item(i);
            if (child.getNodeName().equals("NavigationPropertyBinding"))
                singleton.getNavigationPropertyBindings().add(parseNavigationPropertyBinding(child));
        }

        return singleton;

    }

    private static CsdlActionImport parseActionImport(Node node) {
        CsdlActionImport actionImport = new CsdlActionImport();
        XmlAttributes attributes = new XmlAttributes(node.getAttributes());
        actionImport.setName(attributes.getString("Name"));
        actionImport.setAction(attributes.getString("Action"));
        return actionImport;
    }

    private static CsdlFunctionImport parseFunctionImport(Node node) {
        CsdlFunctionImport functionImport = new CsdlFunctionImport();
        XmlAttributes attributes = new XmlAttributes(node.getAttributes());
        functionImport.setName(attributes.getString("Name"));
        functionImport.setFunction(attributes.getString("Function"));
        functionImport.setIncludeInServiceDocument(attributes.getBoolean("IncludeInServiceDocument"));
        return functionImport;
    }


    private static void setType(String type, Object obj) {
        try {
            Method setCollection = obj.getClass().getMethod("setCollection", boolean.class);
            boolean isCollection = (type.startsWith("Collection(") && type.endsWith(")"));
            setCollection.invoke(obj, isCollection);
            Method setType = obj.getClass().getMethod("setType", String.class);
            if (isCollection) {
                setType.invoke(obj, type.substring(11, type.length() - 1));
            } else {
                setType.invoke(obj, type);
            }

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    static class XmlAttributes {
        private final HashMap<String, String> innerMap = new HashMap<String, String>();

        public XmlAttributes(NamedNodeMap attributes) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node n = attributes.item(i);
                innerMap.put(n.getNodeName(), n.getNodeValue());
            }
        }

        public String getString(String key) {
            return innerMap.get(key);
        }

        public boolean getBoolean(String key) {
            return getBoolean(key, false);
        }

        public boolean getBoolean(String key, boolean defaultValue) {
            if (innerMap.containsKey(key)) return Boolean.valueOf(innerMap.get(key));
            return defaultValue;
        }

    }

}
