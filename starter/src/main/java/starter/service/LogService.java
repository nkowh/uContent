package starter.service;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import starter.RequestContext;
import starter.rest.Json;

import java.io.IOException;

@Service
public class LogService {
    @Autowired
    private RequestContext context;

    private final String LOG_TYPE_NAME = "logInfo";

    public XContentBuilder query(String type, Json parse, int start, int limit, String sort) {
        return null;
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
