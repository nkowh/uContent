package starter;

import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestContext {

    @Autowired
    private Client client;

    @Autowired
    private EsConfig esConfig;

    private String getIndex(){
        return esConfig.getIndex();
    }

    public Client getClient() {
        return client;
    }
}
