package com.github.switcherapi.client;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.StrategyValidator;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.response.CriteriaResponse;
import com.github.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SwitcherExecutionReasonTest extends MockWebServerHelper {
	
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
	void shouldHideExecutionReason() {
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria
		givenResponse(generateCriteriaResponse("true", false));
		
		//given
		List<Entry> entries = new ArrayList<>();
		entries.add(Entry.build(StrategyValidator.DATE, "2019-12-10"));
		
		Switcher switcher = Switchers.getSwitcher(Switchers.REMOTE_KEY);
		
		//test
		assertTrue(switcher.isItOn(entries));
		assertNotNull(switcher.getHistoryExecution());
		assertNull(switcher.getHistoryExecution().stream().findFirst().orElseGet(CriteriaResponse::new).getReason());
	}
	
	@Test
	void shouldShowExecutionReason() {
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria
		givenResponse(generateCriteriaResponse("true", true));
				
		//given
		List<Entry> entries = new ArrayList<>();
		entries.add(Entry.build(StrategyValidator.DATE, "2019-12-10"));
		
		Switcher switcher = Switchers.getSwitcher(Switchers.REMOTE_KEY);
		switcher.setShowReason(true);
		
		//test
		assertTrue(switcher.isItOn(entries));
		assertNotNull(switcher.getHistoryExecution());
		assertNotNull(switcher.getHistoryExecution().stream().findFirst().orElseGet(CriteriaResponse::new).getReason());
		assertEquals(switcher.getSwitcherKey(),
				switcher.getHistoryExecution().stream().findFirst().orElseGet(CriteriaResponse::new).getSwitcherKey());
	}

}
