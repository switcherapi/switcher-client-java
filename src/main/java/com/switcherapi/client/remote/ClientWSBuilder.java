package com.switcherapi.client.remote;

import com.switcherapi.client.SwitcherProperties;
import com.switcherapi.client.exception.SwitcherException;
import com.switcherapi.client.model.ContextKey;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;

public class ClientWSBuilder {

    private static final String KEYSTORE_TYPE = "JKS";

    private static final String PROTOCOL = "TLSv1.2";

    private ClientWSBuilder() {
        throw new IllegalStateException("Utility class");
    }

    public static HttpClient.Builder builder(final ExecutorService executorService, final SwitcherProperties switcherProperties) {
        if (StringUtils.isNotBlank(switcherProperties.getValue(ContextKey.TRUSTSTORE_PATH))) {
            return builderSSL(executorService, switcherProperties);
        }

        return HttpClient.newBuilder().executor(executorService);
    }

    private static HttpClient.Builder builderSSL(final ExecutorService executorService, final SwitcherProperties switcherProperties) {
        try (InputStream readStream = new FileInputStream(switcherProperties.getValue(ContextKey.TRUSTSTORE_PATH))) {
            final KeyStore trustStore = KeyStore.getInstance(KEYSTORE_TYPE);
            trustStore.load(readStream, switcherProperties.getValue(ContextKey.TRUSTSTORE_PASSWORD).toCharArray());

            final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            final SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

            return HttpClient.newBuilder().sslContext(sslContext).executor(executorService);
        } catch (Exception e) {
            throw new SwitcherException("Error while building SSL context", e);
        }
    }

}
