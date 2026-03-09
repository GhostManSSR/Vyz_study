package mta.smtp;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;

public class TLSUpgrade {

    private static final String KEYSTORE_PATH = "keystore.jks";
    private static final String KEYSTORE_PASSWORD = "changeit";

    public static SSLContext createSSLContext() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(KEYSTORE_PATH)) {
            ks.load(fis, KEYSTORE_PASSWORD.toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, KEYSTORE_PASSWORD.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return sslContext;
    }
}
