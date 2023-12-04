package org.tron.streaming;

import lombok.Getter;
import lombok.Setter;

public class BlockMessageDescriptor {
  @Getter
  @Setter
  private String blockHash;

  @Getter
  @Setter
  private long blockNumber;

  @Getter
  @Setter
  private String parentHash;

  @Getter
  @Setter
  private long parentNumber;
}
