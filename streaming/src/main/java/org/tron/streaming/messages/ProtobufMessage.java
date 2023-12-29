package org.tron.streaming.messages;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.jpountz.lz4.LZ4FrameOutputStream;
import org.tron.common.crypto.Hash;
import org.tron.common.utils.ByteArray;
import org.tron.streaming.BlockMessageDescriptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

@Slf4j(topic = "streaming")
public class ProtobufMessage {
    @Getter
    public MessageMetaInfo meta = new MessageMetaInfo();

    private byte[] body;

    public ProtobufMessage(BlockMessageDescriptor descriptor, byte[] body) {
        this.getMeta().setDescriptor(descriptor);
        this.getMeta().setSize(body.length);

        this.body = body;
    }

    public void storeMessage(String streamingDirectory) {
        String directoryPath = getDirectoryName(streamingDirectory);
        String fileName = String.format("%d_%s_%s%s",
                this.getMeta().getDescriptor().getBlockNumber(),
                this.getMeta().getDescriptor().getBlockHash(),
                getBodyHash(),
                ".lz4"
        );

        String fullPath = Paths.get(directoryPath, fileName).toString();

        this.getMeta().setUri(fullPath);

        writeMessageToFileWithCompression(fullPath);
    }

    private String getDirectoryName(String streamingDirectory) {
        String blockFolder =  String.valueOf(1000 * (this.getMeta().getDescriptor().getBlockNumber() / 1000));
        String directoryPath = Paths.get(streamingDirectory, blockFolder).toString();

        return directoryPath;
    }

    private String getBodyHash() {
        return ByteArray.toHexString(Hash.sha3(body));
    }

    private void writeMessageToFileWithCompression(String fullPath) {
        new File(fullPath).getParentFile().mkdirs();

        try {
            LZ4FrameOutputStream outStream = new LZ4FrameOutputStream(new FileOutputStream(new File(fullPath)));
            outStream.write(body);
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
