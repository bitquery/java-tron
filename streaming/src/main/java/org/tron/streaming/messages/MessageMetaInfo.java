package org.tron.streaming.messages;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.tron.streaming.BlockMessageDescriptor;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class MessageMetaInfo {
    private MessageAuthenticator authenticator;
    private BlockMessageDescriptor descriptor;
    private String uri;
    private int size;
    private List<String> servers;
}
