package org.tron.streaming.messages;

import lombok.Data;

@Data
public class MessageAuthenticator {
    private String bodyHash;
    private String time;
    private String id;
    private String signer;
    private String signature;
}
