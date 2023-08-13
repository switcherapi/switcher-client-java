package com.github.switcherapi.client;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.exception.SwitcherRemoteException;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SwitcherApiMock2Test extends MockWebServerHelper {
	
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
		
		Switchers.configure(ContextBuilder.builder()
				.offlineMode(false)
				.snapshotLocation(null)
				.snapshotSkipValidation(false)
				.environment("default")
				.silentMode(null)
				.snapshotAutoLoad(false)
				.snapshotAutoUpdateInterval(null));
		
		Switchers.initializeClient();
	}

	@Test
	void shouldReturnError_componentNotRegistered() {
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria
		givenResponse(generateStatusResponse("401"));
		
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		assertThrows(SwitcherRemoteException.class, switcher::isItOn);
	}

}
