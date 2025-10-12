package com.switcherapi.client;

import com.switcherapi.Switchers;
import com.switcherapi.client.model.Switcher;
import com.switcherapi.fixture.CountDownHelper;
import com.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwitcherThrottleTest extends MockWebServerHelper {
	
	@BeforeAll
	static void setup() throws IOException {
		MockWebServerHelper.setupMockServer();
        
        Switchers.loadProperties();
        Switchers.configure(ContextBuilder.builder().url(String.format("http://localhost:%s", mockBackEnd.getPort())));
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
				.environment("default")
				.silentMode(null)
				.snapshotAutoLoad(false)
				.snapshotAutoUpdateInterval(null));
	}
	
	@Test
	void shouldReturnTrue_withThrottle() {
		Switchers.initializeClient();

		// Initial remote call
		givenResponse(generateMockAuth(10)); //auth
		givenResponse(generateCriteriaResponse("true", false)); //criteria - sync (cached)
		
		// Throttle period - should use cache
		givenResponse(generateCriteriaResponse("false", false)); //criteria - async (background)
		givenResponse(generateCriteriaResponse("false", false)); //criteria - async after 1 sec (background)
		
		//test
		Switcher switcher = Switchers
				.getSwitcher(Switchers.REMOTE_KEY)
				.checkValue("value")
				.throttle(1000);
		
		for (int i = 0; i < 100; i++) {
			assertTrue(switcher.isItOn());
		}

		CountDownHelper.wait(1);
		assertFalse(switcher.isItOn());
	}

}
