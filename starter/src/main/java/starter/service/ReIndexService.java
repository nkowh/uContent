package starter.service;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.hppc.cursors.ObjectObjectCursor;
import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.RangeFilterBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import starter.RequestContext;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class ReIndexService {

    @Autowired
    private RequestContext context;


    public XContentBuilder getReindexLog(String operationId, int size) throws IOException {
        SearchRequestBuilder searchRequestBuilder = context.getClient().prepareSearch("$system").setTypes("reindexLog").setSize(size).addSort("timestamp", SortOrder.DESC);
        String query = "{\"term\":{\"operationId\":" + operationId + "}}";
        searchRequestBuilder.setQuery(query);
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder().startObject();
        xContentBuilder.startArray();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            xContentBuilder.startObject();
            Iterator<Map.Entry<String, Object>> iterator = hit.getSource().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                xContentBuilder.field(entry.getKey(), entry.getValue());
            }
            xContentBuilder.endObject();
        }
        xContentBuilder.endArray().endObject();
        return xContentBuilder;
    }


    public XContentBuilder check() throws IOException {
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder().startObject();
        String index = context.getIndex();
        String[] indices = new String[1];
        indices[0] = "$system";
        TypesExistsRequest typesExistsRequest = new TypesExistsRequest(indices, "reindexSummary");
        TypesExistsResponse typesExistsResponse = context.getClient().admin().indices().typesExists(typesExistsRequest).actionGet();
        if (!typesExistsResponse.isExists()) {
            xContentBuilder.field("isFinished", true).endObject();
            return xContentBuilder;
        }
        SearchRequestBuilder searchRequestBuilder = context.getClient().prepareSearch("$system").setTypes("reindexSummary").addSort("operationId", SortOrder.DESC);
        String query = "{\"term\":{\"srcIndex\":\"" + index + "\"}}";
        searchRequestBuilder.setQuery(query);
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
        SearchHit[] hits = searchResponse.getHits().getHits();
        if (hits.length > 0) {
            SearchHit hit = hits[0];
            boolean isFinished = Boolean.valueOf(hit.getSource().get("isFinished").toString());
            xContentBuilder.field("isFinished", isFinished);
            if (!isFinished) {
                xContentBuilder.field("operationId", hit.getSource().get("operationId").toString())
                .field("srcIndex", hit.getSource().get("srcIndex").toString())
                .field("targetIndex", hit.getSource().get("targetIndex").toString())
                .field("dateFrom", hit.getSource().get("dateFrom"))
                .field("dateTo", hit.getSource().get("dateTo"))
                .field("total", hit.getSource().get("total"));
            }
            xContentBuilder.endObject();
        }else{
            xContentBuilder.field("isFinished", true).endObject();
        }
        return xContentBuilder;
    }

    public static class ReindexJob implements Runnable {

        private Client client;
        private String alias;
        private String target;
        private Date dateFrom = null;
        private Date dateTo = null;
        private long finished = 0l;

        private Logger logger = LoggerFactory.getLogger(ReindexJob.class);

        public ReindexJob(Client client, String alias, String target, Date dateFrom, Date dateTo) {
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
                if (indices.length < 0) {
                    return;
                }
                String newIndex = StringUtils.isNotBlank(target) ? target : name(indices[0]);
                copyMappings(indices[0], newIndex);
                copyIndex(indices[0], newIndex, dateFrom, dateTo);
                alias(indices, newIndex, alias);
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            } catch (ExecutionException e) {
                logger.error(e.getMessage());
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }

        private String[] originalName(String alias) {
            if (!client.admin().indices().prepareExists(alias).execute().actionGet().isExists()) {
                logger.error("The index: " + alias + " which to be reIndexed is not exist");
                throw new RuntimeException("The index: " + alias + " which to be reIndexed is not exist");
            }
            GetIndexRequest getIndexRequest = new GetIndexRequest();
            getIndexRequest.indices(alias);
            return client.admin().indices().prepareGetIndex().setIndices(alias).execute().actionGet().indices();
        }

        private void copyMappings(String index, String target) throws ExecutionException, InterruptedException, IOException {
            logger.info(String.format("copy mappings from %s to %s start......", index, target));
            IndicesExistsResponse indicesExistsResponse = client.admin().indices().prepareExists(target).execute().actionGet();
            if (indicesExistsResponse.isExists()) {
                return;
            }
            GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings(index).get();
            Iterator<ObjectObjectCursor<String, ImmutableOpenMap<String, MappingMetaData>>> iterator = getMappingsResponse.mappings().iterator();
            XContentBuilder xContentBuilder = XContentFactory.jsonBuilder();
            xContentBuilder.startObject();
            xContentBuilder.startObject("mappings");
            while (iterator.hasNext()) {
                ObjectObjectCursor<String, ImmutableOpenMap<String, MappingMetaData>> entry = iterator.next();
                for (ObjectObjectCursor<String, MappingMetaData> typeEntry : entry.value) {
                    xContentBuilder.field(typeEntry.key);
                    xContentBuilder.map(typeEntry.value.sourceAsMap());
                }
            }
            xContentBuilder.endObject().endObject();
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(target);
            createIndexRequest.source(xContentBuilder);
            client.admin().indices().create(createIndexRequest).actionGet();
            logger.info(String.format("copy mappings from %s to %s end.", index, target));
        }

        private String name(String index) {
            if (!index.contains("_v")) {
                return index + "_v1";
            }
            int v = index.lastIndexOf("_v");
            String prefix = index.substring(0, v + 2);
            String suffix = index.substring(v + 2);
            String newSuffix = String.valueOf(Integer.valueOf(suffix) + 1);
            return prefix + newSuffix;
        }

        private void copyIndex(String index, String target, Date from, Date to) throws IOException {
            BulkProcessor bulkProcessor = null;
            try {
                RangeFilterBuilder filterBuilder = null;
                if (from != null) {
                    filterBuilder = FilterBuilders.rangeFilter(Constant.FieldName.CREATEDON).from(from);
                }
                if (to != null) {
                    if (filterBuilder == null) {
                        filterBuilder = FilterBuilders.rangeFilter(Constant.FieldName.CREATEDON).to(to);
                    } else {
                        filterBuilder.to(to);
                    }
                }
                SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index).setSearchType(SearchType.SCAN).setScroll("1m");
                if (filterBuilder != null) {
                    searchRequestBuilder.setPostFilter(filterBuilder);
                }
                SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
                String operationId = new Date().getTime() + "";
                long total = searchResponse.getHits().getTotalHits();
                summary(operationId, index, target, dateFrom, dateTo, total);//记录此次reindex的总述信息
                bulkProcessor = initBulkProcessor(operationId, total);
                logger.info(String.format("copy index from %s to %s start......", index, target));
                do {
                    searchResponse = client.prepareSearchScroll(searchResponse.getScrollId()).setScroll("1m").execute().actionGet();
                    for (SearchHit hit : searchResponse.getHits().getHits()) {
                        String type = hit.getType();
                        Map<String, Object> source = hit.getSource();
                        bulkProcessor.add(new IndexRequest(target, type, hit.getId()).source(source));
                    }
                } while (searchResponse.getHits().getHits().length > 0);
            } finally {
                bulkProcessor.flush();
                bulkProcessor.close();
                logger.info(String.format("copy index from %s to %s end.", index, target));
            }
        }

        private void summary(String operationId, String srcIndex, String target, Date dateFrom, Date dateTo, long total) throws IOException {
            XContentBuilder xContentBuilder = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("operationId", operationId)
                    .field("srcIndex", srcIndex)
                    .field("targetIndex", target)
                    .field("dateFrom", dateFrom)
                    .field("dateTo", dateTo)
                    .field("total", total)
                    .field("isFinished", false)
                    .endObject();
            client.prepareIndex("$system", "reindexSummary", operationId).setSource(xContentBuilder).execute().actionGet();
            logger.info(String.format("log reindexSummary, operationId=%s", operationId));

        }

        private BulkProcessor initBulkProcessor(final String operationId, final long total) {
            return BulkProcessor.builder(client, new BulkProcessor.Listener() {
                public void beforeBulk(long executionId, BulkRequest request) {
                    logger.info(String.format("executionId:%s, numberOfActions:%s", executionId, request.numberOfActions()));
                }

                public void afterBulk(long executionId, BulkRequest bulkRequest, BulkResponse response) {
                    try {
                        double rate = ((bulkRequest.numberOfActions() + finished) / total) * 100;
                        XContentBuilder xContentBuilder = XContentFactory.jsonBuilder()
                                .startObject()
                                .field("operationId", operationId)
                                .field("numberOfActions", bulkRequest.numberOfActions())
                                .field("finished", bulkRequest.numberOfActions() + finished)
                                .field("total", total)
                                .field("rate", rate)
                                .field("timestamp", new Date().getTime())
                                .field("executionId", executionId)
                                .endObject();
                        client.prepareIndex("$system", "reindexLog").setSource(xContentBuilder).execute().actionGet();
                        if (bulkRequest.numberOfActions() + finished == total) {
                            client.prepareUpdate("$system", "reindexSummary", operationId).setDoc("isFinished", true).execute().actionGet();
                        }
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


        private void alias(String[] indices, String newIndex, String alias) {
            IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
            indicesAliasesRequest.removeAlias(indices, alias);
            indicesAliasesRequest.addAlias(alias, newIndex);
            client.admin().indices().aliases(indicesAliasesRequest, new ActionListener<IndicesAliasesResponse>() {
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

}
