package starter;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import starter.service.fs.FileSystem;
import starter.service.fs.FileSystemFactory;
import starter.service.fs.FsConfig;

@SpringBootApplication
public class StarterApplication {

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
}
