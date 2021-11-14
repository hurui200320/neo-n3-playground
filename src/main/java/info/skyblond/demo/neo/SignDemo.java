package info.skyblond.demo.neo;

import com.sun.tools.javac.util.List;
import info.skyblond.demo.neo.contract.TestContract;
import info.skyblond.demo.neo.env.Constants;
import info.skyblond.demo.neo.env.DefaultAccounts;
import info.skyblond.demo.neo.utils.CommonUtils;
import info.skyblond.demo.neo.utils.ContractHelper;
import io.neow3j.compiler.CompilationUnit;
import io.neow3j.contract.ContractManagement;
import io.neow3j.crypto.Sign;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.Witness;
import io.neow3j.types.Hash160;
import io.neow3j.utils.Await;
import io.neow3j.wallet.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class SignDemo {
    private static final Logger logger = LoggerFactory.getLogger("Application");

    private static final Account CONTRACT_OWNER = Account.createMultiSigAccount(
            List.of(DefaultAccounts.ALICE_ACCOUNT.getECKeyPair().getPublicKey(),
                    DefaultAccounts.BOB_ACCOUNT.getECKeyPair().getPublicKey(),
                    DefaultAccounts.USER_ACCOUNT.getECKeyPair().getPublicKey()
            ), 2
    );

    private static final Class<TestContract> TEST_CONTRACT_CLASS = TestContract.class;

    private static Transaction generateDeployRequest() throws Throwable {
        // Compile contract, OWNER is a multi-sig account
        CompilationUnit contractRes = ContractHelper.compileContract(TEST_CONTRACT_CLASS, CONTRACT_OWNER.getAddress());
        // calculate address
        Hash160 contractHash = ContractHelper.getContractHash(CONTRACT_OWNER,
                contractRes.getNefFile(), contractRes.getManifest());
        logger.info("Script hash of the deployed contract: " + contractHash);
        logger.info("Contract Address: " + contractHash.toAddress());
        // This is a deployment tx without any signature
        return new ContractManagement(Constants.NEOW3J)
                .deploy(contractRes.getNefFile(), contractRes.getManifest())
                // You need global scope since deploy will be:
                //     ContractManagement -> TestContract
                // The TestContract need to verify the sign, so it has to be global
                .signers(AccountSigner.global(CONTRACT_OWNER))
                .getUnsignedTransaction();
    }

    private static String memberSign(String hashDataBase64, Account account) {
        byte[] hashData = Base64.getDecoder().decode(hashDataBase64);
        Sign.SignatureData signatureData = Sign.signMessage(hashData, account.getECKeyPair());
        logger.info("Signature data: v: {}", signatureData.getV());
        logger.info("Signature data: r: {}", signatureData.getR());
        logger.info("Signature data: s: {}", signatureData.getS());

        return Base64.getEncoder().encodeToString(signatureData.getConcatenated());
    }

    private static void deploy(Transaction tx, String... signBase64) throws Exception {
        ArrayList<Sign.SignatureData> signatures = new ArrayList<>();
        for (String s : signBase64) {
            Sign.SignatureData signatureData = Sign.SignatureData.fromByteArray(Base64.getDecoder().decode(s));
            // From underlying code, I guess the V is not important, since only R and S is used.
            logger.info("Recovered signature data: v: {}", signatureData.getV());
            logger.info("Recovered signature data: r: {}", signatureData.getR());
            logger.info("Recovered signature data: s: {}", signatureData.getS());
            signatures.add(signatureData);
        }
        Witness multiSigWitness = Witness.createMultiSigWitness(signatures, CONTRACT_OWNER.getVerificationScript());
        tx.addWitness(multiSigWitness);
        // send
        NeoSendRawTransaction response = tx.send();
        if (response.hasError()) {
            throw new Exception(String.format("Deployment was not successful. Error message from neo-node was: "
                    + "'%s'\n", response.getError().getMessage()));
        }
        Await.waitUntilTransactionIsExecuted(tx.getTxId(), Constants.NEOW3J);
        logger.info("Deployed! Tx: {}", tx.getTxId());
    }

    private static void run() throws Throwable {
        // This request can be generated from member's code
        // As long as they got the same compile result, the tx should be same
        Transaction request = generateDeployRequest();
        logger.info("Request tx hash: {}", request.getTxId());
        // Only hash data is required for signing
        byte[] requestHashData = request.getHashData();
        String requestHashDataBase64 = Base64.getEncoder().encodeToString(requestHashData);
        logger.info("Request hash data: {}", requestHashDataBase64);

        logger.info("Alice signing....");
        String aliceSignBase64 = memberSign(requestHashDataBase64, DefaultAccounts.ALICE_ACCOUNT);
        logger.info("Alice signature: {}", aliceSignBase64);

        logger.info("Bob signing....");
        String bobSignBase64 = memberSign(requestHashDataBase64, DefaultAccounts.BOB_ACCOUNT);
        logger.info("Bob signature: {}", bobSignBase64);

        logger.info("Try to deploy...");
        deploy(request, aliceSignBase64, bobSignBase64);
    }


    public static void main(String[] args) {
        try {
            Map<Account, java.util.List<Account>> map = new HashMap<>();
            //noinspection ArraysAsListWithZeroOrOneArgument
            map.put(DefaultAccounts.GENESIS_MULTI_SIG_ACCOUNT, Arrays.asList(DefaultAccounts.NODE_ACCOUNT));
            // give contract owner gas to deploy contract
            CommonUtils.prepareAccounts(map, CONTRACT_OWNER);
            run();
        } catch (Throwable t) {
            logger.error("Error!", t);
        }
    }
}
