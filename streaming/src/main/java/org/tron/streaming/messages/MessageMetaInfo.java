package org.tron.streaming.messages;

import lombok.Data;
import org.tron.streaming.BlockMessageDescriptor;

@Data
public class MessageMetaInfo {
    private BlockMessageDescriptor descriptor;
    private String uri;
    private int size;
}
