package info.skyblond.demo.neo;

import com.sun.tools.javac.util.List;
import info.skyblond.demo.neo.contract.TestContract;
import info.skyblond.demo.neo.env.Constants;
import info.skyblond.demo.neo.env.DefaultAccounts;
import info.skyblond.demo.neo.utils.CommonUtils;
import info.skyblond.demo.neo.utils.ContractHelper;
import info.skyblond.demo.neo.utils.InvokeHelper;
import io.neow3j.compiler.CompilationUnit;
import io.neow3j.contract.SmartContract;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.Transaction;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger("Application");

    private static final Account CONTRACT_OWNER = Account.createMultiSigAccount(
            List.of(DefaultAccounts.ALICE_ACCOUNT.getECKeyPair().getPublicKey(),
                    DefaultAccounts.BOB_ACCOUNT.getECKeyPair().getPublicKey(),
                    DefaultAccounts.USER_ACCOUNT.getECKeyPair().getPublicKey()
            ), 2
    );

    private static final Map<Account, java.util.List<Account>> MULTI_SIG_MAP = new HashMap<>();

    static {
        //noinspection ArraysAsListWithZeroOrOneArgument
        MULTI_SIG_MAP.put(DefaultAccounts.GENESIS_MULTI_SIG_ACCOUNT, Arrays.asList(DefaultAccounts.NODE_ACCOUNT));
        MULTI_SIG_MAP.put(CONTRACT_OWNER, Arrays.asList(DefaultAccounts.ALICE_ACCOUNT,
                DefaultAccounts.BOB_ACCOUNT, DefaultAccounts.USER_ACCOUNT));
    }

    private static final Class<TestContract> TEST_CONTRACT_CLASS = TestContract.class;
    private static SmartContract contract = null;

    private static void run() throws Throwable {
        // Compile contract, OWNER is a multi-sig account
        CompilationUnit contractRes = ContractHelper.compileContract(TEST_CONTRACT_CLASS, CONTRACT_OWNER.getAddress());
        contractRes.getManifest().getAbi().getMethods().forEach(method -> System.out.println(method.getName()));
        // Deploy contract with Node
        Hash160 contractHash = ContractHelper.deployContract(contractRes, CONTRACT_OWNER, MULTI_SIG_MAP);
        contract = new SmartContract(contractHash, Constants.NEOW3J);

        testContractOwner();
        testMultiSigMember();
    }

    private static void testContractOwner() throws Throwable {
        Transaction tx = InvokeHelper.sign(contract.invokeFunction("foo")
                .signers(AccountSigner.calledByEntry(CONTRACT_OWNER)), MULTI_SIG_MAP);
        boolean result = InvokeHelper.executeTx(tx, true).getExecutions().get(0).getStack().get(0).getBoolean();
        logger.info("Owner is owner: {}", result);
    }

    private static void testMultiSigMember() throws Throwable {
        Transaction tx = InvokeHelper.sign(contract.invokeFunction("foo")
                .signers(AccountSigner.calledByEntry(DefaultAccounts.ALICE_ACCOUNT),
                        AccountSigner.calledByEntry(DefaultAccounts.BOB_ACCOUNT),
                        AccountSigner.calledByEntry(DefaultAccounts.USER_ACCOUNT)), MULTI_SIG_MAP);
        boolean result = InvokeHelper.executeTx(tx, true).getExecutions().get(0).getStack().get(0).getBoolean();
        logger.info("Member is owner: {}", result);
    }

    public static void main(String[] args) {
        try {
            CommonUtils.prepareAccounts(MULTI_SIG_MAP, CONTRACT_OWNER, DefaultAccounts.ALICE_ACCOUNT,
                    DefaultAccounts.BOB_ACCOUNT, DefaultAccounts.USER_ACCOUNT);
            run();
        } catch (Throwable t) {
            logger.error("Error!", t);
        } finally {
            try {
                Transaction tx = InvokeHelper.sign(contract.invokeFunction("_deploy",
                                ContractParameter.string("asd"), ContractParameter.bool(true))
                        .signers(AccountSigner.calledByEntry(DefaultAccounts.ALICE_ACCOUNT)), MULTI_SIG_MAP);
                InvokeHelper.executeTx(tx, true);
            } catch (Throwable t) {
                logger.error("Error!", t);
            }
        }
    }
}
