package com.github.switcherapi.client.remote;

import com.github.switcherapi.client.SwitcherContextBase;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.model.ContextKey;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.security.KeyStore;
import java.util.Objects;
import java.util.concurrent.Executors;

public class ClientWSBuilder {

    private static final String KEYSTORE_TYPE = "JKS";

    private static final String PROTOCOL = "TLSv1.2";

    private ClientWSBuilder() {
        throw new IllegalStateException("Utility class");
    }

    public static HttpClient.Builder builder() {
        int poolSize = Integer.parseInt(Objects.nonNull(SwitcherContextBase.contextStr(ContextKey.POOL_CONNECTION_SIZE)) ?
                SwitcherContextBase.contextStr(ContextKey.POOL_CONNECTION_SIZE) : "10");

        if (StringUtils.isNotBlank(SwitcherContextBase.contextStr(ContextKey.TRUSTSTORE_PATH))) {
            return builderSSL(poolSize);
        }

        return HttpClient.newBuilder().executor(Executors.newFixedThreadPool(poolSize));
    }

    public static HttpClient.Builder builderSSL(int poolSize) {
        try (InputStream readStream = new FileInputStream(SwitcherContextBase.contextStr(ContextKey.TRUSTSTORE_PATH))) {
            final KeyStore trustStore = KeyStore.getInstance(KEYSTORE_TYPE);
            trustStore.load(readStream, SwitcherContextBase.contextStr(ContextKey.TRUSTSTORE_PASSWORD).toCharArray());

            final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            final SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

            return HttpClient.newBuilder().sslContext(sslContext).executor(Executors.newFixedThreadPool(poolSize));
        } catch (Exception e) {
            throw new SwitcherException("Error while building SSL context", e);
        }
    }

}
