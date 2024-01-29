package org.tron.streaming;

import com.google.protobuf.ByteString;
import evm_messages.BlockMessageOuterClass.Contract;
import evm_messages.BlockMessageOuterClass.Opcode;
import evm_messages.BlockMessageOuterClass.AddressCode;
import evm_messages.BlockMessageOuterClass.Trace;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.runtime.vm.DataWord;
import org.tron.core.capsule.EvmTraceCapsuleI;

import java.util.List;

@Slf4j(topic = "capsule")
public class NoEvmTraceCapsule implements EvmTraceCapsuleI {
    private Trace.Builder traceBuilder;

    public NoEvmTraceCapsule() {
        this.traceBuilder = Trace.newBuilder();
    }


    @Override
    public void setCaptureStart(ByteString from, ByteString to, long energy, AddressCode addressCodeTo) {
        return;
    }

    @Override
    public void setCaptureEnd(long energyUsed, RuntimeException error) {
        return;
    }
    @Override
    public void setCaptureEnter(ByteString from, ByteString to, ByteString data, long energy, ByteString value, Opcode opcode, AddressCode addressCodeTo) {
        return;
    }

    // CaptureExit should always be called after CaptureEnter
    // Otherwise nothing is changed
    @Override
    public void setCaptureExit(long energyUsed, RuntimeException error) {
        return;
    }
    @Override
    public void addCaptureState(int pc, Opcode opcode, long energy, long cost, int depth, RuntimeException error) {
        return;
    }
    @Override
    public void addLogToCaptureState(byte[] address, byte[] data, AddressCode addressCode, List<DataWord> topicsData) {
        return;
    }
    @Override
    public void addStorageToCaptureState(byte[] address, byte[] loc, byte[] value) {
        return;
    }
    @Override
    public void setCaptureFault(int pc, Opcode opcode, long energy, long cost, int depth, RuntimeException error, List<ByteString> stack, Contract contract, byte[] memory) {
        return;
    }
    @Override
    public AddressCode addressCode(byte[] code) {
        return AddressCode.newBuilder().build();
    }

    @Override
    public Opcode opcode(int code, String name) {
        return Opcode.newBuilder().build();
    }

    @Override
    public Contract contract(byte[] callerAddress, byte[] address, byte[] codeAddr, byte[] value) {
        return Contract.newBuilder().build();
    }

    @Override
    public byte[] getData() { return this.traceBuilder.build().toByteArray();}

    @Override
    public Trace getInstance() { return this.traceBuilder.build();}
}
