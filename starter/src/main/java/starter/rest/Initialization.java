package starter.rest;

import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import starter.RequestContext;
import starter.service.Constant;
import starter.service.GroupService;
import starter.service.UserService;
import starter.uContentException;

@RestController
@RequestMapping(value="initialization/",produces = MediaType.APPLICATION_JSON_VALUE)
public class Initialization {

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private RequestContext context;

    @RequestMapping(value = "status", method = RequestMethod.GET)
    public boolean checkInitialized() {
        Client client = context.getClient();

        //check indices
        IndicesExistsResponse indicesExistsResponse = client.admin().indices().prepareExists(context.getIndex()).execute().actionGet();
        if (!indicesExistsResponse.isExists()){
            return false;
        }

        //check user mapping
        GetMappingsResponse getUserMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(context.getIndex()).addTypes(Constant.FieldName.USERTYPENAME).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> userMappings = getUserMappingsResponse.getMappings();
        if(userMappings.size()==0){
            return false;
        }

        //check admin user
        if (!client.prepareGet(context.getIndex(), Constant.FieldName.USERTYPENAME, Constant.ADMIN).execute().actionGet().isExists()) {
            return false;
        }

        //check group mapping
        GetMappingsResponse getGroupMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(context.getIndex()).addTypes(Constant.FieldName.GROUPTYPENAME).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> groupMappings = getGroupMappingsResponse.getMappings();
        if(groupMappings.size()==0){
            return false;
        }

        //check everyone adminGroup
        if (!client.prepareGet(context.getIndex(), Constant.FieldName.GROUPTYPENAME, Constant.EVERYONE).execute().actionGet().isExists()) {
            return false;
        }
        if (!client.prepareGet(context.getIndex(), Constant.FieldName.GROUPTYPENAME, Constant.ADMINGROUP).execute().actionGet().isExists()) {
            return false;
        }

        return true;
    }

    @RequestMapping(value = "initial", method = RequestMethod.POST)
    public void systemDataInitialize(@RequestParam(defaultValue = "5") int shards,
                                     @RequestParam(defaultValue = "1") int replicas) {
        try {
            Client client = context.getClient();
            //check and create indices
            IndicesExistsResponse indicesExistsResponse = client.admin().indices().prepareExists(context.getIndex()).execute().actionGet();
            if (!indicesExistsResponse.isExists()){
                //添加分片和副本设置，默认五个主分片，一个副本
                Settings settings = ImmutableSettings.settingsBuilder().put("number_of_shards", shards).put("number_of_replicas", replicas).build();
                client.admin().indices().prepareCreate(context.getIndex()).setSettings(settings).execute().actionGet();
            }

            //check again
            if (client.admin().indices().prepareExists(context.getIndex()).execute().actionGet().isExists()){
                userService.initialUserData();
                groupService.initialGroupData();
            }
        } catch (Exception e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
