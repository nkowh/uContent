package com.nikoyo.ucontent.uc8.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.count.CountRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.hppc.cursors.ObjectObjectCursor;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.rest.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.threadpool.ThreadPool;

import java.io.*;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Created by Administrator on 2015/7/6.
 */
public class ExportAction extends BaseRestHandler {

    private static String PATH = "d:\\exportDir";
    private static ObjectMapper mapper = new ObjectMapper();

    @Inject
    private ThreadPool threadPool;

    @Inject
    protected ExportAction(Settings settings, RestController controller, Client client) {
        super(settings, controller, client);
//        controller.registerHandler(RestRequest.Method.PUT, "/_reindex/{index}", this);
//        controller.registerHandler(RestRequest.Method.POST, "/_reindex/{index}", this);
        controller.registerHandler(RestRequest.Method.GET, "/_export/{index}/{type}", this);
        File directory = new File(PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }


    @Override
    protected void handleRequest(RestRequest request, RestChannel channel, Client client) throws Exception {
        String token = UUID.randomUUID().toString();
        System.out.println("handleRequest:" + token);
        threadPool.scheduler().execute(new ExportService(request, channel, client, token));
        XContentBuilder builder = channel.newBuilder().startObject().field("token", token).endObject();
        channel.sendResponse(new BytesRestResponse(RestStatus.OK, builder));
    }

    private static void exportIndexMappings(Client client, RestRequest request) {
        String index = request.param("index");
        String type = request.param("type");
        GetMappingsRequest getMappingsRequest = new GetMappingsRequest();
        getMappingsRequest.indices(index);
        getMappingsRequest.types(type);
        try {
            GetMappingsResponse getMappingsResponse = client.admin().indices().getMappings(getMappingsRequest).get();
            Iterator<ObjectObjectCursor<String, ImmutableOpenMap<String, MappingMetaData>>> iterator = getMappingsResponse.mappings().iterator();
            while(iterator.hasNext()){
                ObjectObjectCursor<String, ImmutableOpenMap<String, MappingMetaData>> entry = iterator.next();
                for(ObjectObjectCursor<String, MappingMetaData> typeEntry : entry.value) {
                    File file = new File(PATH + File.separator + "_mapping_" + index + "_" + type + ".json");
                    OutputStream out = new FileOutputStream(file);
                    IOUtils.copy(new ByteArrayInputStream(typeEntry.value.source().string().getBytes()), out);
                    IOUtils.closeQuietly(out);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void exportDoc(Client client, RestRequest request, String token){
        String index = request.param("index");
        String type = request.param("type");
        OutputStream out = null;
        try {
            File file = new File(PATH + File.separator + "_doc_" + index + "_" + type + ".json");
            if(file.exists()){
                file.delete();
            }
            out = new FileOutputStream(file, true);
            CountRequest countRequest = new CountRequest(index);
            countRequest.types(type);
            long total = client.count(countRequest).actionGet().getCount();
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.searchType(SearchType.SCAN);
            searchRequest.scroll("1m");
            searchRequest.indices(index);
            searchRequest.types(type);
            SearchResponse response = client.search(searchRequest).get();
            do{
                response = client.prepareSearchScroll(response.getScrollId()).setScroll("1m").execute().get();
                for(SearchHit hit : response.getHits().getHits()){
                    Map<String, Object> source = hit.getSource();
                    byte[] bytes = (mapper.writeValueAsString(source) + "\r\n").getBytes();
                    IOUtils.copy(new ByteArrayInputStream(bytes), out);
                    System.out.println("exportDoc :" + token);
                    UpdateRequest updateRequest = new UpdateRequest("system", "export_process", token)
                            .script("ctx._source.finished += count").addScriptParam("count", 1)
                            .upsert(XContentFactory.jsonBuilder()
                                    .startObject()
                                    .field("token", token)
                                    .field("total", total)
                                    .field("finished", 1)
                                    .field("startAt", new Date())
                                    .field("condition", "")
                                    .endObject());
                    client.update(updateRequest);
                }
            }while(response.getHits().getHits().length > 0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            IOUtils.closeQuietly(out);
        }
    }



    class ExportService implements Runnable{
        private RestRequest request;
        private RestChannel channel;
        private Client client;
        private String token;

        public ExportService(RestRequest request, RestChannel channel, Client client, String token) {
            this.request = request;
            this.channel = channel;
            this.client = client;
            this.token = token;
        }

        public void run() {
            ExportAction.exportIndexMappings(client, request);
            ExportAction.exportDoc(client, request, token);
        }
    }



}
