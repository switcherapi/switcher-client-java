package com.github.switcherapi.client.remote;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.ContextBuilder;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.SwitchersCheck;
import com.github.switcherapi.client.model.response.CriteriaResponse;
import com.github.switcherapi.client.service.remote.ClientRemote;
import com.github.switcherapi.client.service.remote.ClientRemoteService;
import com.github.switcherapi.client.service.remote.SwitcherRemoteService;
import com.github.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.glassfish.jersey.internal.guava.Sets;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientRemoteTest extends MockWebServerHelper {

    private ClientRemote clientRemote;

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
        clientRemote = new ClientRemoteService();
        ((QueueDispatcher) mockBackEnd.getDispatcher()).clear();

        Switchers.configure(ContextBuilder.builder().timeoutMs(null));
        Switchers.initializeClient();
    }

    @Test
    void shouldExecuteCriteria() {
        //given
        givenResponse(generateMockAuth(100));
        givenResponse(generateCriteriaResponse("true", false));

        Switcher switcher = new Switcher("KEY", new SwitcherRemoteService());

        //test
        CriteriaResponse actual = clientRemote.executeCriteria(switcher);
        assertTrue(actual.isItOn());
    }

    @Test
    void shouldCheckSwitchers() {
        //given
        final Set<String> switcherKeys = Sets.newHashSet();
        switcherKeys.add("KEY");

        givenResponse(generateMockAuth(100));
        givenResponse(generateCheckSwitchersResponse(switcherKeys));

        //test
        SwitchersCheck actual = clientRemote.checkSwitchers(switcherKeys);
        assertEquals(1, actual.getNotFound().length);
    }

}
