package org.tron.streaming.messages;

import com.google.common.primitives.Bytes;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.jpountz.lz4.LZ4FrameOutputStream;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.Hash;
import org.tron.common.parameter.CommonParameter;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.JsonUtil;
import org.tron.core.config.args.StreamingConfig;
import org.tron.streaming.BlockMessageDescriptor;
import org.tron.streaming.EllipticSigner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;

@Slf4j(topic = "streaming")
public class ProtobufMessage {
    @Getter
    public MessageMetaInfo meta = new MessageMetaInfo();

    private byte[] body;

    private StreamingConfig streamingConfig;

    private EllipticSigner signer;

    public ProtobufMessage(BlockMessageDescriptor descriptor, byte[] body) {
        getMeta().setDescriptor(descriptor);
        getMeta().setAuthenticator(new MessageAuthenticator());

        this.body = body;
        this.streamingConfig = CommonParameter.getInstance().getStreamingConfig();
        this.signer = new EllipticSigner();

        getMeta().setSize(body.length);
        getMeta().setServers(this.streamingConfig.getFileStorageUrls());
    }

    public void sign() {
        prepareAuthenticator();

        byte[] message = ByteArray.fromHexString(getMeta().getAuthenticator().getId());
        ECKey.ECDSASignature signature = this.signer.sign(message);

        getMeta().getAuthenticator().setSigner(this.signer.getAddress());
        getMeta().getAuthenticator().setSignature(ByteArray.toHexString(signature.toByteArray()));
    }

    public void store() {
        String fullPath = getBlockPath();
        writeMessageToFileWithCompression(fullPath);

        logger.info("Stored message, Path: {}, Length: {}", fullPath, getMeta().getSize());

        getMeta().setUri(fullPath);
    }

    private void prepareAuthenticator() {
        logger.info("Preparing authenticator for block protobuf message");

        byte[] bodyHash = getBodyHash();

        Instant insTime = Instant.now();
        String time = String.format("%d%d", insTime.getEpochSecond(), insTime.getNano());
        byte[] timeBytes = ByteArray.fromLong(Long.parseLong(time));

        byte[] descriptor = ByteArray.fromObject(JsonUtil.obj2Json(getMeta().getDescriptor()));

        byte[] idHash = Bytes.concat(bodyHash, timeBytes, descriptor);
        idHash = Hash.sha3(idHash);

        getMeta().getAuthenticator().setBodyHash(ByteArray.toHexString(bodyHash));
        getMeta().getAuthenticator().setTime(time);
        getMeta().getAuthenticator().setId(ByteArray.toHexString(idHash));
    }

    private String getBlockPath() {
        String directoryPath = getDirectoryName();
        String fileName = String.format("%s_%s_%s%s",
                getPaddedBlockNumber(getMeta().getDescriptor().getBlockNumber()),
                getMeta().getDescriptor().getBlockHash(),
                ByteArray.toHexString(getBodyHash()),
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

    private byte[] getBodyHash() {
        return Hash.sha3(body);
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
