package org.tron.streaming.messages;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.jpountz.lz4.LZ4FrameOutputStream;
import org.tron.common.crypto.Hash;
import org.tron.common.parameter.CommonParameter;
import org.tron.common.utils.ByteArray;
import org.tron.core.config.args.StreamingConfig;
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

    private StreamingConfig streamingConfig;

    public ProtobufMessage(BlockMessageDescriptor descriptor, byte[] body) {
        getMeta().setDescriptor(descriptor);

        this.body = body;
        this.streamingConfig = CommonParameter.getInstance().getStreamingConfig();
        getMeta().setSize(body.length);
        getMeta().setServers(this.streamingConfig.getFileStorageUrls());
    }

    public void storeMessage() {
        String fullPath = getBlockPath();

        getMeta().setUri(fullPath);

        writeMessageToFileWithCompression(fullPath);
    }

    private String getBlockPath() {
        String directoryPath = getDirectoryName();
        String fileName = String.format("%s_%s_%s%s",
                getPaddedBlockNumber(getMeta().getDescriptor().getBlockNumber()),
                getMeta().getDescriptor().getBlockHash(),
                getBodyHash(),
                streamingConfig.getPathGeneratorSuffix()
        );

        String fullPath = Paths.get(directoryPath, fileName).toString();

        return fullPath;
    }

    private String getPaddedBlockNumber(long number) {
        String template = "%%%s%dd";
        String formattedBlockNumber = String.format(
                template,
                streamingConfig.getPathGeneratorSpacer(),
                streamingConfig.getPathGeneratorBlockNumberPadding()
        );

        String blockNumber = String.format(formattedBlockNumber, number);

        return blockNumber;
    }

    private String getDirectoryName() {
        long blockNumber = getMeta().getDescriptor().getBlockNumber();
        int bucketSize = streamingConfig.getPathGeneratorBucketSize();

        String blockDir = getPaddedBlockNumber(bucketSize * (blockNumber / bucketSize));
        String dirName = Paths.get(streamingConfig.getFileStorageRoot(), blockDir).toString();

        return dirName;
    }

    private String getBodyHash() {
        return ByteArray.toHexString(Hash.sha3(body));
    }

    private void writeMessageToFileWithCompression(String fullPath) {
        new File(fullPath).getParentFile().mkdirs();

        try {
            LZ4FrameOutputStream outStream = new LZ4FrameOutputStream(new FileOutputStream(new File(fullPath)));

            logger.info("Stored message path: {}, length: {}", fullPath, getMeta().getSize());

            outStream.write(body);
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
