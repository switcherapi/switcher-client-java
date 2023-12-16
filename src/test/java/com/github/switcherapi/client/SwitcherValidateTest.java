package com.github.switcherapi.client;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.exception.SwitchersValidationException;
import com.github.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.glassfish.jersey.internal.guava.Sets;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SwitcherValidateTest extends MockWebServerHelper {

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
	void shouldValidateSwitchers() {
		//given
		final Set<String> notFound = Sets.newHashSet();
		
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria/check_switchers
		givenResponse(generateCheckSwitchersResponse(notFound));
		
		//test
		assertDoesNotThrow(Switchers::checkSwitchers);
	}
	
	@Test
	void shouldValidateSwitchers_notConfiguredSwitcherBeingUsed() {
		//given
		final Set<String> notFound = Sets.newHashSet();
		notFound.add("NOT_FOUND_1");
		
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria/check_switchers
		givenResponse(generateCheckSwitchersResponse(notFound));
		
		//test
		Exception ex = assertThrows(SwitchersValidationException.class,
				Switchers::checkSwitchers);
		
		assertEquals(String.format(
				"Something went wrong: Unable to load the following Switcher Key(s): %s", notFound), 
				ex.getMessage());
	}

}
