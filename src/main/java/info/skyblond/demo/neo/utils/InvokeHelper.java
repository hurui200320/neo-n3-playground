package info.skyblond.demo.neo.utils;

import info.skyblond.demo.neo.env.Constants;
import io.neow3j.contract.SmartContract;
import io.neow3j.crypto.ECKeyPair;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.transaction.*;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
import io.neow3j.types.ContractParameter;
import io.neow3j.utils.Await;
import io.neow3j.wallet.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.neow3j.transaction.Witness.createContractWitness;

public class InvokeHelper {
    private static final Logger logger = LoggerFactory.getLogger(InvokeHelper.class);

    public static NeoApplicationLog executeTx(Transaction tx, boolean wait) throws Exception {
        NeoSendRawTransaction response = tx.send();
        if (response.hasError()) {
            throw new Exception(String.format("Error when executing tx: %s", response.getError().getMessage()));
        }
        logger.info("Waiting for tx: {}", tx.getTxId());
        if (wait) {
            Await.waitUntilTransactionIsExecuted(tx.getTxId(), Constants.NEOW3J);
        }
        logger.info("Tx {} gas fee: {}", tx.getTxId(), CommonUtils.getGasWithDecimals(tx.getSystemFee() + tx.getNetworkFee()));
        return tx.getApplicationLog();
    }

    public static Transaction sign(TransactionBuilder builder, Map<Account, List<Account>> multiSigMap) throws Throwable {
        Transaction transaction = builder.getUnsignedTransaction();
        byte[] txBytes = transaction.getHashData();
        transaction.getSigners().forEach(signer -> {
            if (signer instanceof ContractSigner) {
                ContractSigner contractSigner = (ContractSigner) signer;
                transaction.addWitness(createContractWitness(contractSigner.getVerifyParameters()));
            } else {
                Account a = ((AccountSigner) signer).getAccount();
                if (a.isMultiSig() || a.getECKeyPair() == null) {
                    List<Account> multiSigSourceAccounts = multiSigMap.get(a);
                    if (multiSigSourceAccounts == null) {
                        throw new IllegalStateException("Transactions has multi-sig signers, but no source account defined.");
                    }
                    try {
                        transaction.addMultiSigWitness(a.getVerificationScript(), multiSigSourceAccounts.toArray(new Account[0]));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    signWithAccount(transaction, txBytes, a);
                }
            }
        });
        return transaction;
    }

    private static void signWithAccount(Transaction transaction, byte[] txBytes, Account acc) {
        ECKeyPair keyPair = acc.getECKeyPair();
        if (keyPair == null) {
            throw new TransactionConfigurationException("Cannot create transaction signature " +
                    "because account " + acc.getAddress() + " does not hold a private key.");
        }
        transaction.addWitness(Witness.create(txBytes, keyPair));
    }

}
