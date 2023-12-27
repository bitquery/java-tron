package org.tron.core.capsule;

import com.google.protobuf.ByteString;
import evm_messages.BlockMessageOuterClass.CaptureExit;
import evm_messages.BlockMessageOuterClass.CaptureStateHeader;
import evm_messages.BlockMessageOuterClass.CaptureState;
import evm_messages.BlockMessageOuterClass.Call;
import evm_messages.BlockMessageOuterClass.Opcode;
import evm_messages.BlockMessageOuterClass.CaptureEnter;
import evm_messages.BlockMessageOuterClass.AddressCode;
import evm_messages.BlockMessageOuterClass.CaptureEnd;
import evm_messages.BlockMessageOuterClass.CaptureStart;
import evm_messages.BlockMessageOuterClass.Trace;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j(topic = "capsule")
public class EvmTraceCapsule implements ProtoCapsule<Trace> {
    private Trace.Builder traceBuilder;
    private Call call;

    private int enterIndex;
    private int exitIndex;

    public EvmTraceCapsule() {
        this.traceBuilder = Trace.newBuilder();
        this.call = null;

        this.enterIndex = 0;
        this.exitIndex = 0;
    }

    public void setCaptureStart(ByteString from, ByteString to, boolean create, ByteString data, long energy, ByteString value, AddressCode addressCodeTo) {
        CaptureStart captureStart = CaptureStart.newBuilder()
                .setFrom(from)
                .setTo(to)
                .setCreate(create)
                .setInput(data)
                .setGas(energy)
                .setValue(value)
                .setToCode(addressCodeTo)
                .build();

        this.traceBuilder.setCaptureStart(captureStart);
    }

    public void setCaptureEnd(long energyUsed, RuntimeException error) {
        CaptureEnd captureEnd = CaptureEnd.newBuilder()
                // .setOutput()
                .setGasUsed(energyUsed)
                .setError(getErrorString(error))
                .build();

        this.traceBuilder.setCaptureEnd(captureEnd);
    }

    public void setCaptureEnter(ByteString from, ByteString to, ByteString data, long energy, ByteString value, Opcode opcode, AddressCode addressCodeTo) {
        this.enterIndex += 1;

        CaptureEnter captureEnter = CaptureEnter.newBuilder()
                .setOpcode(opcode)
                .setFrom(from)
                .setTo(to)
                .setInput(data)
                .setGas(energy)
                .setValue(value)
                .setToCode(addressCodeTo)
                .build();

        int depth = 1;
        int callerIndex = -1;

        if (this.call != null) {
            depth = this.call.getDepth() + 1;
            callerIndex = this.call.getIndex();
        }

        this.call = Call.newBuilder()
                .setDepth(depth)
                .setCaptureEnter(captureEnter)
                .setCallerIndex(callerIndex)
                .setIndex(0)
                .setEnterIndex(this.enterIndex)
                .setExitIndex(this.exitIndex)
                .build();

        int callsCount = this.traceBuilder.getCallsCount();
        if (callsCount != 0) {
            this.call = this.call.toBuilder().setIndex(callsCount).build();
        }

        this.traceBuilder.addCalls(this.call);
    }

    // CaptureExit should always be called after CaptureEnter
    // Otherwise nothing is changed
    public void setCaptureExit(long energyUsed, RuntimeException error) {
        this.exitIndex += 1;

        CaptureExit captureExit = CaptureExit.newBuilder()
                // .setOutput(output)
                .setGasUsed(energyUsed)
                .setError(getErrorString(error))
                .build();

        if (this.call == null) {
            return;
        }

        // Add captureExit to already existed call record.
        Call callWithExit = this.call.toBuilder().setCaptureExit(captureExit).build();
        this.traceBuilder.setCalls(this.call.getIndex(), callWithExit);

        int callerIndex = this.call.getCallerIndex();
        if (callerIndex >= 0) {
            this.call = this.traceBuilder.getCalls(callerIndex);
        } else {
            this.call = null;
        }
    }

    public void addCaptureState(int pc, Opcode opcode, long energy, long cost, int depth, RuntimeException error) {
        if (skipOpcode(opcode)) {
            return;
        }

        CaptureStateHeader captureStateHeader = CaptureStateHeader.newBuilder()
                .setPc(pc)
                .setOpcode(opcode)
                .setGas(energy)
                .setCost(cost)
                .setDepth(depth)
                .setError(getErrorString(error))
                .setEnterIndex(this.enterIndex)
                .setExitIndex(this.exitIndex)
                .build();

        CaptureState captureState = CaptureState.newBuilder()
                .setCaptureStateHeader(captureStateHeader)
//                .setLog()
//                .setStore()
                .build();


        this.traceBuilder.addCaptureStates(captureState);
    }

    public AddressCode addressCode(byte[] code) {
        ByteString hash =  ByteString.copyFrom(code);
        int size = code.length;
        AddressCode addressCode = AddressCode.newBuilder()
                .setHash(hash)
                .setSize(size)
                .build();

        return addressCode;
    }

    public Opcode opcode(int code, String name) {
        Opcode opcode = Opcode.newBuilder()
                .setCode(code)
                .setName(name)
                .build();

        return opcode;
    }

    private boolean skipOpcode(Opcode opcode) {
        int code = opcode.getCode();
        String name = opcode.getName();

        List<String> skippedNames = Arrays.asList("POP", "JUMP", "JUMPI", "JUMPDEST", "MSTORE", "MSTORE8");
        if (skippedNames.contains(name)) {
            return true;
        }

        // Arithmetic operations
        if(code >= 1 && code <= 11){
            return true;
        }

        // Bitwise, comparison and cryptographic operations
        if(code >= 16 && code <= 32){
            return true;
        }

        // Push, dup and swap operations
        if(code >= 95 && code <= 159){
            return true;
        }

        return false;
    }

    private String getErrorString(RuntimeException error) {
        if (error != null) {
            return error.getMessage();
        }

        return "";
    }

    @Override
    public byte[] getData() { return this.traceBuilder.build().toByteArray();}

    @Override
    public Trace getInstance() { return this.traceBuilder.build();}
}
