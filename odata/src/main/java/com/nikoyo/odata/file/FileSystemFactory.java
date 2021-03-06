package com.nikoyo.odata.file;


import com.nikoyo.odata.file.local.LocalFileSystem;
import com.nikoyo.odata.file.losf.BlockFileSystem;
import org.elasticsearch.client.Client;

public class FileSystemFactory {

    private final Client client;

    public FileSystemFactory(Client client) {
        this.client = client;
    }

    public FileSystem newFileSystem(FsConfig config) {
        // String fs = Configuration.UC_PROPERTIES.getProperty("fs", "local");
        String fs = config.getType();
        if ("local".equalsIgnoreCase(fs)) {
            return new LocalFileSystem(config.getRoot());
        } else if ("losf".equalsIgnoreCase(fs)) {
            return new BlockFileSystem(client.settings().get("node.name"), config.getRoot(), config.getBlocksize());
        } else if ("s3".equalsIgnoreCase(fs)) {
            //return new S3FileSystem(config.getAccessKey(), config.getSecretKey(), config.getBucketName(), config.getEndpoint());
        }
        return null;

    }

}
