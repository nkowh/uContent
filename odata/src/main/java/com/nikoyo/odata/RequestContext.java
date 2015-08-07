package com.nikoyo.odata;


import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.server.api.ServiceMetadata;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestContext {

    private HttpServletRequest httpServletRequest;

    private String repositoryId;

    private DefaultEdmProvider provider;

    private ServiceMetadata serviceMetadata;

    @Autowired
    private Client client;

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    public ServiceMetadata getServiceMetadata() {
        return serviceMetadata;
    }

    public void setServiceMetadata(ServiceMetadata serviceMetadata) {
        this.serviceMetadata = serviceMetadata;
    }

    public CsdlAbstractEdmProvider getProvider() {
        if (StringUtils.isBlank(repositoryId)) throw new RuntimeException("repositoryId must not be empty");
        if (provider != null) return provider;
        try {
            GetResponse response = client.prepareGet(repositoryId, "$system", "metadata").execute().actionGet();
            String metadata = response.getSource().get("value").toString();
            provider = new DefaultEdmProvider(metadata);
            return provider;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


}
