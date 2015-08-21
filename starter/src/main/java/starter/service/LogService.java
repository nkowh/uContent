package starter.service;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import starter.RequestContext;
import starter.rest.Json;

import java.io.IOException;
import java.util.Map;

@Service
public class LogService {
    @Autowired
    private RequestContext context;

    private final String LOG_TYPE_NAME = "logInfo";

    public XContentBuilder query(Json query, int from, int size) throws IOException {
        SearchRequestBuilder searchRequestBuilder = context.getClient()
                .prepareSearch(context.getIndex())
                .setTypes(LOG_TYPE_NAME)
                .setFrom(from)
                .setSize(size)
                //.addSort(sort, sord.equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC)
                ;
        if (query != null && !query.isEmpty()) {
            searchRequestBuilder.setQuery(query);
        }
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

        XContentBuilder builder = XContentFactory.jsonBuilder();
        SearchHits searchHits = searchResponse.getHits();
        builder.startObject()
                .field("total", searchHits.totalHits());
        builder.startArray("logInfos");
        for (SearchHit searchHitFields : searchHits) {
            Map<String, Object> source = searchHitFields.getSource();
            builder.startObject()
                    .field("_id", searchHitFields.getId())
                    .field("userName", source.get("userName"))
                    .field("ipAddress", source.get("ipAddress"))
                    .startObject("timeInfo")
                    .field("start", source.get("timeInfo.start"))
                    .field("start_format", source.get("timeInfo.start_format"))
                    .field("end", source.get("timeInfo.end"))
                    .field("end_format", source.get("timeInfo.end_format"))
                    .field("consume", source.get("timeInfo.consume"))
                    .field("consume_format", source.get("timeInfo.consume_format"))
                    .endObject()
                    .startObject("actionInfo")
                    .field("url", source.get("actionInfo.url"))
                    .field("className", source.get("actionInfo.className"))
                    .field("methodName", source.get("actionInfo.methodName"))
                    .field("paramNames", source.get("actionInfo.paramNames"))
                    .endObject()
                    .field("resultInfo", source.get("resultInfo"))
                    .startObject("exceptionInfo")
                    .field("msg", source.get("exceptionInfo.msg"))
                    .field("statusCode", source.get("exceptionInfo.statusCode"))
                    .field("stackTrace", source.get("exceptionInfo.stackTrace"))
                    .endObject()
                    .field("logDate", source.get("logDate"))
                    .endObject();
        }
        builder.endArray();
        builder.endObject();

        System.out.println(builder.string());
        return builder;
    }


    public XContentBuilder createLog(XContentBuilder builder_in) throws IOException {
        Client client = context.getClient();
        IndexResponse indexResponse = client.prepareIndex(context.getIndex(), LOG_TYPE_NAME)
                .setSource(builder_in).execute().actionGet();

        XContentBuilder builder_out = XContentFactory.jsonBuilder();
        builder_out.startObject()
                .field("_index", indexResponse.getIndex())
                .field("_type", indexResponse.getType())
                .field("_id", indexResponse.getId())
                .field("_version", indexResponse.getVersion())
                .field("created", indexResponse.isCreated())
                .endObject();

        return builder_out;
    }

}
