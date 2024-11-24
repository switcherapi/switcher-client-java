package com.github.switcherapi.client.remote;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.ContextBuilder;
import com.github.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.github.switcherapi.client.remote.Constants.DEFAULT_TIMEOUT;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientWSTest extends MockWebServerHelper {

    private static ExecutorService executorService;

    @BeforeAll
    static void setup() throws IOException {
        executorService = Executors.newSingleThreadExecutor();
        MockWebServerHelper.setupMockServer();

        Switchers.loadProperties();
        Switchers.configure(ContextBuilder.builder().url(String.format("http://localhost:%s", mockBackEnd.getPort())));
        Switchers.initializeClient();
    }

    @AfterAll
    static void tearDown() throws IOException {
        MockWebServerHelper.tearDownMockServer();
        executorService.shutdown();
    }

    @BeforeEach
    void resetSwitcherContextState() {
        ((QueueDispatcher) mockBackEnd.getDispatcher()).clear();

        Switchers.configure(ContextBuilder.builder());
        Switchers.initializeClient();
    }

    @Test
    void shouldBeAlive() {
        // given
        ClientWS clientWS = ClientWSImpl.build(Switchers.getSwitcherProperties(), executorService, DEFAULT_TIMEOUT);
        givenResponse(generateTimeOut(100));

        // test
        boolean response = clientWS.isAlive();
        assertTrue(response);
    }

    @Test
    void shouldTimeOut() {
        // given
        ClientWS clientWS = ClientWSImpl.build(Switchers.getSwitcherProperties(), executorService, DEFAULT_TIMEOUT);
        givenResponse(generateTimeOut(3500));

        // test
        boolean response = clientWS.isAlive();
        assertFalse(response);
    }

    @Test
    void shouldExtendTimeOut() {
        ClientWS clientWS = ClientWSImpl.build(Switchers.getSwitcherProperties(), executorService, 4000);
        givenResponse(generateTimeOut(3500));

        // test
        boolean response = clientWS.isAlive();
        assertTrue(response);
    }

}
