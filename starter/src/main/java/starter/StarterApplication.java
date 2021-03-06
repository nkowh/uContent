package starter;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;
import starter.service.fs.FileSystem;
import starter.service.fs.FileSystemFactory;
import starter.service.fs.FsConfig;

@SpringBootApplication
@EnableScheduling
public class StarterApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(StarterApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(StarterApplication.class, args);
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


    @Bean
    public MultipartResolver MultipartResolverInstance() {
        return new NkoMultipartResolver();
    }

}
