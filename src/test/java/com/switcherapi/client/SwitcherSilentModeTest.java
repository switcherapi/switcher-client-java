package com.switcherapi.client;

import com.switcherapi.SwitchersBase;
import com.switcherapi.client.model.SwitcherRequest;
import com.switcherapi.fixture.CountDownHelper;
import com.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static com.switcherapi.client.remote.Constants.DEFAULT_ENV;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwitcherSilentModeTest extends MockWebServerHelper {

	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources/snapshot";
	
	@BeforeAll
	static void setup() throws IOException {
		setupMockServer();
    }
	
	@AfterAll
	static void tearDown() {
		tearDownMockServer();
    }
	
	@BeforeEach
	void resetSwitcherContextState() {
		((QueueDispatcher) mockBackEnd.getDispatcher()).clear();

		SwitchersBase.configure(ContextBuilder.builder(true)
				.context(SwitchersBase.class.getName())
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.domain("domain")
				.apiKey("apiKey")
				.component("component")
				.environment(DEFAULT_ENV));
	}
	
	@Test
	void shouldReturnTrue_silentMode() {
		//given
		SwitchersBase.configure(ContextBuilder.builder()
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.environment("fixture1")
				.silentMode("5s"));

		SwitchersBase.initializeClient();
		
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria
		givenResponse(generateCriteriaResponse("true", false));
		
		//test
		SwitcherRequest switcher = SwitchersBase.getSwitcher(SwitchersBase.USECASE11);
		assertTrue(switcher.isItOn());

		CountDownHelper.wait(2);
		
		//isAlive - service unavailable
		givenResponse(generateStatusResponse("503"));
		
		//test will trigger silent
		assertTrue(switcher.isItOn());
		
		//test will use silent (read from snapshot)
		assertTrue(switcher.isItOn());
	}

}
