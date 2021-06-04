package info.skyblond.demo.neo.env;

import io.neow3j.crypto.ECKeyPair;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;

import java.math.BigInteger;
import java.util.Collections;

public class DefaultAccount {
    public static final Account GENESIS_DEFAULT_ACCOUNT = new Account(
            ECKeyPair.create(new BigInteger("c42675cc4c8d1c3a319d2c972690654390153a21717674e6ebe46c2c62e5cd48", 16))
    );

    public static final Account GENESIS_MULTI_SIG_ACCOUNT = Account.createMultiSigAccount(Collections.singletonList(GENESIS_DEFAULT_ACCOUNT.getECKeyPair().getPublicKey()), 1);

    public static final Wallet GENESIS_WALLET = Wallet.withAccounts(GENESIS_DEFAULT_ACCOUNT, GENESIS_MULTI_SIG_ACCOUNT);


    public static final Account ALICE_ACCOUNT = new Account(
            ECKeyPair.create(new BigInteger("10823794535c0c7cec2e82de6e144d6eb81ffc24fd6ce46ef4110b4b4333c635", 16))
    );

    public static final Wallet ALICE_WALLET = Wallet.withAccounts(ALICE_ACCOUNT);


    public static final Account BOB_ACCOUNT = new Account(
            ECKeyPair.create(new BigInteger("9ebea73a1029f444ea07914e75ec72c87bdc848f49ef68431043667e30420d49", 16))
    );

    public static final Wallet BOB_WALLET = Wallet.withAccounts(BOB_ACCOUNT);


    public static final Account USER_ACCOUNT = new Account(
            ECKeyPair.create(new BigInteger("fbe6c8940ac7c3c81a6fa88ab46fc6346714a58f958a63b82465fe3b481cc106", 16))
    );

    public static final Wallet USER_WALLET = Wallet.withAccounts(USER_ACCOUNT);

}
