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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientWSTest extends MockWebServerHelper {

    @BeforeAll
    static void setup() throws IOException {
        MockWebServerHelper.setupMockServer();

        Switchers.loadProperties();
        Switchers.configure(ContextBuilder.builder().url(String.format("http://localhost:%s", mockBackEnd.getPort())));
        Switchers.initializeClient();
    }

    @AfterAll
    static void tearDown() throws IOException {
        MockWebServerHelper.tearDownMockServer();
    }

    @BeforeEach
    void resetSwitcherContextState() {
        ((QueueDispatcher) mockBackEnd.getDispatcher()).clear();

        Switchers.configure(ContextBuilder.builder().timeoutMs(null));
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
