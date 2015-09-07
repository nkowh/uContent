package starter.service;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
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
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.RangeFilterBuilder;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.threadpool.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import starter.RequestContext;
import starter.uContentException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;


@Service
public class ReIndexService implements Runnable{

    @Autowired
    private RequestContext context;

    private Date dateFrom = null;
    private Date dateTo = null;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    Logger logger = LoggerFactory.getLogger(ReIndexService.class);

    public ReIndexService(Date dateFrom, Date dateTo) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    @Override
    public void run() {
        String[] indices = originalName(context.getIndex());
        try {
            for(String index : indices){
                copyMappings(index);
                copyIndex(index, dateFrom, dateTo);
            }
            alias(indices, context.getIndex());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
        if (!index.contains("_v")) {
            return index + "_v1";
        }
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
                if (filterBuilder == null) {
                    filterBuilder = FilterBuilders.rangeFilter(Constant.FieldName.CREATEDON).to(to);
                }else{
                    filterBuilder.to(to);
                }
            }
            SearchRequestBuilder searchRequestBuilder = context.getClient().prepareSearch(index).setSearchType(SearchType.SCAN).setScroll("1m");
            if (filterBuilder != null) {
                searchRequestBuilder.setPostFilter(filterBuilder);
            }
            SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
            do{
                searchResponse = context.getClient().prepareSearchScroll(searchResponse.getScrollId()).setScroll("1m").execute().actionGet();
                for(SearchHit hit : searchResponse.getHits().getHits()){
                    String type = hit.getType();
                    Map<String, Object> source = hit.getSource();
                    bulkProcessor.add(new IndexRequest(name(index), type).source(source));
                }
            }while(searchResponse.getHits().getHits().length > 0);
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
            //TODO 进度记录

            }

            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                logger.info(String.format("executionId: %s failed\r\n, %s", executionId, failure.getMessage()));
            }
        }).setBulkActions(3000).setFlushInterval(TimeValue.timeValueSeconds(5)).setConcurrentRequests(0).build();
    }


    private void alias(String[] indices, String alias){
        context.getClient().admin().indices().prepareAliases().removeAlias(indices, alias).execute().actionGet();
        List<String> newIndices = new ArrayList<String>();
        for(String s : indices){
            newIndices.add(name(s));
        }
        IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
        indicesAliasesRequest.removeAlias(indices, alias);
        indicesAliasesRequest.addAlias(alias, newIndices.toArray(new String[]{}));
        context.getClient().admin().indices().aliases(indicesAliasesRequest, new ActionListener<IndicesAliasesResponse>(){
            public void onResponse(IndicesAliasesResponse indicesAliasesResponse) {
                //TODO 删除原索引
                //client.admin().indices().prepareDelete(request.param("index")).execute();
            }
            public void onFailure(Throwable e) {
                //TODO
            }
        });
    }




}
