package com.github.switcherapi.client;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.fixture.CountDownHelper;
import com.github.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SwitcherThrottleTest extends MockWebServerHelper {
	
	@BeforeAll
	static void setup() throws IOException {
		MockWebServerHelper.setupMockServer();
        
        Switchers.loadProperties();
        Switchers.configure(ContextBuilder.builder().url(String.format("http://localhost:%s", mockBackEnd.getPort())));
    }
	
	@AfterAll
	static void tearDown() throws IOException {
		MockWebServerHelper.tearDownMockServer();
    }
	
	@BeforeEach
	void resetSwitcherContextState() {
		((QueueDispatcher) mockBackEnd.getDispatcher()).clear();

		Switchers.configure(ContextBuilder.builder()
				.local(false)
				.snapshotLocation(null)
				.snapshotSkipValidation(false)
				.environment("default")
				.silentMode(null)
				.snapshotAutoLoad(false)
				.snapshotAutoUpdateInterval(null));
	}
	
	@Test
	void shouldReturnTrue_withThrottle() {
		Switchers.initializeClient();

		// First call
		givenResponse(generateMockAuth(10)); //auth
		givenResponse(generateCriteriaResponse("true", false)); //criteria
		
		// Async call
		givenResponse(generateMockAuth(10)); //auth
		givenResponse(generateCriteriaResponse("true", false)); //criteria
		
		//test
		Switcher switcher = Switchers.getSwitcher(Switchers.REMOTE_KEY);
		
		for (int i = 0; i < 10; i++) {
			assertTrue(switcher.throttle(1000).isItOn());
		}
		
		// Async call
		givenResponse(generateMockAuth(10)); //auth
		givenResponse(generateCriteriaResponse("true", false)); //criteria

		CountDownHelper.wait(1);
		assertTrue(switcher.isItOn());
	}

}
