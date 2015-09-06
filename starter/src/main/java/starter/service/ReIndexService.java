package starter.service;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.hppc.cursors.ObjectObjectCursor;
import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.RangeFilterBuilder;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import starter.RequestContext;
import starter.uContentException;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;


@Service
public class ReIndexService {

    @Autowired
    private RequestContext context;

    Logger logger = LoggerFactory.getLogger(ReIndexService.class);

    public void reIndex(){
        try {
            String[] indices = originalName(context.getIndex());
            for(String index : indices){
                copyMappings(index);
//                copyIndex(index, from, to);
            }
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
        int v = index.lastIndexOf("_v");
        String prefix = index.substring(0, v + 2);
        String suffix = index.substring(v + 2);
        String newSuffix = String.valueOf(Integer.valueOf(suffix) + 1);
        return prefix + newSuffix;
    }

    private void copyIndex(String index, Date from, Date to){
        BulkProcessor bulkProcessor = initBulkProcessor();
        try {
            RangeFilterBuilder filterBuilder = null;
            if (from != null) {
                filterBuilder = FilterBuilders.rangeFilter(Constant.FieldName.CREATEDON).from(from);
            }
            if (to != null) {
                filterBuilder.to(to);
            }

            SearchRequestBuilder searchRequestBuilder = context.getClient().prepareSearch(index).setSearchType(SearchType.SCAN).setScroll("1m");
            if (filterBuilder != null) {
                searchRequestBuilder.setPostFilter(filterBuilder);
            }
            SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
            do{
                searchResponse = context.getClient().prepareSearchScroll(searchResponse.getScrollId()).setScroll("1m").execute().get();
                for(SearchHit hit : searchResponse.getHits().getHits()){
                    String type = hit.getType();
                    Map<String, Object> source = hit.getSource();
                    bulkProcessor.add(new IndexRequest(name(index), type).source(source));
                }
            }while(searchResponse.getHits().getHits().length > 0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally{
            bulkProcessor.flush();
            bulkProcessor.close();
        }
    }


    private BulkProcessor initBulkProcessor(){
        return BulkProcessor.builder(context.getClient(), new BulkProcessor.Listener() {
            public void beforeBulk(long executionId, BulkRequest request) {
                logger.info(String.format("executionId: %s, 本次提交请求个数：%s", executionId, request.numberOfActions()));
            }

            public void afterBulk(long executionId, BulkRequest bulkRequest, BulkResponse response) {


            }

            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                logger.info(String.format("executionId: %s failed\r\n, %s", executionId, failure.getMessage()));
            }
        }).setBulkActions(3000).setFlushInterval(TimeValue.timeValueSeconds(5)).setConcurrentRequests(0).build();
    }





}
