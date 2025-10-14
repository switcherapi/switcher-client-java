package com.switcherapi.client;

import com.switcherapi.Switchers;
import com.switcherapi.client.exception.SwitcherException;
import com.switcherapi.client.exception.SwitcherRemoteException;
import com.switcherapi.client.model.SwitcherRequest;
import com.switcherapi.fixture.CountDownHelper;
import com.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.switcherapi.client.remote.Constants.DEFAULT_ENV;
import static org.junit.jupiter.api.Assertions.*;

class SwitcherFail1Test extends MockWebServerHelper {
	
	@BeforeAll
	static void setup() throws IOException {
		MockWebServerHelper.setupMockServer();
        
        Switchers.loadProperties();
        Switchers.configure(ContextBuilder.builder().url(String.format("http://localhost:%s", mockBackEnd.getPort())));
        Switchers.initializeClient();
    }
	
	@AfterAll
	static void tearDown() {
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
				.environment(DEFAULT_ENV)
				.silentMode(null)
				.snapshotAutoLoad(false)
				.snapshotAutoUpdateInterval(null));
		
		Switchers.initializeClient();
	}
	
	@Test
	void shouldReturnError_keyNotFound() {
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria
		givenResponse(generateStatusResponse("404"));
		
		SwitcherRequest switcher = Switchers.getSwitcher(Switchers.REMOTE_KEY);
		assertThrows(SwitcherRemoteException.class, switcher::isItOn);
	}

	@Test
	void shouldReturnSuccessDefaultResult_keyNotFound() {
		//auth
		givenResponse(generateMockAuth(10));

		//criteria
		givenResponse(generateStatusResponse("404"));

		SwitcherRequest switcher = Switchers.getSwitcher(Switchers.REMOTE_KEY);
		assertTrue(switcher.defaultResult(true).isItOn());
		assertFalse(switcher.defaultResult(false).isItOn());
	}
	
	@Test
	void shouldReturnError_unauthorizedAPIAccess() {
		//auth
		givenResponse(generateStatusResponse("401"));
		
		SwitcherRequest switcher = Switchers.getSwitcher(Switchers.REMOTE_KEY);
		assertThrows(SwitcherException.class, switcher::isItOn);
	}
	
	@Test
	void shouldReturnTrue_tokenExpired() {
		//auth
		givenResponse(generateMockAuth(2));
		
		//criteria
		givenResponse(generateCriteriaResponse("true", false));
		
		SwitcherRequest switcher = Switchers.getSwitcher(Switchers.REMOTE_KEY);
		
		//test
		assertTrue(switcher.isItOn());

		CountDownHelper.wait(2);
		
		//auth
		givenResponse(generateMockAuth(2));
		
		//criteria
		givenResponse(generateCriteriaResponse("true", false));
				
		//test
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void shouldNotValidateSwitchers_serviceUnavailable() {
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria/check_switchers
		givenResponse(generateStatusResponse("503"));
		
		//test
		assertThrows(SwitcherRemoteException.class, Switchers::checkSwitchers);
	}

}
