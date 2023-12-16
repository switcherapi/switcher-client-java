package com.github.switcherapi.client;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.fixture.CountDownHelper;
import com.github.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwitcherBasicTest extends MockWebServerHelper {

	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources/snapshot";
	
	@BeforeAll
	static void setup() throws IOException {
		MockWebServerHelper.setupMockServer();
        
        Switchers.loadProperties();
        Switchers.configure(ContextBuilder.builder().url(String.format("http://localhost:%s", mockBackEnd.getPort())));
        Switchers.initializeClient();
    }
	
	@AfterAll
	static void tearDown() throws IOException {
		MockWebServerHelper.tearDownMockServer();
        
        //clean generated outputs
    	SwitcherContext.stopWatchingSnapshot();
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
		
		Switchers.initializeClient();
	}
	
	@Test
	void shouldReturnTrue() {
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria
		givenResponse(generateCriteriaResponse("true", false));
		
		//test
		Switcher switcher = Switchers.getSwitcher(Switchers.REMOTE_KEY);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void shouldReturnFalse() {
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria
		givenResponse(generateCriteriaResponse("false", false));
		
		//test
		Switcher switcher = Switchers.getSwitcher(Switchers.REMOTE_KEY);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void shouldReturnTrue_silentMode() {
		//given
		Switchers.configure(ContextBuilder.builder()
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.environment("fixture1")
				.silentMode("5s"));
		
		Switchers.initializeClient();
		
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria
		givenResponse(generateCriteriaResponse("true", false));
		
		//test
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE11);
		assertTrue(switcher.isItOn());

		CountDownHelper.wait(2);
		
		//isAlive - service unavailable
		givenResponse(generateStatusResponse("503"));
		
		//test will trigger silent
		assertTrue(switcher.isItOn());
		
		//test will use silent (read from snapshot)
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void shouldReturnTrue_withThrottle() {
		// First call
		givenResponse(generateMockAuth(10)); //auth
		givenResponse(generateCriteriaResponse("true", false)); //criteria
		
		// Async call
		givenResponse(generateMockAuth(10)); //auth
		givenResponse(generateCriteriaResponse("true", false)); //criteria
		
		
		//test
		Switcher switcher = Switchers.getSwitcher(Switchers.REMOTE_KEY);
		switcher.throttle(1000);
		
		for (int i = 0; i < 10; i++) {
			assertTrue(switcher.isItOn());			
		}
		
		// Async call
		givenResponse(generateMockAuth(10)); //auth
		givenResponse(generateCriteriaResponse("true", false)); //criteria

		CountDownHelper.wait(1);
		assertTrue(switcher.isItOn());
	}

}
