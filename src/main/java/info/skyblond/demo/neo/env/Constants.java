package info.skyblond.demo.neo.env;

import io.neow3j.contract.GasToken;
import io.neow3j.contract.NeoToken;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;

public class Constants {
    public static final Neow3j NEOW3J = Neow3j.build(new HttpService("http://127.0.0.1:50012"));

    public static final NeoToken NEO_TOKEN = new NeoToken(NEOW3J);

    public static final GasToken GAS_TOKEN = new GasToken(NEOW3J);
}
