package info.skyblond.demo.neo;

import info.skyblond.demo.neo.contract.TestContract;
import info.skyblond.demo.neo.env.Constants;
import info.skyblond.demo.neo.env.DefaultAccount;
import info.skyblond.demo.neo.utils.ContractHelper;
import io.neow3j.contract.SmartContract;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger("Application");
    private static final Account DEPLOY_ACCOUNT = DefaultAccount.GENESIS_MULTI_SIG_ACCOUNT;
    private static final Wallet DEPLOY_SIGN_WALLET = DefaultAccount.GENESIS_WALLET;
    private static final Class<TestContract> TEST_CONTRACT_CLASS = TestContract.class;
    private static SmartContract contract = null;

    public static void testRun() throws Throwable {
        // TODO test run here
    }

    public static void main(String[] args) {
        boolean isError = false;
        try {
            // init
            var contractRes = ContractHelper.compileContract(TEST_CONTRACT_CLASS);
            Hash160 contractHash = ContractHelper.deployContract(contractRes, DEPLOY_ACCOUNT, DEPLOY_SIGN_WALLET);
            contract = new SmartContract(contractHash, Constants.NEOW3J);
            logger.info("Start executing user's code");
            // test run
            testRun();
        } catch (Throwable t) {
            logger.error("Error occur: ", t);
            logger.info("Trying to cleaning up...");
            isError = true;
            if (t.getLocalizedMessage() != null && t.getLocalizedMessage().contains("Contract Already Exists: ")) {
                contract = new SmartContract(new Hash160(t.getLocalizedMessage().split("Contract Already Exists: ")[1]), Constants.NEOW3J);
            }
        } finally {
            // clean up
            ContractHelper.destroyContract(contract, DEPLOY_ACCOUNT, DEPLOY_SIGN_WALLET);
        }
        if (isError) {
            System.exit(-1);
        } else {
            System.exit(0);
        }
    }
}
