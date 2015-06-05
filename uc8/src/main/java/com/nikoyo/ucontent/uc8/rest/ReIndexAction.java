package com.nikoyo.ucontent.uc8.rest;

import com.nikoyo.ucontent.uc8.file.FileSystem;
import com.nikoyo.ucontent.uc8.file.FileSystemFactory;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.hppc.cursors.ObjectObjectCursor;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;


/**
 * Created by panw on 2015/6/3.
 */
public class ReIndexAction extends BaseRestHandler {


    protected ReIndexAction(Settings settings, RestController controller, Client client) {
        super(settings, controller, client);
        controller.registerHandler(RestRequest.Method.PUT, "/_reIndex/{srcIndex}/{tarIndex}", this);
        controller.registerHandler(RestRequest.Method.POST, "/_reIndex/{srcIndex}/{tarIndex}", this);
    }


    private void copyIndexMappings(Client client, String srcIndex, String tarIndex) {
        if(!client.admin().indices().prepareExists(srcIndex).execute().actionGet().isExists()){
            throw new RuntimeException("The index: " + srcIndex + " which to be reIndexed is not exist");
        }
        if(client.admin().indices().prepareExists(tarIndex).execute().actionGet().isExists()){
            throw new RuntimeException("The index: " + tarIndex + " which to be reIndexed to is aleady exist");
        }
        GetMappingsRequest getMappingsRequest = new GetMappingsRequest();
        getMappingsRequest.indices(srcIndex);
        try {
            GetMappingsResponse getMappingsResponse = client.admin().indices().getMappings(getMappingsRequest).get();
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
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(tarIndex);
            createIndexRequest.source(xContentBuilder);
            client.admin().indices().create(createIndexRequest).actionGet();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BulkResponse doReIndex(Client client, RestRequest request){
        SearchRequest searchRequest = parseSearchRequest(request);
        try {
            SearchResponse response = client.search(searchRequest).get();
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            do{
                response = client.prepareSearchScroll(response.getScrollId()).setScroll("1m").execute().get();
                for(SearchHit hit : response.getHits().getHits()){
                    String type = hit.getType();
                    Map<String, Object> source = hit.getSource();
                    bulkRequest.add(client.prepareIndex(request.param("tarIndex"), type).setSource(source));
                }
            }while(response.getHits().getHits().length > 0);
            return bulkRequest.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SearchRequest parseSearchRequest(RestRequest request) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(request.param("srcIndex"));
        searchRequest.searchType(SearchType.SCAN);
        searchRequest.scroll("1m");
        searchRequest.extraSource(parseSearchSource(request));
        return searchRequest;
    }

    public static SearchSourceBuilder parseSearchSource(RestRequest request) {
        QueryBuilder queryBuilder = QueryBuilders.rangeQuery("_creation_time").from(request.param("index_from")).to(request.param("index_to"));
        return new SearchSourceBuilder().query(queryBuilder);
    }

    @Override
    protected void handleRequest(RestRequest request, RestChannel channel, Client client) throws Exception {
        this.copyIndexMappings(client, request.param("srcIndex"), request.param("tarIndex"));
        BulkResponse response = this.doReIndex(client, request);
        if (response.hasFailures()) {
            //TODO 日志
            new RuntimeException(response.buildFailureMessage());
        }
    }
}
