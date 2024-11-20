package com.github.switcherapi.client.remote;

import com.github.switcherapi.client.ContextBuilder;
import com.github.switcherapi.client.SwitcherContextBase;
import com.github.switcherapi.client.exception.SwitcherException;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.ClientBuilder;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class ClientWSBuilderTest {

    @Test
    void shouldCreateClientBuilder() {
        // given
        SwitcherContextBase.configure(ContextBuilder.builder()
                .truststorePath("")
                .truststorePassword(""));

        // test
        ClientBuilder clientBuilder = ClientWSBuilder.builder();
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

        // test
        ClientBuilder clientBuilder = ClientWSBuilder.builder();
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

        // test
        assertThrows(SwitcherException.class, ClientWSBuilder::builder);
    }

    @Test
    void shouldNotCreateClientBuilderSSL_invalidKeystorePath() {
        // given
        SwitcherContextBase.configure(ContextBuilder.builder()
                .truststorePath("INVALID")
                .truststorePassword("changeit"));

        // test
        assertThrows(SwitcherException.class, ClientWSBuilder::builder);
    }
}
