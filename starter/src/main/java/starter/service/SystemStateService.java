package starter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.WriteConsistencyLevel;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import starter.rest.Json;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Component
public class SystemStateService {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");


//    @Autowired
//    private Client client;
//
//    @Scheduled(fixedRate = 3000)
//    public void reportCurrentTime() throws IOException {
//        ObjectMapper mapper = new ObjectMapper();
//        NodesInfoResponse response = client.admin().cluster().prepareNodesInfo()
//                .setJvm(true).setOs(true).execute().actionGet();
//        XContentBuilder xContent = response.toXContent(JsonXContent.contentBuilder().startObject(), MonitorService.EMPTY_PARAMS).endObject();
//        XContentBuilder resultBuilder = JsonXContent.contentBuilder();
//        resultBuilder.startObject();
//        Json json = mapper.readValue(xContent.bytes().array(), Json.class);
//        Map<String, Object> nodes = (Map<String, Object>) json.get("nodes");
//        for (String nodeName : nodes.keySet()) {
//            Map<String, Object> node = (Map<String, Object>) nodes.get(nodeName);
//            resultBuilder.field("name",node.get("name"));
//            resultBuilder.field("ip",node.get("ip"));
//            Map<String, Object> os = (Map<String, Object>) node.get("os");
//
//        }
        //client.prepareIndex("$System","monitor").setOpType(IndexRequest.OpType.CREATE).setSource;
//                        System.out.println("The time is now " + dateFormat.format(new Date()));
//    }
}
