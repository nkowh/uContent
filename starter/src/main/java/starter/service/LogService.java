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
                    .field("start", ((Map<String, Object>) source.get("timeInfo")).get("start"))
                    .field("start_format", ((Map<String, Object>) source.get("timeInfo")).get("start_format"))
                    .field("end", ((Map<String, Object>) source.get("timeInfo")).get("end"))
                    .field("end_format", ((Map<String, Object>) source.get("timeInfo")).get("end_format"))
                    .field("consume", ((Map<String, Object>) source.get("timeInfo")).get("consume"))
                    .field("consume_format", ((Map<String, Object>) source.get("timeInfo")).get("consume_format"))
                    .endObject()
                    .startObject("actionInfo")
                    .field("url", ((Map<String, Object>) source.get("actionInfo")).get("url"))
                    .field("className", ((Map<String, Object>) source.get("actionInfo")).get("className"))
                    .field("methodName", ((Map<String, Object>) source.get("actionInfo")).get("methodName"))
                    .field("paramNames", ((Map<String, Object>) source.get("actionInfo")).get("paramNames"))
                    .endObject()
                    .field("resultInfo", source.get("resultInfo"))
                    .startObject("exceptionInfo")
                    .field("ex_msg", ((Map<String, Object>) source.get("exceptionInfo")).get("ex_msg"))
                    .field("ex_statusCode", ((Map<String, Object>) source.get("exceptionInfo")).get("ex_statusCode"))
                    .field("ex_stackTrace", ((Map<String, Object>) source.get("exceptionInfo")).get("ex_stackTrace"))
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
