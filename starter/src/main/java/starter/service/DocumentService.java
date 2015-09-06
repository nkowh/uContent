package starter.service;


import org.apache.commons.io.IOUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.LocalDateTime;
import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import starter.RequestContext;
import starter.rest.Json;
import starter.service.fs.FileSystem;
import starter.uContentException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DocumentService {

    @Autowired
    private RequestContext context;

    @Autowired
    private FileSystem fs;

    @Autowired
    private TypeService typeService;

    @Autowired
    private UserService userService;

    Logger logger = LoggerFactory.getLogger(DocumentService.class);

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


    public XContentBuilder query(String[] types, String query, int start, int limit, SortBuilder[] sort, String highlight, boolean allowableActions) throws IOException {
        SearchRequestBuilder searchRequestBuilder = context.getClient().prepareSearch(context.getIndex()).setFrom(start).setSize(limit);
        //set types
        if (types != null && types.length > 0) {
            searchRequestBuilder.setTypes(types);
        }else{
            List<String> allTypes = typeService.getAllTypes();
            searchRequestBuilder.setTypes(allTypes.toArray(new String[]{}));
        }
        //set query
        if (StringUtils.isNotBlank(query)) {
            searchRequestBuilder.setQuery(query);
        }
        //set sort
        if (sort != null && sort.length > 0) {
            for(SortBuilder sortBuilder : sort){
                searchRequestBuilder.addSort(sortBuilder);
            }
        }
        //set highlight
        if (StringUtils.isNotBlank(highlight)) {
            searchRequestBuilder.addHighlightedField(highlight);
        }
        //_fullText field not return
        String[] exclude = {"_streams._fullText"};
        searchRequestBuilder.setFetchSource(null, exclude);

        //set acl filter
        TermFilterBuilder termFilter1 = FilterBuilders.termFilter(Constant.FieldName.USER, context.getUserName());
        TermFilterBuilder termFilter2 = FilterBuilders.termFilter(Constant.FieldName.PERMISSION, Constant.Permission.read);
        BoolFilterBuilder boolFilter1 = FilterBuilders.boolFilter().must(termFilter1, termFilter2);
        BoolFilterBuilder boolFilterBuilder = FilterBuilders.boolFilter().should(boolFilter1);
        List<String> groups = userService.getGroupsOfUser(context.getUserName());
        for(String group : groups){
            TermFilterBuilder termFilter3 = FilterBuilders.termFilter(Constant.FieldName.GROUP, group);
            TermFilterBuilder termFilter4 = FilterBuilders.termFilter(Constant.FieldName.PERMISSION, Constant.Permission.read);
            BoolFilterBuilder boolFilter2 = FilterBuilders.boolFilter().must(termFilter3, termFilter4);
            boolFilterBuilder.should(boolFilter2);
        }
        FilterBuilder filter = FilterBuilders.nestedFilter(Constant.FieldName.ACL, boolFilterBuilder);
        searchRequestBuilder.setPostFilter(filter);
        //process result
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder().startObject();
        xContentBuilder.field("total", searchResponse.getHits().getTotalHits());
        xContentBuilder.startArray("documents");
        for(SearchHit hit : searchResponse.getHits().getHits()){
            xContentBuilder.startObject();
            xContentBuilder.field("_index", hit.getIndex())
                    .field("_type", hit.getType())
                    .field("_id", hit.getId())
                    .field("_score", hit.getScore())
                    .field("_version", hit.getVersion())
                    .field("_highlight", hit.getHighlightFields());
            Map<String, Object> source = hit.getSource();
            Iterator<Map.Entry<String, Object>> iterator = source.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, Object> entry = iterator.next();
                xContentBuilder.field(entry.getKey(), entry.getValue());
            }
            if (allowableActions) {
                xContentBuilder.field(Constant.FieldName.ALLOWABLEACTIONS, getUserPermission(context.getUserName(), source.get("_acl")));
            }
            xContentBuilder.endObject();
        }
        xContentBuilder.endArray().endObject();
        return xContentBuilder;
    }

    public XContentBuilder create(String type, Json body) throws IOException, ParseException {
        validate(body, type);
        beforeCreate(body);
        IndexResponse indexResponse = context.getClient().prepareIndex(context.getIndex(), type).setSource(body).execute().actionGet();
        XContentBuilder builder = JsonXContent.contentBuilder();
        builder.startObject()
                .field("_index", indexResponse.getIndex())
                .field("_type", indexResponse.getType())
                .field("_id", indexResponse.getId())
                .field("_version", indexResponse.getVersion())
                .field("_created", indexResponse.isCreated())
                .endObject();
        return builder;
    }

    public XContentBuilder create(String type, Json body, List<MultipartFile> files) throws IOException, ParseException {
        if (!files.isEmpty()) {
            List<Map<String, Object>> streams = new ArrayList<Map<String, Object>>();
            for(MultipartFile file : files){
                Map<String, Object> stream = new HashMap<String, Object>();
                String fileId = fs.write(file.getBytes());
                if (StringUtils.isBlank(fileId)) {
                    logger.error(String.format("The stream: %s store failed", file.getName()));
                    throw new uContentException("FS store failed", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                stream.put(Constant.FieldName.STREAMID, fileId);
                stream.put(Constant.FieldName.STREAMNAME, file.getName());
                stream.put(Constant.FieldName.LENGTH, file.getSize());
                stream.put(Constant.FieldName.CONTENTTYPE, file.getContentType());
                stream.put(Constant.FieldName.FULLTEXT, parse(file.getInputStream()));
                streams.add(stream);
            }
            body.put(Constant.FieldName.STREAMS, streams);
        }
        return create(type, body);
    }

    public Json head(String type, String id) throws IOException {
        return get(type, id, true, false);
    }

    public Json get(String type, String id, boolean head, boolean allowableActions) throws IOException {
        GetResponse getResponse = checkPermission(type, id, context.getUserName(), Constant.Permission.read);
        return processGet(getResponse, head, allowableActions);
    }

    public XContentBuilder update(String type, String id, Json body) throws IOException, ParseException {
        GetResponse getResponse = checkPermission(type, id, context.getUserName(), Constant.Permission.write);
        processAcl(body, getResponse.getSource().get(Constant.FieldName.ACL));
        validate(body, type);
        beforeUpdate(body);
        UpdateResponse updateResponse = context.getClient().prepareUpdate(context.getIndex(), type, id).setDoc(body).execute().actionGet();
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
        xContentBuilder.startObject()
                .field("_index", context.getIndex())
                .field("_type", type)
                .field("_id", id)
                .field("_version", updateResponse.getVersion());
        xContentBuilder.endObject();
        return xContentBuilder;
    }

    public XContentBuilder patch(String type, String id, Json body) throws IOException, ParseException {
        return update(type, id, body);
    }

    public XContentBuilder delete(String type, String id) throws IOException {
        checkPermission(type, id, context.getUserName(), Constant.Permission.write);
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
        xContentBuilder.startObject()
                .field("_index", context.getIndex())
                .field("_type", type)
                .field("_id", id);
        DeleteResponse deleteResponse = context.getClient().prepareDelete(context.getIndex(), type, id).execute().actionGet();
        xContentBuilder.field("_version", deleteResponse.getVersion());
        xContentBuilder.field("_found", deleteResponse.isFound());
        xContentBuilder.endObject();
        return xContentBuilder;
    }


    private void beforeCreate(Json body){
        LocalDateTime localDateTime = new DateTime().toLocalDateTime();
        body.put(Constant.FieldName.CREATEDBY, context.getUserName());
        body.put(Constant.FieldName.CREATEDON, localDateTime);
        body.put(Constant.FieldName.LASTUPDATEDBY, context.getUserName());
        body.put(Constant.FieldName.LASTUPDATEDON, localDateTime);
        List<Object> permission = new ArrayList<Object>();
        permission.add(Constant.Permission.read);
        permission.add(Constant.Permission.write);
        Map<String, Object> ace = new HashMap<String, Object>();
        ace.put(Constant.FieldName.USER, context.getUserName());
        ace.put(Constant.FieldName.PERMISSION, permission);
        List<Map<String, Object>> acl = new ArrayList<Map<String, Object>>();
        acl.add(ace);
        body.put(Constant.FieldName.ACL, acl);
    }

    private Json processGet(GetResponse getResponse, boolean head, boolean allowableActions) throws IOException {
        Json json = new Json();
        json.put("_index", getResponse.getIndex());
        json.put("_type", getResponse.getType());
        json.put("_id", getResponse.getId());
        json.put("_found", getResponse.isExists());
        if (getResponse.isExists()) {
            json.put("_version", getResponse.getVersion());
            if (!head){
                Map<String, Object> source = getResponse.getSource();
                if (source != null) {
                    Iterator<Map.Entry<String, Object>> iterator = source.entrySet().iterator();
                    while (iterator.hasNext()){
                        Map.Entry<String, Object> entry = iterator.next();
                        json.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            if (allowableActions) {
                json.put(Constant.FieldName.ALLOWABLEACTIONS, getUserPermission(context.getUserName(), getResponse.getSource().get(Constant.FieldName.ACL)));
            }
        }
        return json;
    }

    private Set getUserPermission(String user, Object acl) throws IOException {
        Set uPermission = getPermissionByUser(user, acl);
        List<String> groups = getGroups(user);
        Set gPermission = getPermissionByGroups(groups, acl);
        uPermission.addAll(gPermission);
        return uPermission;
    }

    private List<String> getGroups(String user) throws IOException {
        return userService.getGroupsOfUser(user);
    }

    private Set getPermissionByUser(String user, Object acl){
        List<Map<String, Object>> _acl = (List<Map<String, Object>>)acl;
        Set permission = new HashSet();
        if (_acl != null && !_acl.isEmpty()) {
            for(Map<String, Object> map : _acl){
                Object u = map.get(Constant.FieldName.USER);
                if(u != null && u.toString().equals(user)){
                    permission.addAll((List)map.get(Constant.FieldName.PERMISSION));
                }
            }
        }
        return permission;
    }

    private Set getPermissionByGroups(List<String> groups, Object acl){
        List<Map<String, Object>> _acl = (List<Map<String, Object>>)acl;
        Set permission = new HashSet();
        if (_acl != null && !_acl.isEmpty()) {
            for(Map<String, Object> map : _acl){
                Object u = map.get(Constant.FieldName.GROUP);
                if(u != null && groups.contains(u.toString())){
                    permission.addAll((List)map.get(Constant.FieldName.PERMISSION));
                }
            }
        }
        return permission;
    }

    private boolean hasPermission(String user, Object acl, Constant.Permission action) throws IOException {
        List<Map<String, Object>> _acl = (List<Map<String, Object>>)acl;
        Set permission = getPermissionByUser(user, _acl);
        if (permission.contains(action.toString())) {
            return true;
        }else{
            List<String> groups = getGroups(user);
            permission = getPermissionByGroups(groups, _acl);
            return permission.contains(action.toString());
        }
    }

    private void beforeUpdate(Json body){
        if(body != null){
            LocalDateTime localDateTime = new DateTime().toLocalDateTime();
            body.put(Constant.FieldName.LASTUPDATEDBY, context.getUserName());
            body.put(Constant.FieldName.LASTUPDATEDON, localDateTime);
        }
    }

    public void processAcl(Json body, Object srcAcl){
        Object newAcl = body.get(Constant.FieldName.ACL);
        if (newAcl != null) {
            List<Map<String, Object>> _srcAcl = (List<Map<String, Object>>) srcAcl;
            Object addAcl = ((Map<String, Object>) newAcl).get("add");
            Object removeAcl = ((Map<String, Object>) newAcl).get("remove");
            if (addAcl != null) {
                List<Map<String, Object>> _addAcl = (List<Map<String, Object>>) addAcl;
                handleAddAcl(_addAcl, _srcAcl);
            }
            if (removeAcl != null) {
                List<Map<String, Object>> _removeAcl = (List<Map<String, Object>>) removeAcl;
                handleRemoveAcl(_removeAcl, _srcAcl);
            }
            body.put(Constant.FieldName.ACL, _srcAcl);
        }
    }

    private void handleRemoveAcl(List<Map<String, Object>> removeAcl, List<Map<String, Object>> _srcAcl) {
        for(Map<String, Object> map : removeAcl){
            Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
            List<String> newPermission = new ArrayList<String>();
            String who = null;
            while (it.hasNext()){
                Map.Entry<String, Object> entry = it.next();
                String key = entry.getKey();
                if (key.equals(Constant.FieldName.PERMISSION)) {
                    newPermission = (List<String>) map.get(key);
                }else{
                    who = key;
                }
            }
            Iterator<Map<String, Object>> iterator = _srcAcl.iterator();
            while (iterator.hasNext()){
                Map<String, Object> src_ace = iterator.next();
                Object o = src_ace.get(who);
                if (o != null && o.toString().equals(map.get(who).toString())) {
                    List<String> oldPermission = (List<String>) src_ace.get(Constant.FieldName.PERMISSION);
                    oldPermission.removeAll(newPermission);
                }
            }
        }
    }

    private void handleAddAcl(List<Map<String, Object>> addAcl, List<Map<String, Object>> _srcAcl) {
        for(Map<String, Object> map : addAcl){
            Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
            List<String> newPermission = new ArrayList<String>();
            String who = null;
            while (it.hasNext()){
                Map.Entry<String, Object> entry = it.next();
                String key = entry.getKey();
                if (key.equals(Constant.FieldName.PERMISSION)) {
                    newPermission = (List<String>) map.get(key);
                }else{
                    who = key;
                }
            }
            boolean found = false;
            Iterator<Map<String, Object>> iterator = _srcAcl.iterator();
            while (iterator.hasNext()){
                Map<String, Object> src_ace = iterator.next();
                Object o = src_ace.get(who);
                if (o != null && o.toString().equals(map.get(who).toString())) {
                    List<String> oldPermission = (List<String>) src_ace.get(Constant.FieldName.PERMISSION);
                    for(String s : newPermission){
                        if (!oldPermission.contains(s)) {
                            oldPermission.add(s);
                        }
                    }
                    found = true;
                }
            }
            if (!found) {
                _srcAcl.add(map);
            }
        }
    }


    public GetResponse checkPermission(String type, String id, String user, Constant.Permission permission) throws IOException {
        String[] exclude = {"_streams._fullText"};
        GetResponse getResponse = context.getClient().prepareGet(context.getIndex(), type, id).setFetchSource(null,exclude).execute().actionGet();
        if (!getResponse.isExists()) {
            logger.warn(String.format("The doc: %s in type %s of index %s is not exist", id, type, context.getIndex()));
            throw new uContentException("Not found", HttpStatus.NOT_FOUND);
        }
        if (!hasPermission(user, getResponse.getSource().get(Constant.FieldName.ACL), permission)) {
            logger.warn(String.format("The user: %s do not have the permission: %s on doc %s", user, permission, id));
            throw new uContentException("Forbidden", HttpStatus.FORBIDDEN);
        }
        return getResponse;
    }


    private void validate(Json body, String type) throws IOException, ParseException {
        Map<String, Map<String, Object>> definition = typeService.getProperties(type);
        Set<String> keySet = definition.keySet();
        Iterator<Map.Entry<String, Object>> iterator = body.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, Object> entry = iterator.next();
            String key = entry.getKey();
            if (key.equals(Constant.FieldName.STREAMS) || key.equals(Constant.FieldName.ACL)) {
                continue;
            }
            if (!keySet.contains(key)) {//ignore undefined property
                logger.warn(String.format("The property: %s has not defined, Ignore!", key));
                iterator.remove();
                continue;
            }
            Map<String, Object> property = definition.get(key);
            String propType = property.get(Constant.FieldName.TYPE).toString();
            entry.setValue(formatValue(propType, entry.getValue()));
        }
        Iterator<Map<String, Object>> it = definition.values().iterator();
        while (it.hasNext()){
            Map<String, Object> entry = it.next();
            if ((Boolean)entry.get(Constant.FieldName.REQUIRED) == true) {
                String propName = entry.get(Constant.FieldName.NAME).toString();
                Object v = body.get(propName);
                if(v == null){
                    Object defaultValue = entry.get(Constant.FieldName.DEFAULTVALUE);
                    if (defaultValue == null || defaultValue.toString().equals("")) {
                        logger.error(String.format("Property : %s is required", propName));
                        throw new uContentException(String.format("Property : %s is required", propName), HttpStatus.BAD_REQUEST);
                    }
                    body.put(propName, formatValue(entry.get(Constant.FieldName.TYPE).toString(), defaultValue));
                }
            }
        }
    }


    private Object formatValue(String type, Object value) throws ParseException {
        if (value == null || value.toString().equals("")) {
            return null;
        }
        String StringValue = value.toString();
        switch (type){
            case Constant.Type.INTEGER :
                return Integer.valueOf(StringValue);
            case Constant.Type.FLOAT :
                return Float.valueOf(StringValue);
            case Constant.Type.DATE :
                return sdf.parse(StringValue);
            case Constant.Type.BOOLEAN :
                return Boolean.valueOf(StringValue);
            default:
                return StringValue;
        }
    }


    public String parse(InputStream in){
        try {
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            parser.parse(in, handler, metadata);
            return handler.toString();
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (SAXException e) {
            logger.error(e.getMessage());
        } catch (TikaException e) {
            logger.error(e.getMessage());
        } finally{
            IOUtils.closeQuietly(in);
        }
        return "";
    }

}
