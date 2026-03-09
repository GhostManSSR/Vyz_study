package mta.security;

import java.security.*;

public class KeyProvider {

    private static PrivateKey privateKey;

    static {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();
            privateKey = pair.getPrivate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PrivateKey getPrivateKey() {
        return privateKey;
    }
}
