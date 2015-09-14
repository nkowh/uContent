package starter;

import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestContext {

    @Autowired
    private Client client;

    @Autowired
    private EsConfig esConfig;

    @Autowired
    HttpServletRequest request;

    @Autowired
    HttpServletResponse response;

    public String getIndex() {
        return esConfig.getAlias();
    }

    public Client getClient() {
        return client;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public String getUserName() {
        return this.request.getUserPrincipal().getName();
    }
}
