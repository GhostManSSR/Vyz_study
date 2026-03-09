package mta.security;

import java.io.FileInputStream;
import java.security.KeyStore;

public class TestKeystore {
    public static void main(String[] args) {
        try {
            String path = "keystore.jks";
            String password = "changeit";

            FileInputStream fis = new FileInputStream(path);
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(fis, password.toCharArray());

            System.out.println("Keystore loaded successfully!");
            System.out.println("Aliases: " + ks.aliases().nextElement());

            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
