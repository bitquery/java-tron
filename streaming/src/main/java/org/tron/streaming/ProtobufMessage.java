package org.tron.streaming;

import net.jpountz.lz4.LZ4FrameOutputStream;
import org.tron.common.crypto.Hash;
import org.tron.common.utils.ByteArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class ProtobufMessage {
    private BlockMessageDescriptor blockMsgDescriptor;
    private byte[] protobufMsg;

    public ProtobufMessage(BlockMessageDescriptor blockMsgDescriptor, byte[] protobufMsg) {
        this.blockMsgDescriptor = blockMsgDescriptor;
        this.protobufMsg = protobufMsg;
    }

    public void storeMessage(String streamingDirectory) {
        String directoryPath = generateDirectoryName(streamingDirectory);
        String fileName = String.format("%d_%s_%s%s",
                blockMsgDescriptor.getBlockNumber(),
                blockMsgDescriptor.getBlockHash(),
                generateBodyHash(),
                ".lz4"
        );

        String fullPath = Paths.get(directoryPath, fileName).toString();

        writeMessageToFileWithCompression(fullPath);
    }

    private String generateDirectoryName(String streamingDirectory) {
        String blockFolder =  String.valueOf(1000 * (blockMsgDescriptor.getBlockNumber() / 1000));
        String directoryPath = Paths.get(streamingDirectory, blockFolder).toString();

        return directoryPath;
    }

    private String generateBodyHash() {
        return ByteArray.toHexString(Hash.sha3(protobufMsg));
    }

    private void writeMessageToFileWithCompression(String fullPath) {
        new File(fullPath).getParentFile().mkdirs();

        try {
            LZ4FrameOutputStream outStream = new LZ4FrameOutputStream(new FileOutputStream(new File(fullPath)));
            outStream.write(protobufMsg);
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
