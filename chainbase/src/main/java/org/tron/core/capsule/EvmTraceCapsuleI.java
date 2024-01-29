package org.tron.core.capsule;

import com.google.protobuf.ByteString;
import evm_messages.BlockMessageOuterClass;
import org.tron.common.runtime.vm.DataWord;

import java.util.List;

public interface EvmTraceCapsuleI {
    public void setCaptureStart(ByteString from, ByteString to, long energy, BlockMessageOuterClass.AddressCode addressCodeTo);
    public void setCaptureEnd(long energyUsed, RuntimeException error);
    public void setCaptureEnter(ByteString from, ByteString to, ByteString data, long energy, ByteString value, BlockMessageOuterClass.Opcode opcode, BlockMessageOuterClass.AddressCode addressCodeTo);
    public void setCaptureExit(long energyUsed, RuntimeException error);
    public void addCaptureState(int pc, BlockMessageOuterClass.Opcode opcode, long energy, long cost, int depth, RuntimeException error);
    public void addLogToCaptureState(byte[] address, byte[] data, BlockMessageOuterClass.AddressCode addressCode, List<DataWord> topicsData);
    public void addStorageToCaptureState(byte[] address, byte[] loc, byte[] value);
    public void setCaptureFault(int pc, BlockMessageOuterClass.Opcode opcode, long energy, long cost, int depth, RuntimeException error, List<ByteString> stack, BlockMessageOuterClass.Contract contract, byte[] memory);
    public BlockMessageOuterClass.AddressCode addressCode(byte[] code);

    public BlockMessageOuterClass.Opcode opcode(int code, String name);

    public BlockMessageOuterClass.Contract contract(byte[] callerAddress, byte[] address, byte[] codeAddr, byte[] value);

    public byte[] getData();

    public BlockMessageOuterClass.Trace getInstance();
}
