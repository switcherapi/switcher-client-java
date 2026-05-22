package com.switcherapi.client;

import com.switcherapi.SwitchersBase;
import com.switcherapi.client.model.SwitcherRequest;
import com.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwitcherForceResolveTest extends MockWebServerHelper {

	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources/snapshot";
	
	@BeforeAll
	static void setup() throws IOException {
		setupMockServer();

		SwitchersBase.configure(ContextBuilder.builder(true) // Override default properties
				.context(SwitchersBase.class.getName())
				.domain("domain")
				.apiKey("apiKey")
				.component("component")
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.local(true)
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.snapshotSkipValidation(false)
				.snapshotAutoLoad(false)
				.snapshotAutoUpdateInterval(null)
				.environment("fixture1"));

		SwitchersBase.initializeClient();
    }
	
	@AfterAll
	static void tearDown() {
		tearDownMockServer();
    }
	
	@BeforeEach
	void restoreStubs() {
		((QueueDispatcher) mockBackEnd.getDispatcher()).clear();
	}

	@Test
	void shouldResolveLocally() {
		SwitcherRequest switcher = SwitchersBase.getSwitcher(SwitchersBase.USECASE11);
		assertTrue(switcher.remote(false).isItOn());
	}
	
	@Test
	void shouldForceResolveRemotely() {
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria
		givenResponse(generateCriteriaResponse("false", false));
		
		//test
		SwitcherRequest switcher = SwitchersBase.getSwitcher(SwitchersBase.USECASE11);
		assertFalse(switcher.remote(true).isItOn());
	}

}
