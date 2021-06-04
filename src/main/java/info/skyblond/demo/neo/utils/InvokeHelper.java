package info.skyblond.demo.neo.utils;

import io.neow3j.contract.SmartContract;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.transaction.Signer;
import io.neow3j.transaction.Transaction;
import io.neow3j.types.ContractParameter;
import io.neow3j.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public class InvokeHelper {
    private static final Logger logger = LoggerFactory.getLogger(InvokeHelper.class);

    private static Pair<Transaction, NeoSendRawTransaction> buildTxAndSend(
            SmartContract contract, String function,
            ContractParameter[] parameters, Signer[] signers,
            Wallet wallet
    ) throws Throwable {
        var tx = contract
                .invokeFunction(function, parameters)
                .signers(signers).wallet(wallet).sign();
        var response = tx.send();
        return new Pair<>(tx, response);
    }

    public static NeoApplicationLog invokeFunction(SmartContract contract, String function, ContractParameter[] parameters, Signer[] signers, Wallet wallet) throws Throwable {
        var txAndResp = buildTxAndSend(contract, function, parameters, signers, wallet);
        AtomicReference<NeoApplicationLog> result = new AtomicReference<>();
        if (txAndResp.getSecond().getError() == null) {
            txAndResp.getFirst().track().blockingSubscribe(l -> {
                result.set(txAndResp.getFirst().getApplicationLog());
                logger.info("{} tx: {}", function, txAndResp.getFirst().getTxId());
            });
        } else {
            throw new Exception(String.format("Error when invoking %s: %s", function, txAndResp.getSecond().getError().getMessage()));
        }
        logger.info("{} gas fee: {}", function, CommonUtils.getGasWithDecimals(txAndResp.getFirst().getSystemFee() + txAndResp.getFirst().getNetworkFee()));
        return result.get();
    }
}
