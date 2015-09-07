package com.nikoyo.ucontent.uc8.rest;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.rest.*;
import org.elasticsearch.threadpool.ThreadPool;

import java.io.*;
import java.util.Date;
import java.util.UUID;

/**
 * Created by Administrator on 2015/7/8.
 */
public class ImportAction extends BaseRestHandler {

    private static String PATH = "d:\\exportDir";
    private static final String TYPE_PREFIX = "{\"mappings\":";
    private static final String TYPE_SUFFIX= "}";

    @Inject
    private ThreadPool threadPool;

    @Inject
    protected ImportAction(Settings settings, RestController controller, Client client) {
        super(settings, controller, client);
//        controller.registerHandler(RestRequest.Method.PUT, "/_reindex/{index}", this);
//        controller.registerHandler(RestRequest.Method.POST, "/_reindex/{index}", this);
        controller.registerHandler(RestRequest.Method.GET, "/_import/{index}/{type}", this);
    }

    @Override
    protected void handleRequest(RestRequest request, RestChannel channel, Client client) throws Exception {
        String token = UUID.randomUUID().toString();
        threadPool.scheduler().execute(new ImportService(request, channel, client, token));
        XContentBuilder builder = channel.newBuilder().startObject().field("token", token).endObject();
        channel.sendResponse(new BytesRestResponse(RestStatus.OK, builder));
    }

    private static void importMapping(Client client, RestRequest request) {
        String index = request.param("index");
        String type = request.param("type");
        if (StringUtils.isBlank(index) || StringUtils.isBlank(type)) {
            return;
        }
        if (client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists()) {
            String[] indices = {index};
            if(client.admin().indices().typesExists(new TypesExistsRequest(indices, type)).actionGet().isExists()){
                throw new RuntimeException("The type: " + type + " in " + index  + " is already exist");
            }
        }
        File file = new File(PATH + File.separator + "_mapping_" + index + "_" + type + ".json") ;
        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String tempString = null;
                while ((tempString = reader.readLine()) != null) {
                    CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
                    createIndexRequest.source(TYPE_PREFIX + tempString + TYPE_SUFFIX);
                    client.admin().indices().create(createIndexRequest).actionGet();
                }
                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static void importDoc(Client client, RestRequest request, String token){
        String index = request.param("index");
        String type = request.param("type");
        BulkProcessor bulkProcessor = initBulkProcessor(request, client, token, 0);
        File file = new File(PATH + File.separator + "_doc_" + index + "_" + type + ".json");
        if (file.exists() && file.isFile()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String tempString = null;
                while ((tempString = reader.readLine()) != null) {
                    bulkProcessor.add(new IndexRequest(index, type).source(tempString));
                }
                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                    UpdateRequest updateRequest = new UpdateRequest("system", "import_process", token)
                            .script("ctx._source.finished += count").addScriptParam("count", bulkRequest.numberOfActions())
                            .upsert(XContentFactory.jsonBuilder()
                                    .startObject()
                                    .field("token", token)
                                    .field("total", total)
                                    .field("finished", bulkRequest.numberOfActions())
                                    .field("startAt", new Date())
                                    .field("condition", String.format("index: %s, type:%s"), request.param("index"), request.param("type"))
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
                        client.prepareIndex("system", "import_failures").setSource(xContentBuilder).execute();
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

    class ImportService implements Runnable{
        private RestRequest request;
        private RestChannel channel;
        private Client client;
        private String token;

        public ImportService(RestRequest request, RestChannel channel, Client client, String token) {
            this.request = request;
            this.channel = channel;
            this.client = client;
            this.token = token;
        }

        public void run() {
            ImportAction.importMapping(client, request);
            ImportAction.importDoc(client, request, token);
        }
    }
}
