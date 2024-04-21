package com.github.switcherapi.client;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwitcherBasicTest extends MockWebServerHelper {

	private boolean authTokenGenerated = false;
	
	@BeforeAll
	static void setup() throws IOException {
		MockWebServerHelper.setupMockServer();

		Switchers.terminateSnapshotAutoUpdateWorker();
		Switchers.terminateAsyncExecutorService();
		Switchers.loadProperties(); // Load default properties from resources
		Switchers.initializeClient(); // SwitcherContext requires preload before config override
		Switchers.configure(ContextBuilder.builder() // Override default properties
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.local(false)
				.snapshotLocation(null)
				.snapshotSkipValidation(false)
				.environment("default")
				.silentMode(null)
				.snapshotAutoLoad(false)
				.snapshotAutoUpdateInterval(null));
    }
	
	@AfterAll
	static void tearDown() throws IOException {
		MockWebServerHelper.tearDownMockServer();
    }
	
	@BeforeEach
	void resetSwitcherContextState() {
		((QueueDispatcher) mockBackEnd.getDispatcher()).clear();

		Switchers.initializeClient();
	}
	
	@Test
	void shouldReturnTrue() {
		//auth
		givenAuthResponse();
		
		//criteria
		givenResponse(generateCriteriaResponse("true", false));
		
		//test
		Switcher switcher = Switchers.getSwitcher(Switchers.REMOTE_KEY);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void shouldReturnFalse() {
		//auth
		givenAuthResponse();
		
		//criteria
		givenResponse(generateCriteriaResponse("false", false));
		
		//test
		Switcher switcher = Switchers.getSwitcher(Switchers.REMOTE_KEY);
		assertFalse(switcher.isItOn());
	}

	// Helpers

	private void givenAuthResponse() {
		if (!authTokenGenerated) {
			givenResponse(generateMockAuth(10));
			authTokenGenerated = true;
		}
	}

}
