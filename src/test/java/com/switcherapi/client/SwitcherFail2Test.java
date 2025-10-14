package com.switcherapi.client;

import com.switcherapi.Switchers;
import com.switcherapi.client.exception.SwitcherContextException;
import com.switcherapi.client.exception.SwitcherRemoteException;
import com.switcherapi.client.model.SwitcherRequest;
import com.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.switcherapi.client.remote.Constants.DEFAULT_ENV;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SwitcherFail2Test extends MockWebServerHelper {
	
	@BeforeAll
	static void setup() throws IOException {
		MockWebServerHelper.setupMockServer();

		Switchers.loadProperties();
		Switchers.configure(ContextBuilder.builder().url(String.format("http://localhost:%s", mockBackEnd.getPort())));
		Switchers.initializeClient();
    }
	
	@AfterAll
	static void tearDown() {
		MockWebServerHelper.tearDownMockServer();
    }
	
	@BeforeEach
	void resetSwitcherContextState() {
		((QueueDispatcher) mockBackEnd.getDispatcher()).clear();
		
		Switchers.configure(ContextBuilder.builder()
				.local(false)
				.snapshotLocation(null)
				.snapshotSkipValidation(false)
				.environment(DEFAULT_ENV)
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
		
		SwitcherRequest switcher = Switchers.getSwitcher(Switchers.REMOTE_KEY);
		assertThrows(SwitcherRemoteException.class, switcher::isItOn);
	}

	@Test
	void shouldReturnError_switcherCannotRunLocally() {
		SwitcherRequest switcher = Switchers.getSwitcher(Switchers.REMOTE_KEY);

		Exception ex = assertThrows(SwitcherContextException.class, () -> switcher.remote(false));
		assertEquals("Something went wrong: Context has errors - Switcher is not configured to run locally", ex.getMessage());
	}

}
