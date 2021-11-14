package info.skyblond.demo.neo.utils;

import info.skyblond.demo.neo.env.Constants;
import info.skyblond.demo.neo.env.DefaultAccounts;
import io.neow3j.transaction.Transaction;
import io.neow3j.wallet.Account;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class CommonUtils {
    public static double getGasWithDecimals(long value) {
        return value / Math.pow(10, 8);
    }

    public static void prepareAccounts(Map<Account, List<Account>> multiSigMap, Account... list) throws Throwable {
        for (int i = 0; i < list.length; i++) {
            Transaction tx = InvokeHelper.sign(Constants.GAS_TOKEN.transfer(DefaultAccounts.GENESIS_MULTI_SIG_ACCOUNT,
                    list[i].getScriptHash(), BigInteger.valueOf(50_00000000L)), multiSigMap);
            InvokeHelper.executeTx(tx, i == list.length - 1);
        }
    }
}
