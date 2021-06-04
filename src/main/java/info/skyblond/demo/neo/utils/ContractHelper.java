package info.skyblond.demo.neo.utils;

import info.skyblond.demo.neo.env.Constants;
import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.Compiler;
import io.neow3j.contract.ContractManagement;
import io.neow3j.contract.NefFile;
import io.neow3j.contract.SmartContract;
import io.neow3j.protocol.core.response.ContractManifest;
import io.neow3j.transaction.Signer;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ContractHelper {
    private static final Logger logger = LoggerFactory.getLogger(ContractHelper.class);

    public static <T> CompilationUnit compileContract(Class<T> contractClass) throws IOException {
        logger.info("Compiling contract...");
        // Compile the NonFungibleToken contract and construct a SmartContract object from it.
        return new Compiler().compile(contractClass.getCanonicalName());
    }

    public static Hash160 deployContract(
            CompilationUnit res,
            Account account,
            Wallet wallet
    ) throws Throwable {
        logger.info("Deploying contract...");
        var tx = new ContractManagement(Constants.NEOW3J)
                .deploy(res.getNefFile(), res.getManifest())
                .signers(Signer.global(account.getScriptHash()))
                .wallet(wallet)
                .sign();
        var response = tx.send();
        if (response.hasError()) {
            throw new Exception(String.format("Deployment was not successful. Error message from neo-node was: "
                    + "'%s'\n", response.getError().getMessage()));
        }
        tx.track().blockingSubscribe();
        var contractHash = getContractHash(account, res.getNefFile(), res.getManifest());
        logger.info("Script hash of the deployed contract: " + contractHash);
        logger.info("Contract Address: " + contractHash.toAddress());
        return contractHash;
    }

    public static Hash160 getContractHash(Account account, NefFile nefFile, ContractManifest manifest) {
        return SmartContract.getContractHash(account.getScriptHash(), nefFile.getCheckSumAsInteger(), manifest.getName());
    }

    public static void destroyContract(
            SmartContract contract,
            Account account,
            Wallet wallet
    ) {
        logger.info("Destroying contract...");
        try {
            InvokeHelper.invokeFunction(
                    contract, "destroy",
                    new ContractParameter[0], new Signer[]{Signer.calledByEntry(account.getScriptHash())},
                    wallet
            );
        } catch (Throwable throwable) {
            logger.warn("Cannot destroy contract: ", throwable);
        }
    }
}
