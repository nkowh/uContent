package starter.service.fs.local;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import starter.service.fs.FileSystem;

import java.io.File;
import java.io.IOException;
import java.util.zip.CRC32;

public class LocalFileSystem implements FileSystem {

    private final String root;

    public LocalFileSystem(String root) {
        this.root = root;
        for (int i = 0; i < 256; i++) {
            String level1 = FilenameUtils.concat(root, String.format("%02X", i));
            for (int j = 0; j < 256; j++) {
                File level2 = new File(FilenameUtils.concat(level1, String.format("%02X", j)));
                boolean res = level2.mkdirs();
                if (!res) return;
            }
        }
    }

    public String write(byte[] bytes) throws IOException {
        File dest = buildFilePath(bytes);
        if (!dest.exists()) FileUtils.writeByteArrayToFile(dest, bytes);
        return dest.getAbsolutePath();
    }

    private File buildFilePath(byte[] bytes) throws IOException {
        String crc32 = checksumCRC32(bytes);
        String first2 = crc32.substring(0, 2);
        String first4 = crc32.substring(2, 4);
        return new File(FilenameUtils.concat(FilenameUtils.concat(FilenameUtils.concat(root, first2), first4), crc32));
    }

    public byte[] read(String location) {
        try {
            return FileUtils.readFileToByteArray(new File(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public static String checksumCRC32(byte[] buffer) throws IOException {
        CRC32 crc = new CRC32();
        crc.update(buffer);
        return String.format("%08X", crc.getValue());
    }
}
