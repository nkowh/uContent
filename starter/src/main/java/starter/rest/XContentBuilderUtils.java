package starter.rest;


import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/8/17.
 */
public class XContentBuilderUtils {


    public static XContentBuilder toXContentBuilder(List<Map<String, Object>> list) throws IOException {
        XContentBuilder builder = JsonXContent.contentBuilder();
        if (list != null) {
            builder.startObject().startArray("_acl");
            for(Map<String, Object> map : list){
                builder.startObject();
                Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
                while(it.hasNext()){
                    Map.Entry<String, Object> entry = it.next();
                    builder.field(entry.getKey(), entry.getValue());
                }
                builder.endObject();
            }
            builder.endArray().endObject();
        }
        return builder;
    }
}
