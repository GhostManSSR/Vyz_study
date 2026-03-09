package mta.security;

import java.security.*;
import java.util.Base64;

public class DKIMSigner {

    private PrivateKey privateKey;

    public DKIMSigner(PrivateKey key) {
        this.privateKey = key;
    }

    public String sign(String data) throws Exception {

        Signature signature =
                Signature.getInstance("SHA256withRSA");

        signature.initSign(privateKey);

        signature.update(data.getBytes());

        byte[] signed = signature.sign();

        return Base64.getEncoder().encodeToString(signed);
    }
}
