package com.github.switcherapi.client;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwitcherApiMock3Test extends MockWebServerHelper {

	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources/snapshot";
	
	@BeforeAll
	static void setup() throws IOException {
		MockWebServerHelper.setupMockServer();

		Switchers.loadProperties(); // Load default properties from resources
		Switchers.initializeClient(); // SwitcherContext requires preload before config override
		Switchers.configure(ContextBuilder.builder() // Override default properties
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.local(true)
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.snapshotSkipValidation(false)
				.environment("fixture1"));

        Switchers.initializeClient();
    }
	
	@AfterAll
	static void tearDown() throws IOException {
		MockWebServerHelper.tearDownMockServer();
    }
	
	@BeforeEach
	void resetSwitcherContextState() {
		((QueueDispatcher) mockBackEnd.getDispatcher()).clear();
	}

	@Test
	void shouldResolveLocally() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE11);
		assertTrue(switcher.remote(false).isItOn());
	}
	
	@Test
	void shouldForceResolveRemotely() {
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria
		givenResponse(generateCriteriaResponse("false", false));
		
		//test
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE11);
		assertFalse(switcher.remote(true).isItOn());
	}

}
