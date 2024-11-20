package com.github.switcherapi.client.remote;

import com.github.switcherapi.client.SwitcherContextBase;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.model.ContextKey;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.client.ClientBuilder;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

public class ClientWSBuilder {

    private static final String KEYSTORE_TYPE = "JKS";

    private static final String PROTOCOL = "TLSv1.2";

    private ClientWSBuilder() {
        throw new IllegalStateException("Utility class");
    }

    public static ClientBuilder builder() {
        if (StringUtils.isNotBlank(SwitcherContextBase.contextStr(ContextKey.TRUSTSTORE_PATH))) {
            return builderSSL();
        }

        return ClientBuilder.newBuilder();
    }

    public static ClientBuilder builderSSL() {
        try (InputStream readStream = new FileInputStream(SwitcherContextBase.contextStr(ContextKey.TRUSTSTORE_PATH))) {
            final KeyStore trustStore = KeyStore.getInstance(KEYSTORE_TYPE);
            trustStore.load(readStream, SwitcherContextBase.contextStr(ContextKey.TRUSTSTORE_PASSWORD).toCharArray());

            final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            final SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

            return ClientBuilder.newBuilder().sslContext(sslContext);
        } catch (Exception e) {
            throw new SwitcherException("Error while building SSL context", e);
        }
    }

}
