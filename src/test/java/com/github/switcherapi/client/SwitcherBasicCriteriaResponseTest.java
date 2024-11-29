package com.github.switcherapi.client;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.model.SwitcherRequest;
import com.github.switcherapi.client.model.SwitcherResult;
import com.github.switcherapi.fixture.MetadataErrorSample;
import com.github.switcherapi.fixture.MetadataSample;
import com.github.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SwitcherBasicCriteriaResponseTest extends MockWebServerHelper {

	private boolean authTokenGenerated = false;
	
	@BeforeAll
	static void setup() throws IOException {
		MockWebServerHelper.setupMockServer();

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
	void shouldReturnCriteriaResponse() {
		//auth
		givenAuthResponse();

		//criteria
		givenResponse(generateCriteriaResponse("true", "Success"));

		//test
		SwitcherRequest switcher = Switchers.getSwitcher(Switchers.REMOTE_KEY);
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
		SwitcherRequest switcher = Switchers.getSwitcher(Switchers.REMOTE_KEY);
		SwitcherResult response = switcher
				.checkValue("value")
				.checkNumeric("10")
				.submit();

		assertFalse(response.isItOn());
		assertEquals("Strategy VALUE_VALIDATION does not agree", response.getReason());
	}

	@Test
	void shouldReturnCriteriaResponseWithMetadata() {
		//auth
		givenAuthResponse();

		//criteria
		givenResponse(generateCriteriaResponse(new MetadataSample("123")));

		//test
		SwitcherRequest switcher = Switchers.getSwitcher(Switchers.REMOTE_KEY);
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
		SwitcherRequest switcher = Switchers.getSwitcher(Switchers.REMOTE_KEY);
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
