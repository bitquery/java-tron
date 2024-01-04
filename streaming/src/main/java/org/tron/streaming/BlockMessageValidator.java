package org.tron.streaming;

import com.google.protobuf.Empty;
import evm_messages.BlockMessageOuterClass.Trace;
import org.tron.core.exception.BadBlockException;
import org.tron.core.exception.StreamingMessageValidateException;
import org.tron.protos.streaming.TronMessage;

public class BlockMessageValidator {
    TronMessage.BlockMessage message;
    long blockNumber;

    public BlockMessageValidator(TronMessage.BlockMessage message) {
        this.message = message;
        this.blockNumber = message.getHeader().getNumber();
    }

    public void validate() throws StreamingMessageValidateException {
        transactions();
    }

    public void transactions() throws StreamingMessageValidateException {
        for (TronMessage.Transaction tx : this.message.getTransactionsList()) {
            internalTransactionsAndTraces(tx);
            logsAndCaptureStateLogs(tx);
        }
    }

    private void internalTransactionsAndTraces(TronMessage.Transaction tx) throws StreamingMessageValidateException {
        int expectedCount = tx.getInternalTransactionsCount();
        int actualCount = 0;

        Trace trace = tx.getTrace();

        if (trace.hasCaptureStart()) {
            actualCount += 1;
        }
        actualCount += trace.getCallsCount();

        if (expectedCount != actualCount) {
            throw new StreamingMessageValidateException(
                    String.format(
                            "'Internal Transaction' validation for block %s wasn't passed. Expected: %d, actual: %d",
                            this.blockNumber,
                            expectedCount,
                            actualCount
                    )
            );
        }
    }

    private void logsAndCaptureStateLogs(TronMessage.Transaction tx)  throws StreamingMessageValidateException {
        int expectedCount = tx.getLogsCount();
        long actualCount = tx.getTrace().getCaptureStatesList().stream()
                .filter(x -> x.hasLog())
                .count();

        if (expectedCount != actualCount) {
            throw new StreamingMessageValidateException(
                    String.format(
                            "'LOG' validation for block %s wasn't passed. Expected: %d, actual: %d",
                            this.blockNumber,
                            expectedCount,
                            actualCount
                    )
            );
        }
    }
}
