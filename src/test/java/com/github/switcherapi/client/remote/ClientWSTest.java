package com.github.switcherapi.client.remote;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.ContextBuilder;
import com.github.switcherapi.client.service.remote.ClientRemoteService;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.QueueDispatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientWSTest {

    private static MockWebServer mockBackEnd;

    private void givenResponse(MockResponse response) {
        ((QueueDispatcher) mockBackEnd.getDispatcher()).enqueueResponse(response);
    }

    private MockResponse generateTimeOut(int timeoutMs) {
        return new MockResponse()
                .setResponseCode(200)
                .setHeadersDelay(timeoutMs, TimeUnit.MILLISECONDS);
    }

    @BeforeAll
    static void setup() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        ((QueueDispatcher) mockBackEnd.getDispatcher()).setFailFast(true);

        Switchers.loadProperties();
        Switchers.configure(ContextBuilder.builder().url(String.format("http://localhost:%s", mockBackEnd.getPort())));
        Switchers.initializeClient();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    void resetSwitcherContextState() {
        ((QueueDispatcher) mockBackEnd.getDispatcher()).clear();
        ClientRemoteService.getInstance().clearAuthResponse();

        Switchers.configure(ContextBuilder.builder()
                .offlineMode(false)
                .snapshotLocation(null)
                .snapshotSkipValidation(false)
                .environment("default")
                .silentMode(null)
                .snapshotAutoLoad(false)
                .snapshotAutoUpdateInterval(null)
                .timeoutMs(null));

        Switchers.initializeClient();
    }

    @Test
    void shouldBeAlive() {
        // given
        ClientWS clientWS = new ClientWSImpl();
        givenResponse(generateTimeOut(100));

        // test
        boolean response = clientWS.isAlive();
        assertTrue(response);
    }

    @Test
    void shouldTimeOut() {
        // given
        ClientWS clientWS = new ClientWSImpl();
        givenResponse(generateTimeOut(3500));

        // test
        boolean response = clientWS.isAlive();
        assertFalse(response);
    }

    @Test
    void shouldExtendTimeOut() {
        // given
        Switchers.configure(ContextBuilder.builder()
                .timeoutMs("4000"));

        Switchers.initializeClient();

        ClientWS clientWS = new ClientWSImpl();
        givenResponse(generateTimeOut(3500));

        // test
        boolean response = clientWS.isAlive();
        assertTrue(response);
    }
}
