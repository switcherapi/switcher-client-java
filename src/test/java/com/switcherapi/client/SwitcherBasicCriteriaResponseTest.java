package com.switcherapi.client;

import com.switcherapi.SwitchersBase;
import com.switcherapi.client.model.SwitcherBuilder;
import com.switcherapi.client.model.SwitcherRequest;
import com.switcherapi.client.model.SwitcherResult;
import com.switcherapi.fixture.MetadataErrorSample;
import com.switcherapi.fixture.MetadataSample;
import com.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.switcherapi.client.remote.Constants.DEFAULT_ENV;
import static org.junit.jupiter.api.Assertions.*;

class SwitcherBasicCriteriaResponseTest extends MockWebServerHelper {

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
	void shouldReturnCriteriaResponse() {
		//auth
		givenAuthResponse();

		//criteria
		givenResponse(generateCriteriaResponse("true", "Success"));

		//test
		SwitcherRequest switcher = SwitchersBase.getSwitcher(SwitchersBase.USECASE11);
		SwitcherResult response = switcher.submit();

		assertTrue(response.isItOn());
        assertEquals("Success", response.getReason());
	}

	@Test
	void shouldReturnCriteriaResponseWithInputs() {
		//auth
		givenAuthResponse();

		//criteria
		givenResponse(generateCriteriaResponse("false", "Strategy VALUE_VALIDATION does not agree"));

		//test
		SwitcherRequest switcher = SwitchersBase.getSwitcher(SwitchersBase.USECASE11);
		SwitcherResult response = switcher
				.checkValue("value")
				.checkNumeric("10")
				.submit();

		assertFalse(response.isItOn());
		assertEquals("Strategy VALUE_VALIDATION does not agree", response.getReason());
	}

	@Test
	void shouldFlushStrategyInputs() {
		SwitcherBuilder switcherBuilder = SwitchersBase
				.getSwitcher(SwitchersBase.USECASE11)
				.checkValue("value")
				.checkNumeric("10");

		assertEquals(2, switcherBuilder.getEntry().size());

		//test
		switcherBuilder
				.resetInputs()
				.checkValue("anotherValue");

		assertEquals(1, switcherBuilder.getEntry().size());
	}

	@Test
	void shouldReturnCriteriaResponseWithMetadata() {
		//auth
		givenAuthResponse();

		//criteria
		givenResponse(generateCriteriaResponse(new MetadataSample("123")));

		//test
		SwitcherRequest switcher = SwitchersBase.getSwitcher(SwitchersBase.USECASE11);
		SwitcherResult response = switcher.submit();

		assertEquals("123", response.getMetadata(MetadataSample.class).getTransactionId());
	}

	@Test
	void shouldReturnCriteriaResponseWithWrongMetadata() {
		//auth
		givenAuthResponse();

		//criteria
		givenResponse(generateCriteriaResponse(new MetadataErrorSample("123")));

		//test
		SwitcherRequest switcher = SwitchersBase.getSwitcher(SwitchersBase.USECASE11);
		SwitcherResult response = switcher.submit();

		assertNotNull(response.getMetadata(MetadataSample.class));
		assertNull(response.getMetadata(MetadataSample.class).getTransactionId());
	}

	// Helpers

	private void givenAuthResponse() {
		if (!authTokenGenerated) {
			givenResponse(generateMockAuth(10));
			authTokenGenerated = true;
		}
	}

}
