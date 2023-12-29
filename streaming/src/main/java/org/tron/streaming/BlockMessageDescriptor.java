package org.tron.streaming;

import lombok.Data;

@Data
public class BlockMessageDescriptor {
  private String blockHash;
  private long blockNumber;
  private String parentHash;
  private long parentNumber;
}
