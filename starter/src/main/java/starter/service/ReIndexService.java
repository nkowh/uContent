package starter.service;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.hppc.cursors.ObjectObjectCursor;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import starter.RequestContext;
import starter.uContentException;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;


@Service
public class ReIndexService {

    @Autowired
    private RequestContext context;

    Logger logger = LoggerFactory.getLogger(ReIndexService.class);

    public void reIndex(){
        try {
            copyMappings(context.getIndex());
        } catch (ExecutionException e) {//TODO 异常回滚？
            logger.error(e.getMessage());
            throw new uContentException("Copy mappings failed", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            throw new uContentException("Copy mappings failed", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new uContentException("Copy mappings failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private String[] originalName(String alias){
        Client client = context.getClient();
        if(!client.admin().indices().prepareExists(alias).execute().actionGet().isExists()){
            throw new RuntimeException("The index: " + alias + " which to be reIndexed is not exist");
        }
        GetIndexRequest getIndexRequest = new GetIndexRequest();
        getIndexRequest.indices(alias);
        return client.admin().indices().prepareGetIndex().setIndices(alias).execute().actionGet().indices();
    }

    private void copyMappings(String index) throws ExecutionException, InterruptedException, IOException {
        Client client = context.getClient();
        GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings(index).get();
        Iterator<ObjectObjectCursor<String, ImmutableOpenMap<String, MappingMetaData>>> iterator = getMappingsResponse.mappings().iterator();
        XContentBuilder xContentBuilder = XContentFactory.jsonBuilder();
        xContentBuilder.startObject();
        xContentBuilder.startObject("mappings");
        while(iterator.hasNext()){
            ObjectObjectCursor<String, ImmutableOpenMap<String, MappingMetaData>> entry = iterator.next();
            for(ObjectObjectCursor<String, MappingMetaData> typeEntry : entry.value){
                xContentBuilder.field(typeEntry.key);
                xContentBuilder.map(typeEntry.value.sourceAsMap());
            }
        }
        xContentBuilder.endObject().endObject();
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(name(index));
        createIndexRequest.source(xContentBuilder);
        client.admin().indices().create(createIndexRequest).actionGet();
    }

    private String name(String index){
//        if (StringUtils.isNotBlank(index)) {
//            int v = index.lastIndexOf("_v");
//            String prefix = index.substring(0, v + 2);
//            String suffix = index.substring(v + 2);
//            String newSuffix = String.valueOf(Integer.valueOf(suffix) + 1);
//            return prefix + newSuffix;
//        }
        return index + "xx";
    }




}
