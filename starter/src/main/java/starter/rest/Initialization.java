package starter.rest;

import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
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
@RequestMapping(value="initialized/",produces = MediaType.APPLICATION_JSON_VALUE)
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

    @RequestMapping(value = "", method = RequestMethod.POST)
    public void systemDataInitialize() {
        try {
            userService.initialUserData();
            groupService.initialGroupData();
        } catch (Exception e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
