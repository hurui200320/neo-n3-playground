package info.skyblond.demo.neo.contract;

import io.neow3j.devpack.ByteString;
import io.neow3j.devpack.Hash160;
import io.neow3j.devpack.Runtime;
import io.neow3j.devpack.StringLiteralHelper;
import io.neow3j.devpack.annotations.ManifestExtra;
import io.neow3j.devpack.annotations.OnDeployment;
import io.neow3j.devpack.annotations.OnVerification;
import io.neow3j.devpack.annotations.Permission;
import io.neow3j.devpack.contracts.ContractManagement;

@ManifestExtra(key = "name", value = "Test Contract")
@ManifestExtra(key = "github", value = "https://github.com/hurui200320/neo-n3-playground")
@ManifestExtra(key = "author", value = "hurui200320")
// ContractManagement::*
@Permission(contract = "0xfffdc93764dbaddd97c48f252a53ea4643faa3fd")
public class TestContract {
    private static final Hash160 CONTRACT_OWNER = StringLiteralHelper.addressToScriptHash("<CONTRACT_OWNER_ADDRESS_PLACEHOLDER>");

    public static boolean foo() {
        return Runtime.checkWitness(CONTRACT_OWNER);
    }

    @OnDeployment
    public static void deploy(Object data, boolean update) throws Exception {
        if (!Runtime.checkWitness(CONTRACT_OWNER)) {
            throw new Exception("Not contract owner!");
        }
    }

    public static void destroy() throws Exception {
        ContractManagement.destroy();
    }
}
