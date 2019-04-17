package org.tron.common.zksnark;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.crypto.zksnark.ZksnarkUtils;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Sha256Update;
import org.tron.common.zksnark.zen.Librustzcash;
import org.tron.core.capsule.ProtoCapsule;
import org.tron.protos.Contract.PedersenHash;


@Slf4j
public class PedersenHashCapsule implements ProtoCapsule<PedersenHash> {

  private PedersenHash pedersenHash;

  public PedersenHashCapsule() {
    this.pedersenHash = PedersenHash.getDefaultInstance();
  }

  public PedersenHashCapsule(final PedersenHash pedersenHash) {
    this.pedersenHash = pedersenHash;
  }

  public PedersenHashCapsule(final byte[] data) {
    try {
      this.pedersenHash = PedersenHash.parseFrom(data);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage(), e);
    }
  }

  public ByteString getContent() {
    return this.pedersenHash.getContent();
  }

  public void setContent(ByteString content) {
    this.pedersenHash = PedersenHash.newBuilder().setContent(content).build();
  }

  @Override
  public byte[] getData() {
    return this.pedersenHash.toByteArray();
  }

  @Override
  public PedersenHash getInstance() {
    return this.pedersenHash;
  }

  public static PedersenHashCapsule combine(final PedersenHash a, final PedersenHash b, int depth) {
    byte[] res = new byte[32];

    Librustzcash.librustzcashMerkleHash(depth, a.getContent().toByteArray(), b.getContent().toByteArray(), res);

    PedersenHashCapsule pedersenHashCapsule = new PedersenHashCapsule();
    pedersenHashCapsule.setContent(ByteString.copyFrom(res));

    return pedersenHashCapsule;
  }

  public static PedersenHashCapsule uncommitted() {
    byte[] res = new byte[32];

    Librustzcash.librustzcash_tree_uncommitted(res);

    PedersenHashCapsule compressCapsule = new PedersenHashCapsule();
    compressCapsule.setContent(ByteString.copyFrom(res));

    return compressCapsule;
  }

  public boolean isPresent() {
    return !pedersenHash.getContent().isEmpty();
  }

  public static void main(String[] args) {
    byte[] a =
        ByteArray.fromHexString("0000000000000000000000000000000000000000000000000000000000000000");
    byte[] b =
        ByteArray.fromHexString("0000000000000000000000000000000000000000000000000000000000000000");

    PedersenHash sa = PedersenHash.newBuilder().setContent(ByteString.copyFrom(a)).build();
    PedersenHash sb = PedersenHash.newBuilder().setContent(ByteString.copyFrom(b)).build();
    PedersenHash result = combine(sa, sb, 0).getInstance();
    // 5bf43b5736c19b714d1f462c9d22ba3492c36e3d9bbd7ca24d94b440550aa561
    System.out.println(ByteArray.toHexString(result.getContent().toByteArray()));
  }
}
