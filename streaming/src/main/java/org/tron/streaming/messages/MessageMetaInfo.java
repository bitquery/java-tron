package org.tron.streaming.messages;

import lombok.Data;
import org.tron.streaming.BlockMessageDescriptor;

import java.util.List;

@Data
public class MessageMetaInfo {
    private MessageAuthenticator authenticator;
    private BlockMessageDescriptor descriptor;
    private String uri;
    private int size;
    private List<String> servers;
}
