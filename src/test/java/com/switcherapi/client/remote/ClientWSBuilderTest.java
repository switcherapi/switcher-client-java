package com.switcherapi.client.remote;

import com.switcherapi.client.ContextBuilder;
import com.switcherapi.client.SwitcherContextBase;
import com.switcherapi.client.SwitcherProperties;
import com.switcherapi.client.exception.SwitcherException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.ClientBuilder;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class ClientWSBuilderTest {

    private static ExecutorService executorService;

    @BeforeAll
    static void setup() {
        executorService = Executors.newSingleThreadExecutor();
    }

    @AfterAll
    static void tearDown() {
        executorService.shutdown();
    }

    @Test
    void shouldCreateClientBuilder() {
        // given
        SwitcherContextBase.configure(ContextBuilder.builder()
                .truststorePath("")
                .truststorePassword(""));

        SwitcherProperties properties = SwitcherContextBase.getSwitcherProperties();

        // test
        ClientBuilder clientBuilder = ClientWSBuilder.builder(executorService, properties);
        assertNotNull(clientBuilder);

        SSLContext sslContext = clientBuilder.build().getSslContext();
        assertNotNull(sslContext);
        assertEquals("TLS", sslContext.getProtocol());
    }

    @Test
    void shouldCreateClientBuilderSSL() {
        // given
        String truststorePath = Objects.requireNonNull(getClass().getClassLoader()
                .getResource("keystore.jks")).getPath();

        SwitcherContextBase.configure(ContextBuilder.builder()
                .truststorePath(truststorePath)
                .truststorePassword("changeit"));

        SwitcherProperties properties = SwitcherContextBase.getSwitcherProperties();

        // test
        ClientBuilder clientBuilder = ClientWSBuilder.builder(executorService, properties);
        assertNotNull(clientBuilder);

        SSLContext sslContext = clientBuilder.build().getSslContext();
        assertNotNull(sslContext);
        assertEquals("TLSv1.2", sslContext.getProtocol());
    }

    @Test
    void shouldNotCreateClientBuilderSSL_invalidKeystorePassword() {
        // given
        String truststorePath = Objects.requireNonNull(getClass().getClassLoader()
                .getResource("keystore.jks")).getPath();

        SwitcherContextBase.configure(ContextBuilder.builder()
                .truststorePath(truststorePath)
                .truststorePassword("INVALID"));

        SwitcherProperties properties = SwitcherContextBase.getSwitcherProperties();

        // test
        assertThrows(SwitcherException.class, () -> ClientWSBuilder.builder(executorService, properties));
    }

    @Test
    void shouldNotCreateClientBuilderSSL_invalidKeystorePath() {
        // given
        SwitcherContextBase.configure(ContextBuilder.builder()
                .truststorePath("INVALID")
                .truststorePassword("changeit"));

        SwitcherProperties properties = SwitcherContextBase.getSwitcherProperties();

        // test
        assertThrows(SwitcherException.class, () -> ClientWSBuilder.builder(executorService, properties));
    }
}
