package com.nikoyo.ucontent.uc8.rest;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.count.CountRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.QuerySourceBuilder;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.hppc.cursors.ObjectObjectCursor;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.rest.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.threadpool.ThreadPool;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;


/** 重建索引
 * Created by panw on 2015/6/3.
 */
public class ReIndexAction extends BaseRestHandler {

    @Inject
    private ThreadPool threadPool;


    @Inject
    protected ReIndexAction(Settings settings, RestController controller, Client client) {
        super(settings, controller, client);
//        controller.registerHandler(RestRequest.Method.PUT, "/_reindex/{index}", this);
//        controller.registerHandler(RestRequest.Method.POST, "/_reindex/{index}", this);
        controller.registerHandler(RestRequest.Method.GET, "/_reindex/{index}", this);
    }



    @Override
    protected void handleRequest(RestRequest request, RestChannel channel, Client client) throws Exception{
        String token = UUID.randomUUID().toString();
        threadPool.scheduler().execute(new ReindexService(request, channel, client, token));
        XContentBuilder builder = channel.newBuilder().startObject().field("token", token).endObject();
        channel.sendResponse(new BytesRestResponse(RestStatus.OK, builder));
    }


    /**拷贝索引的mappings信息
     * @param client
     * @param index 需要重建的索引
     * @param newIndex 新索引
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     */
    private static void copyIndexMappings(Client client, String index, String newIndex) throws ExecutionException, InterruptedException, IOException {
        GetMappingsRequest getMappingsRequest = new GetMappingsRequest();
        getMappingsRequest.indices(index);
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
                CreateIndexRequest createIndexRequest = new CreateIndexRequest("");
                createIndexRequest.settings().
            }
        }
        xContentBuilder.endObject().endObject();
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(newIndex);
        createIndexRequest.source(xContentBuilder);
        client.admin().indices().create(createIndexRequest).actionGet();
    }

    /** 拷贝数据
     * @param client
     * @param src 源索引
     * @param target 目标索引
     * @param request
     * @param token 本次重建唯一标识
     * @param total 本次重建总的记录数
     */
    private static void doReIndex(Client client, String src, String target, RestRequest request, String token, long total){
        SearchRequest searchRequest = parseSearchRequest(request);
        searchRequest.indices(src);
        BulkProcessor bulkProcessor = initBulkProcessor(request, client, token, total);
        try {
            SearchResponse response = client.search(searchRequest).get();
            do{
                response = client.prepareSearchScroll(response.getScrollId()).setScroll("1m").execute().get();
                for(SearchHit hit : response.getHits().getHits()){
                    String type = hit.getType();
                    Map<String, Object> source = hit.getSource();
                    bulkProcessor.add(new IndexRequest(target, type).source(source));
                }
            }while(response.getHits().getHits().length > 0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally{
            bulkProcessor.flush();
            bulkProcessor.close();
        }
    }

    private static SearchRequest parseSearchRequest(RestRequest request) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.searchType(SearchType.SCAN);
        searchRequest.scroll("1m");
        QueryBuilder queryBuilder = parseQueryBuilder(request);
        if (queryBuilder != null) {
            searchRequest.extraSource(new SearchSourceBuilder().query(queryBuilder));
        }
        return searchRequest;
    }

    private static QueryBuilder parseQueryBuilder(RestRequest request) {
        RangeQueryBuilder rangeQueryBuilder = null;
        if(request.param("index_from") != null && !request.param("index_from").trim().equals("")){
            rangeQueryBuilder = QueryBuilders.rangeQuery("_createAt");
            rangeQueryBuilder.from(request.param("index_from"));
        }
        if(request.param("index_from") != null && !request.param("index_from").trim().equals("")){
            if (rangeQueryBuilder == null) {
                rangeQueryBuilder = QueryBuilders.rangeQuery("_createAt");
            }
            rangeQueryBuilder.to(request.param("index_from"));
        }
        return rangeQueryBuilder;
    }

    private static BulkProcessor initBulkProcessor(final RestRequest request, final Client client, final String token, final long total){
        return BulkProcessor.builder(client, new BulkProcessor.Listener() {
            public void beforeBulk(long executionId, BulkRequest request) {
                //TODO
                System.out.println("executionId:" + executionId + "本次提交请求个数：" + request.numberOfActions());
            }

            public void afterBulk(long executionId, BulkRequest bulkRequest, BulkResponse response) {
                //记录该批次结果信息
                try {
                    UpdateRequest updateRequest = new UpdateRequest("system", "reindex_process", token)
                            .script("ctx._source.finished += count").addScriptParam("count", bulkRequest.numberOfActions())
                            .upsert(XContentFactory.jsonBuilder()
                                    .startObject()
                                    .field("token", token)
                                    .field("total", total)
                                    .field("finished", bulkRequest.numberOfActions())
                                    .field("startAt", new Date())
                                    .field("condition", "")
                                    .endObject());
                    client.update(updateRequest);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // 记录该批次错误信息
                BulkItemResponse[] responseItems = response.getItems();
                XContentBuilder xContentBuilder = null;
                try {
                    for(BulkItemResponse item : responseItems){
                        if (item.isFailed()) {
                            if(xContentBuilder == null){
                                xContentBuilder = XContentFactory.jsonBuilder().startObject()
                                        .field("token", token)
                                        .field("createAt", new Date())
                                        .startArray("failure");
                            }
                            xContentBuilder.startObject()
                                    .field("index", item.getIndex())
                                    .field("type", item.getType())
                                    .field("id", item.getId())
                                    .field("failureMessage", item.getFailureMessage())
                                    .endObject();
                        }
                    }
                    if(xContentBuilder != null){
                        xContentBuilder.endArray().endObject();
                        client.prepareIndex("system", "reindex_failures").setSource(xContentBuilder).execute();
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
            }

            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                System.out.println(failure.getMessage());
            }
        }).setBulkActions(3000).setFlushInterval(TimeValue.timeValueSeconds(5)).setConcurrentRequests(0).build();
    }

    /** 通过别名查询索引名
     * @param client
     * @param alias
     * @return
     */
    private static String[] originalName(Client client, String alias){
        if(!client.admin().indices().prepareExists(alias).execute().actionGet().isExists()){
            //TODO 日志
            throw new RuntimeException("The index: " + alias + " which to be reIndexed is not exist");
        }
        GetIndexRequest getIndexRequest = new GetIndexRequest();
        getIndexRequest.indices(alias);
        return client.admin().indices().getIndex(getIndexRequest).actionGet().indices();
    }

    private static void clear(Client client, Set<String> indexes){
        for(String name : indexes){
            client.admin().indices().prepareDelete(name).execute();
        }
    }



    class ReindexService implements Runnable{
        private RestRequest request;
        private RestChannel channel;
        private Client client;
        private String token;

        public ReindexService(RestRequest request, RestChannel channel, Client client, String token) {
            this.request = request;
            this.channel = channel;
            this.client = client;
            this.token = token;
        }

        public void run() {
            String[] indexes = ReIndexAction.originalName(client, request.param("index"));
            if(indexes == null || indexes.length == 0){
                //TODO 日志
                return;
            }
            //计算需要迁移的总数
            CountRequest countRequest = new CountRequest(request.param("index"));
            QueryBuilder queryBuilder = parseQueryBuilder(request);
            if(queryBuilder != null){
                countRequest.source(new QuerySourceBuilder().setQuery(queryBuilder));
            }
            long total = client.count(countRequest).actionGet().getCount();
            if (total <= 0) {
                //TODO 日志
                return;
            }
            Set<String> newIndexes = new HashSet<String>();
            for(String s : indexes){
                //索引的名称以"V + 数字"结尾
                int i = s.lastIndexOf("v");
                String prefix = s.substring(0, i + 1);
                int suffix = Integer.valueOf(s.substring(s.lastIndexOf("v") + 1)) + 1;
                String newName = prefix + suffix;
                while(client.admin().indices().prepareExists(newName).execute().actionGet().isExists()){
                    newName = prefix + (++suffix);
                }
                try {
                    //复制mappings
                    ReIndexAction.copyIndexMappings(client, s, newName);
                } catch (Exception e) {
                    // TODO 日志
                    e.printStackTrace();
                    //出现异常回滚
                    ReIndexAction.clear(client, newIndexes);
                    return;
                }
                newIndexes.add(newName);
                //拷贝数据
                ReIndexAction.doReIndex(client, s, newName, request, token, total);
            }
            //更新别名
            IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
            indicesAliasesRequest.removeAlias(indexes, request.param("index"));
            indicesAliasesRequest.addAlias(request.param("index"), (String[]) newIndexes.toArray(new String[0]));
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
    }

    public void aa(Client client){
        SearchRequest searchRequest = new SearchRequest();
    }




}
