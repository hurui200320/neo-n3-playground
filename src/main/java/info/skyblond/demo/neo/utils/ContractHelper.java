package info.skyblond.demo.neo.utils;

import info.skyblond.demo.neo.env.Constants;
import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.Compiler;
import io.neow3j.contract.ContractManagement;
import io.neow3j.contract.NefFile;
import io.neow3j.contract.SmartContract;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.exceptions.TransactionConfigurationException;
import io.neow3j.types.Hash160;
import io.neow3j.utils.Await;
import io.neow3j.wallet.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContractHelper {
    private static final Logger logger = LoggerFactory.getLogger(ContractHelper.class);

    public static <T> CompilationUnit compileContract(
            Class<T> contractClass,
            String contractOwnerAddress
    ) throws IOException {
        logger.info("Compiling contract...");
        Map<String, String> replaceMap = new HashMap<>();
        replaceMap.put("<CONTRACT_OWNER_ADDRESS_PLACEHOLDER>", contractOwnerAddress);
        return new Compiler().compile(contractClass.getCanonicalName(), replaceMap);
    }

    public static Hash160 deployContract(
            CompilationUnit res,
            Account deployAccount,
            Map<Account, List<Account>> multiSigMap
    ) throws Throwable {
        logger.info("Deploying contract...");
        try {
            Transaction tx = InvokeHelper.sign(new ContractManagement(Constants.NEOW3J)
                    .deploy(res.getNefFile(), res.getManifest())
                    .signers(AccountSigner.global(deployAccount)), multiSigMap);
            NeoSendRawTransaction response = tx.send();
            if (response.hasError()) {
                throw new Exception(String.format("Deployment was not successful. Error message from neo-node was: "
                        + "'%s'\n", response.getError().getMessage()));
            }
            Await.waitUntilTransactionIsExecuted(tx.getTxId(), Constants.NEOW3J);
        } catch (TransactionConfigurationException e) {
            if (!e.getMessage().contains("Contract Already Exists")) {
                throw new RuntimeException(e);
            }
        }

        Hash160 contractHash = getContractHash(deployAccount, res.getNefFile(), res.getManifest());
        logger.info("Script hash of the deployed contract: " + contractHash);
        logger.info("Contract Address: " + contractHash.toAddress());
        return contractHash;
    }

    public static Hash160 getContractHash(Account account, NefFile nefFile, ContractManifest manifest) {
        return SmartContract.calcContractHash(account.getScriptHash(), nefFile.getCheckSumAsInteger(), manifest.getName());
    }
}
