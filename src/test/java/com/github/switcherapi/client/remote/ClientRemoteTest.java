package com.github.switcherapi.client.remote;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.ContextBuilder;
import com.github.switcherapi.client.SwitcherContext;
import com.github.switcherapi.client.SwitcherProperties;
import com.github.switcherapi.client.exception.SwitchersValidationException;
import com.github.switcherapi.client.model.SwitcherRequest;
import com.github.switcherapi.client.model.SwitcherResult;
import com.github.switcherapi.client.remote.dto.SwitchersCheck;
import com.github.switcherapi.client.service.SwitcherValidator;
import com.github.switcherapi.client.service.ValidatorService;
import com.github.switcherapi.client.service.local.ClientLocal;
import com.github.switcherapi.client.service.local.ClientLocalService;
import com.github.switcherapi.client.service.local.SwitcherLocalService;
import com.github.switcherapi.client.service.remote.ClientRemote;
import com.github.switcherapi.client.service.remote.ClientRemoteService;
import com.github.switcherapi.client.service.remote.SwitcherRemoteService;
import com.github.switcherapi.client.utils.Mapper;
import com.github.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.github.switcherapi.client.remote.Constants.DEFAULT_TIMEOUT;
import static org.junit.jupiter.api.Assertions.*;

class ClientRemoteTest extends MockWebServerHelper {

    private static ExecutorService executorService;

    private ClientRemote clientRemote;

    @BeforeAll
    static void setup() throws IOException {
        executorService = Executors.newSingleThreadExecutor();
        MockWebServerHelper.setupMockServer();

        Switchers.loadProperties();
        Switchers.configure(ContextBuilder.builder().url(String.format("http://localhost:%s", mockBackEnd.getPort())));
        Switchers.initializeClient();
    }

    @AfterAll
    static void tearDown() {
        MockWebServerHelper.tearDownMockServer();
        executorService.shutdown();
    }

    @BeforeEach
    void resetSwitcherContextState() {
        SwitcherProperties switcherProperties = Switchers.getSwitcherProperties();
        clientRemote = new ClientRemoteService(ClientWSImpl.build(switcherProperties, executorService, DEFAULT_TIMEOUT), switcherProperties);
        ((QueueDispatcher) mockBackEnd.getDispatcher()).clear();

        Switchers.configure(ContextBuilder.builder().checkSwitchers(false));
        Switchers.initializeClient();
    }

    @Test
    void shouldExecuteCriteria() {
        //given
        givenResponse(generateMockAuth(100));
        givenResponse(generateCriteriaResponse("true", false));

        SwitcherProperties switcherProperties = Switchers.getSwitcherProperties();
        SwitcherValidator validatorService = new ValidatorService();
        ClientLocal clientLocal = new ClientLocalService(validatorService);
        SwitcherRequest switcher = new SwitcherRequest(
                "KEY",
                new SwitcherRemoteService(clientRemote, new SwitcherLocalService(clientRemote, clientLocal, switcherProperties)),
                switcherProperties);

        //test
        SwitcherResult actual = Mapper.mapFrom(clientRemote.executeCriteria(Mapper.mapFrom(switcher)));
        assertTrue(actual.isItOn());
    }

    @Test
    void shouldCheckSwitchersError() {
        //given
        final Set<String> switcherKeys = new HashSet<>();
        switcherKeys.add("KEY");

        givenResponse(generateMockAuth(100));
        givenResponse(generateCheckSwitchersResponse(switcherKeys));

        //test
        SwitchersCheck actual = clientRemote.checkSwitchers(switcherKeys);
        assertEquals(1, actual.getNotFound().length);
    }

    @Test
    void shouldCheckSwitchersError_throughContextConfiguration() {
        //given
        final Set<String> switcherKeys = new HashSet<>();
        switcherKeys.add("KEY");

        givenResponse(generateMockAuth(100));
        givenResponse(generateCheckSwitchersResponse(switcherKeys));

        //test
        Switchers.configure(ContextBuilder.builder().checkSwitchers(true));
        SwitchersValidationException exception = assertThrows(SwitchersValidationException.class, SwitcherContext::initializeClient);
        assertEquals("Something went wrong: Unable to load the following Switcher Key(s): [KEY]", exception.getMessage());
    }

    @Test
    void shouldCheckSwitchersSuccess_throughContextConfiguration() {
        //given
        givenResponse(generateMockAuth(100));
        givenResponse(generateCheckSwitchersResponse(new HashSet<>()));

        //test
        Switchers.configure(ContextBuilder.builder().checkSwitchers(true));
        assertDoesNotThrow(SwitcherContext::initializeClient);
    }

}
