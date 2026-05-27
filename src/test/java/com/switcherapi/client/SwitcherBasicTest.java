package com.switcherapi.client;

import com.switcherapi.SwitchersBase;
import com.switcherapi.client.model.SwitcherRequest;
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

class SwitcherBasicTest extends MockWebServerHelper {

	private boolean authTokenGenerated = false;
	
	@BeforeAll
	static void setup() throws IOException {
		setupMockServer();
		
		SwitchersBase.configure(ContextBuilder.builder(true)
				.context(SwitchersBase.class.getName())
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.domain("domain")
				.apiKey("apiKey")
				.component("component")
				.environment(DEFAULT_ENV));
    }
	
	@AfterAll
	static void tearDown() {
		tearDownMockServer();
    }
	
	@BeforeEach
	void resetSwitcherContextState() {
		((QueueDispatcher) mockBackEnd.getDispatcher()).clear();

		SwitchersBase.initializeClient();
	}
	
	@Test
	void shouldReturnTrue() {
		//auth
		givenAuthResponse();
		
		//criteria
		givenResponse(generateCriteriaResponse("true", false));
		
		//test
		SwitcherRequest switcher = SwitchersBase.getSwitcher(SwitchersBase.USECASE11);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void shouldReturnFalse() {
		//auth
		givenAuthResponse();
		
		//criteria
		givenResponse(generateCriteriaResponse("false", false));
		
		//test
		SwitcherRequest switcher = SwitchersBase.getSwitcher(SwitchersBase.USECASE11);
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
