package com.switcherapi.client;

import com.switcherapi.SwitchersBase;
import com.switcherapi.client.model.Switcher;
import com.switcherapi.fixture.CountDownHelper;
import com.switcherapi.fixture.MockWebServerHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.switcherapi.client.remote.Constants.DEFAULT_ENV;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwitcherThrottle1Test extends MockWebServerHelper {
	
	@BeforeAll
	static void setup() throws IOException {
		setupMockServer();

		SwitchersBase.configure(ContextBuilder.builder(true)
				.context(SwitchersBase.class.getName())
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.apiKey("apiKey")
				.domain("domain")
				.component("component")
				.environment(DEFAULT_ENV));

		SwitchersBase.initializeClient();
    }
	
	@AfterAll
	static void tearDown() {
		tearDownMockServer();
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
				.flushExecutions()
				.resetInputs()
				.checkValue("value")
				.throttle(1000);
		
		for (int i = 0; i < 50; i++) {
			assertTrue(switcher.isItOn());
		}

		assertEquals(Boolean.FALSE,
				CountDownHelper.waitUntil(10, false, switcher::isItOn));
	}

}
