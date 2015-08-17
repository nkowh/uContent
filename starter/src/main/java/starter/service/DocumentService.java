package starter.service;


import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.get.GetField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import starter.RequestContext;
import starter.rest.Json;
import starter.service.fs.FileSystem;
import starter.uContentException;

import java.io.IOException;
import java.util.*;

@Service
public class DocumentService {

    @Autowired
    private RequestContext context;

    @Autowired
    private FileSystem fs;

    @Autowired
    private ValidateUtils validateUtils;

    public XContentBuilder query(String type, Json parse, int start, int limit, String sort) throws IOException {
        Json filter = new Json();
        Map<String, Object> m1 = new HashMap<String, Object>();
//        m1.put("term","user",context.getUserName())
        context.getClient().prepareSearch().setQuery(parse).setFrom(start).setSize(limit);
        return null;
    }

    public XContentBuilder create(String type, Json body) throws IOException {
        beforeCreate(body);
        IndexResponse indexResponse = context.getClient().prepareIndex(context.getIndex(), type).setSource(body).execute().actionGet();
        XContentBuilder builder = JsonXContent.contentBuilder();
        builder.startObject()
                .field("_index", indexResponse.getIndex())
                .field("_type", indexResponse.getType())
                .field("_id", indexResponse.getId())
                .field("_version", indexResponse.getVersion())
                .field("created", indexResponse.isCreated())
                .endObject();
        return builder;
    }

    public XContentBuilder create(String type, Json body, List<MultipartFile> files) throws IOException {
        if (!files.isEmpty()) {
            List<Map<String, Object>> pages = new ArrayList<Map<String, Object>>();
            for(MultipartFile file : files){
                Map<String, Object> page = new HashMap<String, Object>();
                page.put("name", file.getName());
                page.put("size", file.getSize());
                page.put("contentType", file.getContentType());
                String fileId = fs.write(file.getBytes());
                page.put("fileId", fileId);
                pages.add(page);
            }
            body.put("pages", pages);
        }
        return create(type, body);
    }

    public Json head(String type, String id) throws IOException {
        return get(type, id, true, false);
    }

    public Json get(String type, String id, boolean head, boolean allowableActions) throws IOException {
        GetResponse getResponse = context.getClient().prepareGet(context.getIndex(), type, id).execute().actionGet();
        return processGet(getResponse, head, allowableActions);
    }

    public Json update(String type, String id, Json body) throws IOException {
        GetResponse getResponse = context.getClient().prepareGet(context.getIndex(), type, id).execute().actionGet();
        if (!getResponse.isExists()) {
            throw new uContentException("Not found", HttpStatus.NOT_FOUND);
        }
        if (!hasPermission(context.getUserName(), getResponse.getSource().get("_acl"), Constant.Permission.UPDATE)) {
            throw new uContentException("Forbidden", HttpStatus.FORBIDDEN);
        }
        processJson(body);
        processAcl(body.get("_acl"), getResponse.getSource().get("_acl"));
        UpdateResponse updateResponse = context.getClient().prepareUpdate(context.getIndex(), type, id).setDoc(body).execute().actionGet();
        Json json = new Json();
        json.put("_index", context.getIndex());
        json.put("_type", type);
        json.put("_id", id);
        json.put("_version", updateResponse.getVersion());
        json.put("_isCreated", updateResponse.isCreated());
        return json;
    }

    public Json patch(String type, String id, Json body) throws IOException {
        return update(type, id, body);
    }

    public Json delete(String type, String id) throws IOException {
        GetResponse getResponse = context.getClient().prepareGet(context.getIndex(), type, id).execute().actionGet();
        if (!getResponse.isExists()) {
            throw new uContentException("Not found", HttpStatus.NOT_FOUND);
        }
        if (!hasPermission(context.getUserName(), getResponse.getSource().get("_acl"), Constant.Permission.DELETE)) {
            throw new uContentException("Forbidden", HttpStatus.FORBIDDEN);
        }
        Json json = new Json();
        json.put("_index", context.getIndex());
        json.put("_type", type);
        json.put("_id", id);
        DeleteResponse deleteResponse = context.getClient().prepareDelete(context.getIndex(), type, id).execute().actionGet();
        json.put("_found", deleteResponse.isFound());
        if (deleteResponse.isFound()) {
            json.put("_version", deleteResponse.getVersion());
        }
        return json;
    }


    private void beforeCreate(Json body){
        body.put("createdBy", context.getUserName());
        body.put("created", new DateTime());
        List<Object> permission = new ArrayList<Object>();
        permission.add(Constant.Permission.READ);
        permission.add(Constant.Permission.WRITE);
        permission.add(Constant.Permission.UPDATE);
        permission.add(Constant.Permission.DELETE);
        Map<String, Object> ace = new HashMap<String, Object>();
        ace.put("user", context.getUserName());
        ace.put("permission", permission);
        List<Map<String, Object>> acl = new ArrayList<Map<String, Object>>();
        acl.add(ace);
        body.put("_acl",acl);
    }

    private Json processGet(GetResponse getResponse, boolean head, boolean allowableActions) throws IOException {
        Json json = new Json();
        json.put("_index", getResponse.getIndex());
        json.put("_type", getResponse.getType());
        json.put("_id", getResponse.getId());
        json.put("_found", getResponse.isExists());
        if (getResponse.isExists()) {
            Object acl = getResponse.getSource().get("_acl");
            if(!hasPermission(context.getUserName(), acl, Constant.Permission.READ)){
                throw new uContentException("Forbidden", HttpStatus.FORBIDDEN);
            }
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
                json.put("_allowableActions", getUserPermission(context.getUserName(), acl));
            }
        }
        return json;
    }


    private Set getUserPermission(String user, Object acl){
        Set uPermission = getPermissionByUser(user, acl);
        List<String> groups = getGroups(user);
        Set gPermission = getPermissionByGroups(groups, acl);
        uPermission.addAll(gPermission);
        return uPermission;
    }

    private List<String> getGroups(String user){
        List<String> groups = new ArrayList<String>();
        return groups;
    }

    private Set getPermissionByUser(String user, Object acl){
        List<Map<String, Object>> _acl = (List<Map<String, Object>>)acl;
        Set permission = new HashSet();
        if (_acl != null && !_acl.isEmpty()) {
            for(Map<String, Object> map : _acl){
                Object u = map.get("user");
                if(u != null && u.toString().equals(user)){
                    permission.addAll((List)map.get("permission"));
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
                Object u = map.get("group");
                if(u != null && groups.contains(u.toString())){
                    permission.addAll((List)map.get("permission"));
                }
            }
        }
        return permission;
    }

    public boolean hasPermission(String user, Object acl, Constant.Permission action){
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

    private void processJson(Json body){
        if(body != null){
            body.put("lastUpdatedBy", context.getUserName());
            body.put("lastUpdated", new DateTime());
        }
    }

    private void processAcl(Object o1, Object o2){
        if(o1 != null && o2 != null){
            Map<String, Object> acl1 = (Map<String, Object>) o1;
            List<Map<String, Object>> acl2 = (List<Map<String, Object>>) o2;
            Object obj1 = acl1.get("add");
            if (obj1 != null) {
                List<Map<String, Object>> add = (List<Map<String, Object>>) obj1;
                for(Map<String, Object> m : add){
                    Object u = m.get("user");
                    if (u != null) {//用户
                        for(Map<String, Object> src_m : acl2){
                            Object user = src_m.get("user");
                            if (user != null && user.toString().equals(u.toString())) {
                                List<String> permission = (List<String>)src_m.get("permission");
                                permission.addAll((List<String>)m.get("permission"));
                            }
                        }
                    }
                    Object g = m.get("group");
                    if (g != null) {//用户
                        for(Map<String, Object> src_m : acl2){
                            Object user = src_m.get("group");
                            if (user != null && user.toString().equals(u.toString())) {
                                List<String> permission = (List<String>)src_m.get("permission");
                                permission.addAll((List<String>)m.get("permission"));
                            }
                        }
                    }

                }
            }
            Object obj2 = acl1.get("remove");


        }
    }




    public static void main(String[] args) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("a", "aaa");
//        map.put("b", "ccc");
        List<Map<String, Object>> acl = new ArrayList<Map<String, Object>>();
        Map<String, Object> ace1 = new HashMap<String, Object>();
        ace1.put("user", "zhangsan");
        List<String> permission = new ArrayList<String>();
        permission.add("Read");
        permission.add("Write");
        ace1.put("permission", permission);
        acl.add(ace1);

        Map<String, Object> ace2 = new HashMap<String, Object>();
        ace2.put("group", "group1");
        List<String> permission2 = new ArrayList<String>();
        permission2.add("Read");
        permission2.add("Delete");
        ace2.put("permission", permission);
        acl.add(ace2);

        map.put("_acl", acl);


        Map<String, Object> m = new HashMap<String, Object>();
        m.put("xxxxxxx", "xxxxxxxx1111111111xx");

        Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "starter").build();
        TransportClient client = new TransportClient(settings);
        String host = "192.168.1.20:9300";
        String[] parts = host.split(":");
        client.addTransportAddress(new InetSocketTransportAddress(parts[0], Integer.parseInt(parts[1])));

//        client.prepareIndex("pw", "type1").setId("AU85xh780qpDYigdfgWc").setSource(map).execute().actionGet();

        XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
        xContentBuilder.field("_source").value(m);
//        client.prepareUpdate("pw", "type1", "AU85xh780qpDYigdfgWc").setSource().execute().actionGet();
    }

}
