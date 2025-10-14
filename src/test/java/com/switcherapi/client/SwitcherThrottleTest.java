package com.switcherapi.client;

import com.switcherapi.SwitchersBase;
import com.switcherapi.client.model.Switcher;
import com.switcherapi.client.model.SwitcherBuilder;
import com.switcherapi.fixture.CountDownHelper;
import com.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.switcherapi.client.remote.Constants.DEFAULT_ENV;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwitcherThrottleTest extends MockWebServerHelper {
	
	@BeforeAll
	static void setup() throws IOException {
		MockWebServerHelper.setupMockServer();

		SwitchersBase.configure(ContextBuilder.builder(true)
				.context(SwitchersBase.class.getName())
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.apiKey("TEST_API_KEY")
				.domain("TEST_DOMAIN")
				.component("TEST_COMPONENT")
				.environment(DEFAULT_ENV));
    }
	
	@AfterAll
	static void tearDown() {
		MockWebServerHelper.tearDownMockServer();
    }
	
	@BeforeEach
	void resetSwitcherContextState() {
		((QueueDispatcher) mockBackEnd.getDispatcher()).clear();
		SwitchersBase.initializeClient();
	}
	
	@Test
	void shouldReturnTrue_withThrottle() {
		// Initial remote call
		givenResponse(generateMockAuth(10)); //auth
		givenResponse(generateCriteriaResponse("true", false)); //criteria - sync (cached)
		
		// Throttle period - should use cache
		givenResponse(generateCriteriaResponse("false", false)); //criteria - async (background)
		givenResponse(generateCriteriaResponse("false", false)); //criteria - async after 1 sec (background)
		
		//test
		Switcher switcher = SwitchersBase
				.getSwitcher(SwitchersBase.USECASE11)
				.flush()
				.checkValue("value")
				.throttle(1000);
		
		for (int i = 0; i < 100; i++) {
			assertTrue(switcher.isItOn());
		}

		CountDownHelper.wait(1);
		assertFalse(switcher.isItOn());
	}

	@Test
	void shouldRetrieveNewResponse_whenStrategyInputChanged() {
		// Initial remote call
		givenResponse(generateMockAuth(10)); //auth
		givenResponse(generateCriteriaResponse("true", false)); //criteria - sync (cached)
		givenResponse(generateCriteriaResponse("false", false)); //criteria - async (cached)

		// Throttle period - should use cache
		givenResponse(generateCriteriaResponse("false", false)); //criteria - async after 1 sec (background)

		//test
		SwitcherBuilder switcher = SwitchersBase
				.getSwitcher(SwitchersBase.USECASE11)
				.flush()
				.throttle(1000);

		for (int i = 0; i < 100; i++) {
			assertTrue(switcher.checkValue("value").isItOn());
		}

		assertFalse(switcher.checkValue("value_changed").isItOn());
	}

}
