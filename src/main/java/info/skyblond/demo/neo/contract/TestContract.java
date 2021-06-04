package info.skyblond.demo.neo.contract;

import io.neow3j.devpack.annotations.ManifestExtra;
import io.neow3j.devpack.contracts.ContractManagement;

@ManifestExtra(key = "name", value = "Test Contract")
@ManifestExtra(key = "github", value = "https://github.com/hurui200320/neo-n3-playground")
@ManifestExtra(key = "author", value = "hurui200320")
public class TestContract {

    public static void foo(){
        // TODO contract code here
    }

    // -------------------- Template Code Below --------------------

    public static void destroy(){
        ContractManagement.destroy();
    }
}
