package com.nikoyo.odata;


import com.nikoyo.odata.file.FileSystem;
import com.nikoyo.odata.file.FileSystemFactory;
import com.nikoyo.odata.file.FsConfig;
import org.apache.catalina.servlets.DefaultServlet;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.ResourceServlet;

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

//    @Bean
//    public ServletRegistrationBean servletRegistrationAssets(AssetsServlet servlet) {
//        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(servlet, "/assets/*");
//        servletRegistrationBean.addInitParameter("dir","assets");
//        servletRegistrationBean.setOrder(1);
//        return servletRegistrationBean;
//    }

    @Bean
    public ServletRegistrationBean servletRegistrationWeb(AssetsServlet servlet) {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(servlet, "/web/*");
        servletRegistrationBean.addInitParameter("dir","web");
        servletRegistrationBean.setOrder(1);
        return servletRegistrationBean;
    }

    @Bean
    public ServletRegistrationBean servletRegistrationFavicon(FaviconServlet servlet) {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(servlet, "/favicon.ico");
        servletRegistrationBean.setOrder(2);
        return servletRegistrationBean;
    }

    @Bean
    public ServletRegistrationBean servletRegistrationError(ErrorServlet servlet) {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(servlet, "/error");
        servletRegistrationBean.setOrder(3);
        return servletRegistrationBean;
    }

    @Bean
    public ServletRegistrationBean servletRegistrationMetadata(MetadataServlet servlet) {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(servlet, "/metadata");
        servletRegistrationBean.setOrder(4);
        return servletRegistrationBean;
    }

    @Bean
    public ServletRegistrationBean servletRegistrationOData(ODataServlet servlet) {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(servlet, "/*");
        servletRegistrationBean.setOrder(Integer.MAX_VALUE);
        return servletRegistrationBean;
    }

    @Bean
    public FileSystem fileSystem(Client client, FsConfig fsConfig) {
        FileSystemFactory fileSystemFactory = new FileSystemFactory(client);
        return fileSystemFactory.newFileSystem(fsConfig);
    }

    @Bean
    public Client client(EsConfig esConfig) {
//        Node node = org.elasticsearch.node.NodeBuilder.nodeBuilder().node();
//        return node.client();
        Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", esConfig.getCluster()).build();
        TransportClient client = new TransportClient(settings);
        for (String host : esConfig.getHosts()) {
            String[] parts = host.split(":");
            client.addTransportAddress(new InetSocketTransportAddress(parts[0], Integer.parseInt(parts[1])));
        }
        return client;
    }


}
