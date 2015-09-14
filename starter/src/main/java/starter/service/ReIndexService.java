package starter.service;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.hppc.cursors.ObjectObjectCursor;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.RangeFilterBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import starter.rest.Json;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class ReIndexService implements Runnable{

    private Client client;
    private String alias;
    private String target;
    private Date dateFrom = null;
    private Date dateTo = null;
    private long finished = 0l;

    Logger logger = LoggerFactory.getLogger(ReIndexService.class);

    public ReIndexService() {
    }

    public ReIndexService(Client client, String alias, String target, Date dateFrom, Date dateTo) {
        this.client = client;
        this.alias = alias;
        this.target = target;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }


    @Override
    public void run() {
        try {
            String[] indices = originalName(alias);
            for(String index : indices){
                String newIndex = StringUtils.isNotBlank(target) ? target : name(index);
                check(newIndex);
                copyMappings(index, newIndex);
                copyIndex(index, newIndex, dateFrom, dateTo);
            }
//            alias(indices, alias);
        } catch (InterruptedException e) {
           logger.error(e.getMessage());
        } catch (ExecutionException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void check(String index) {
        if (client.admin().indices().prepareTypesExists(index).setTypes("$reindex").execute().actionGet().isExists()) {
            SearchResponse $reindex = client.prepareSearch("$system").setTypes("$reindex").addSort(Constant.FieldName.CREATEDON, SortOrder.DESC).execute().actionGet();
            SearchHit[] hits = $reindex.getHits().getHits();
            if(hits.length > 0){
                SearchHit last = hits[0];
                long finished = Long.valueOf(last.getSource().get("finished").toString());
                long total = Long.valueOf(last.getSource().get("total").toString());
                if(finished < total){
                    logger.error("Current reindex operation canceled, because there exist a reindex job");
                    //TODO
                    throw new RuntimeException("Current reindex operation canceled, because there exist a reindex job");
                }
            }
        }
    }


    private String[] originalName(String alias){
        if(!client.admin().indices().prepareExists(alias).execute().actionGet().isExists()){
            logger.error("The index: " + alias + " which to be reIndexed is not exist");
            throw new RuntimeException("The index: " + alias + " which to be reIndexed is not exist");
        }
        GetIndexRequest getIndexRequest = new GetIndexRequest();
        getIndexRequest.indices(alias);
        return client.admin().indices().prepareGetIndex().setIndices(alias).execute().actionGet().indices();
    }

    private void copyMappings(String index, String target) throws ExecutionException, InterruptedException, IOException {
        IndicesExistsResponse indicesExistsResponse = client.admin().indices().prepareExists(target).execute().actionGet();
        if (indicesExistsResponse.isExists()) {
            return;
        }
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
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(target);
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

    private void copyIndex(String index, String target, Date from, Date to){
        BulkProcessor bulkProcessor = null;
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
            SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index).setSearchType(SearchType.SCAN).setScroll("1m");
            if (filterBuilder != null) {
                searchRequestBuilder.setPostFilter(filterBuilder);
            }
            SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            bulkProcessor = initBulkProcessor(index, target, sdf.format(new Date()), searchResponse.getHits().getTotalHits());
            do{
                searchResponse = client.prepareSearchScroll(searchResponse.getScrollId()).setScroll("1m").execute().actionGet();
                for(SearchHit hit : searchResponse.getHits().getHits()){
                    String type = hit.getType();
                    Map<String, Object> source = hit.getSource();
                    bulkProcessor.add(new IndexRequest(target, type).source(source));
                }
            }while(searchResponse.getHits().getHits().length > 0);
        } finally{
            bulkProcessor.flush();
            bulkProcessor.close();
        }
    }

    private BulkProcessor initBulkProcessor(final String index, final String target, final String operationId, final long total){
        return BulkProcessor.builder(client, new BulkProcessor.Listener() {
            public void beforeBulk(long executionId, BulkRequest request) {
                logger.info(String.format("executionId:%s, numberOfActions:%s", executionId, request.numberOfActions()));
            }

            public void afterBulk(long executionId, BulkRequest bulkRequest, BulkResponse response) {
            //TODO 进度记录
                try {
                    XContentBuilder xContentBuilder = XContentFactory.jsonBuilder()
                            .startObject()
                            .field("operationId", operationId)
                            .field("srcIndex", index)
                            .field("targetIndex", target)
                            .field("finished", bulkRequest.numberOfActions() + finished)
                            .field("total", total)
                            .field(Constant.FieldName.CREATEDON, new DateTime().toLocalDateTime())
                            .endObject();
                    client.prepareIndex(target, "$reindex", operationId).setSource(xContentBuilder).execute().actionGet();
                    finished += bulkRequest.numberOfActions();
                    logger.info(xContentBuilder.string());
                } catch (IOException e) {
                   logger.error(e.getMessage());
                }
            }

            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                logger.info(String.format("executionId: %s failed\r\n, %s", executionId, failure.getMessage()));
            }
        }).setBulkActions(3000).setFlushInterval(TimeValue.timeValueSeconds(5)).setConcurrentRequests(0).build();
    }


    private void alias(String[] indices, String alias){
        client.admin().indices().prepareAliases().removeAlias(indices, alias).execute().actionGet();
        List<String> newIndices = new ArrayList<String>();
        for(String s : indices){
            newIndices.add(name(s));
        }
        IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
        indicesAliasesRequest.removeAlias(indices, alias);
        indicesAliasesRequest.addAlias(alias, newIndices.toArray(new String[]{}));
        client.admin().indices().aliases(indicesAliasesRequest, new ActionListener<IndicesAliasesResponse>(){
            public void onResponse(IndicesAliasesResponse indicesAliasesResponse) {
                //TODO 删除原索引
                //client.admin().indices().prepareDelete(request.param("index")).execute();
            }
            public void onFailure(Throwable e) {
                //TODO
            }
        });
    }

    public Json getLog(Client client, String alias, String operationId){
        GetResponse getResponse = client.prepareGet(alias, "$reindex", operationId).execute().actionGet();
        Json source = (Json) getResponse.getSource();
        return source;
    }





}
