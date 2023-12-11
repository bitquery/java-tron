package org.tron.streaming;

import com.google.protobuf.ByteString;
import io.netty.channel.SingleThreadEventLoop;
import lombok.Getter;
import org.bouncycastle.math.raw.Interleave;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.streaming.protobuf.TronMessage.CallValue;
import org.tron.streaming.protobuf.TronMessage.InternalTransaction;
import org.tron.streaming.protobuf.TronMessage.Contract;
import org.tron.streaming.protobuf.TronMessage.Log;
import org.tron.streaming.protobuf.TronMessage.Receipt;
import org.tron.streaming.protobuf.TronMessage.TransactionHeader;
import org.tron.streaming.protobuf.TronMessage.TransactionResult;
import org.tron.streaming.protobuf.TronMessage.Transaction;
import org.tron.streaming.protobuf.TronMessage.Witness;
import org.tron.streaming.protobuf.TronMessage.BlockHeader;
import org.tron.streaming.protobuf.TronMessage.BlockMessage;
import org.tron.protos.Protocol.TransactionInfo;
import org.tron.protos.Protocol;

import java.util.ArrayList;
import java.util.List;

public class BlockMessageCreator {

    private BlockCapsule newBlock;

    @Getter
    private BlockMessage blockMessage;

    public BlockMessageCreator(BlockCapsule newBlock) {
        this.newBlock = newBlock;

        BlockMessage.Builder blockBuilder = BlockMessage.newBuilder();
        this.blockMessage = blockBuilder.build();
    }

    public void create() {
        setBlockHeader();
        setBlockWitness();
        setTransactions();
    }

    private void setBlockHeader() {
        BlockHeader header = BlockHeader.newBuilder()
                .setNumber(newBlock.getNum())
                .setHash(newBlock.getBlockId().getByteString())
                .setTimestamp(newBlock.getTimeStamp())
                .setParentHash(newBlock.getParentBlockId().getByteString())
                .setVersion(newBlock.getInstance().getBlockHeader().getRawData().getVersion())
                .setTxTrieRoot(newBlock.getInstance().getBlockHeader().getRawData().getTxTrieRoot())
                .setAccountStateRoot(newBlock.getInstance().getBlockHeader().getRawData().getAccountStateRoot())
                .build();

        this.blockMessage = this.blockMessage.toBuilder().setHeader(header).build();
    }

    private void setBlockWitness() {
        Witness witness = Witness.newBuilder()
                .setAddress(newBlock.getWitnessAddress())
                .setId(newBlock.getInstance().getBlockHeader().getRawData().getWitnessId())
                .setSignature(newBlock.getInstance().getBlockHeader().getWitnessSignature())
                .build();

        this.blockMessage = this.blockMessage.toBuilder().setWitness(witness).build();
    }

    private void setTransactions() {
        List<TransactionInfo> txsInfo = newBlock.getResult().getInstance().getTransactioninfoList();

        int index = 0;
        for (TransactionInfo txInfo : txsInfo) {
            TransactionCapsule txCap = newBlock.getTransactions().get(index);

            TransactionHeader header = getTransactionHeader(txInfo, txCap, index);
            TransactionResult result = getTransactionResult(txInfo);
            Receipt receipt = getTransactionReceipt(txInfo);
            List<Log> logs = getLogs(txInfo);
            List<Contract> contracts = getContracts(txInfo, txCap);
            List<InternalTransaction> internalTransactions = getInternalTransactions(txInfo);

            Transaction tx = Transaction.newBuilder()
                    .setHeader(header)
                    .setResult(result)
                    .setReceipt(receipt)
                    .addAllLogs(logs)
                    .addAllContracts(contracts)
                    .addAllInternalTransactions(internalTransactions)
                    .build();

            this.blockMessage = this.blockMessage.toBuilder().addTransactions(tx).build();

            index++;
        }
    }

    private TransactionHeader getTransactionHeader(TransactionInfo txInfo, TransactionCapsule txCap, int index) {
        TransactionHeader header = TransactionHeader.newBuilder()
                .setId(txInfo.getId())
                .setFee(txInfo.getFee())
                .setIndex(index + 1)
                .setExpiration(txCap.getExpiration())
                .setData(ByteString.copyFrom(txCap.getData()))
                .setFeeLimit(txCap.getFeeLimit())
                .setTimestamp(txCap.getTimestamp())
                .addAllSignatures(txCap.getInstance().getSignatureList())
                .build();

        return header;
    }

    private TransactionResult getTransactionResult(TransactionInfo txInfo) {
        TransactionResult result = TransactionResult.newBuilder()
                .setStatus(txInfo.getResult().toString())
                .setMessage(txInfo.getResMessage().toStringUtf8())
                .build();

        return result;
    }
    private Receipt getTransactionReceipt(TransactionInfo txInfo) {
        Receipt receipt = Receipt.newBuilder()
                .setResult(txInfo.getReceipt().getResult().toString())
                .setEnergyPenaltyTotal(txInfo.getReceipt().getEnergyPenaltyTotal())
                .setEnergyFee(txInfo.getReceipt().getEnergyFee())
                .setEnergyUsageTotal(txInfo.getReceipt().getEnergyUsageTotal())
                .setOriginEnergyUsage(txInfo.getReceipt().getOriginEnergyUsage())
                .setNetUsage(txInfo.getReceipt().getNetUsage())
                .setNetFee(txInfo.getReceipt().getNetFee())
                .build();

        return receipt;
    }

    private List<Log> getLogs(TransactionInfo txInfo) {
        List<Log> logs = new ArrayList();

        int index = 1;
        for (TransactionInfo.Log txInfoLog : txInfo.getLogList()) {
            Log log = Log.newBuilder()
                    .setAddress(txInfoLog.getAddress())
                    .setData(txInfoLog.getData())
                    .addAllTopics(txInfoLog.getTopicsList())
                    .setIndex(index)
                    .build();

            logs.add(log);

            index++;
        }

        return logs;
    }

    private List<Contract> getContracts(TransactionInfo txInfo, TransactionCapsule txCap) {
        List<Contract> contracts = new ArrayList();

        ByteString address = txInfo.getContractAddress();
        String type = txCap.getInstance().getRawData().getContract(0).getType().name();
        String typeUrl = txCap.getInstance().getRawData().getContract(0).getParameter().getTypeUrl();

        Contract contract = Contract.newBuilder()
                .setAddress(address)
                .addAllExecutionResults(txInfo.getContractResultList())
                .setType(type)
                .setTypeUrl(typeUrl)
                .build();

        contracts.add(contract);

        return contracts;
    }

    private List<InternalTransaction> getInternalTransactions(TransactionInfo txInfo) {
        List<InternalTransaction> internalTransactions = new ArrayList();

        int index = 1;
        for (Protocol.InternalTransaction txInternalTx : txInfo.getInternalTransactionsList()) {
            List<CallValue> callValues = getCallvalues(txInternalTx);

            InternalTransaction internalTx = InternalTransaction.newBuilder()
                    .setCallerAddress(txInternalTx.getCallerAddress())
                    .setNote(txInternalTx.getNote().toStringUtf8())
                    .setTransferToAddress(txInternalTx.getTransferToAddress())
                    .addAllCallValues(callValues)
                    .setHash(txInternalTx.getHash())
                    .setIndex(index)
                    .build();

            internalTransactions.add(internalTx);

            index++;
        }

        return internalTransactions;
    }

    private List<CallValue> getCallvalues(Protocol.InternalTransaction internalTx) {
        List<CallValue> callValues = new ArrayList();

        for (Protocol.InternalTransaction.CallValueInfo callValueInfo : internalTx.getCallValueInfoList()){
            CallValue callValue = CallValue.newBuilder()
                    .setCallValue(callValueInfo.getCallValue())
                    .setTokenId(callValueInfo.getTokenId())
                    .build();

            callValues.add(callValue);
        }

        return callValues;
    }
}
