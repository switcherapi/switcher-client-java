package com.github.switcherapi.client.remote;

import com.github.switcherapi.client.ContextBuilder;
import com.github.switcherapi.client.SwitcherContextBase;
import com.github.switcherapi.client.exception.SwitcherException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import java.net.http.HttpClient;
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

        // test
        HttpClient.Builder clientBuilder = ClientWSBuilder.builder(executorService);
        assertNotNull(clientBuilder);

        SSLContext sslContext = clientBuilder.build().sslContext();
        assertNotNull(sslContext);
        assertEquals("Default", sslContext.getProtocol());
    }

    @Test
    void shouldCreateClientBuilderSSL() {
        // given
        String truststorePath = Objects.requireNonNull(getClass().getClassLoader()
                .getResource("keystore.jks")).getPath();

        SwitcherContextBase.configure(ContextBuilder.builder()
                .truststorePath(truststorePath)
                .truststorePassword("changeit"));

        // test
        HttpClient.Builder clientBuilder = ClientWSBuilder.builder(executorService);
        assertNotNull(clientBuilder);

        SSLContext sslContext = clientBuilder.build().sslContext();
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

        // test
        assertThrows(SwitcherException.class, () -> ClientWSBuilder.builder(executorService));
    }

    @Test
    void shouldNotCreateClientBuilderSSL_invalidKeystorePath() {
        // given
        SwitcherContextBase.configure(ContextBuilder.builder()
                .truststorePath("INVALID")
                .truststorePassword("changeit"));

        // test
        assertThrows(SwitcherException.class, () -> ClientWSBuilder.builder(executorService));
    }
}
