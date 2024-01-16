package org.tron.streaming;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.parameter.CommonParameter;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.exception.StreamingMessageValidateException;
import org.tron.protos.streaming.TronMessage;
import org.tron.streaming.messages.ProtobufMessage;
@Slf4j(topic = "streaming")
public class StreamingProcessor {
    private KafkaMessageBroker kafkaBroker;
    public StreamingProcessor() {
        this.kafkaBroker = new KafkaMessageBroker();
    }

    public void process(BlockCapsule newBlock) throws StreamingMessageValidateException {
        Stopwatch timer = Stopwatch.createStarted();

        BlockMessageCreator blockMessageCreator = new BlockMessageCreator(newBlock);
        blockMessageCreator.create();
        TronMessage.BlockMessage blockMessage = blockMessageCreator.getBlockMessage();

        BlockMessageValidator validator = new BlockMessageValidator(blockMessage);
        validator.validate();

        BlockMessageDescriptor blockMsgDescriptor = new BlockMessageDescriptor();
        blockMsgDescriptor.setBlockHash(newBlock.getBlockId().toString());
        blockMsgDescriptor.setBlockNumber(newBlock.getNum());
        blockMsgDescriptor.setParentHash(newBlock.getParentHash().toString());
        blockMsgDescriptor.setParentNumber(newBlock.getParentBlockId().getNum());
        blockMsgDescriptor.setChainId(CommonParameter.getInstance().getStreamingConfig().getChainId());

        ProtobufMessage protobufMessage = new ProtobufMessage(blockMsgDescriptor, blockMessage.toByteArray());
        protobufMessage.sign();
        protobufMessage.store();

        kafkaBroker.send("tron.blocks.test", protobufMessage);

        logger.info(String.format("Streaming processing took %s, Num: %d", timer.stop(), newBlock.getNum()));
    }

    public void close() {
        kafkaBroker.close();

        logger.info("StreamingProcessor closed");
    }
}
