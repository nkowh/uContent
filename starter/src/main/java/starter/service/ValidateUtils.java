package starter.service;

import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.hppc.cursors.ObjectObjectCursor;
import org.springframework.stereotype.Service;
import starter.RequestContext;
import starter.rest.Json;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by Administrator on 2015/8/13.
 */
@Service
public class ValidateUtils {

    public void validateDoc(RequestContext context, String type, Json parse) throws IOException {
        GetMappingsResponse getMappingsResponse = context.getClient().admin().indices().prepareGetMappings(context.getIndex()).setTypes(type).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.mappings();
        if (!mappings.isEmpty()) {
            Iterator<ObjectObjectCursor<String, ImmutableOpenMap<String, MappingMetaData>>> iterator = mappings.iterator();
            while(iterator.hasNext()){
                ObjectObjectCursor<String, ImmutableOpenMap<String, MappingMetaData>> entry = iterator.next();
                for(ObjectObjectCursor<String, MappingMetaData> typeEntry : entry.value){
                    System.out.println(typeEntry.key);
                    System.out.println(typeEntry.value.sourceAsMap());
                }

            }
        }


    }
}
