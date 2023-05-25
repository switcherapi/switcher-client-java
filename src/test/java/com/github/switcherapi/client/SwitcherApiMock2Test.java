package com.github.switcherapi.client;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.exception.SwitcherRemoteException;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.remote.ClientWSImpl;
import com.github.switcherapi.client.service.remote.ClientRemoteService;
import com.github.switcherapi.client.utils.SwitcherUtils;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.QueueDispatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SwitcherApiMock2Test {
	
	private static MockWebServer mockBackEnd;
	
	@BeforeAll
	static void setup() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
		((QueueDispatcher) mockBackEnd.getDispatcher()).setFailFast(true);
        
        Switchers.loadProperties();
        Switchers.configure(ContextBuilder.builder().url(String.format("http://localhost:%s", mockBackEnd.getPort())));
        Switchers.initializeClient();
    }
	
	@AfterAll
	static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }
	
	@BeforeEach
	void resetSwitcherContextState() {
		((QueueDispatcher) mockBackEnd.getDispatcher()).clear();
		ClientRemoteService.getInstance().clearAuthResponse();
		
		Switchers.configure(ContextBuilder.builder()
				.offlineMode(false)
				.snapshotLocation(null)
				.snapshotSkipValidation(false)
				.environment("default")
				.silentMode(false)
				.snapshotAutoLoad(false)
				.snapshotAutoUpdateInterval(null)
				.retryAfter(null));
		
		Switchers.initializeClient();
	}
	
	/**
	 * @see ClientWSImpl#auth()
	 * 
	 * @param secondsAhead time to expire the token
	 * @return Generated mock /auth response
	 */
	private MockResponse generateMockAuth(int secondsAhead) {
		return new MockResponse()
				.setBody(String.format("{ \"token\": \"%s\", \"exp\": \"%s\" }", 
						"mocked_token", SwitcherUtils.addTimeDuration(secondsAhead + "s", new Date()).getTime()/1000))
				.addHeader("Content-Type", "application/json");
	}
	
	/**
	 * @see ClientWSImpl#isAlive()
	 * 
	 * @param code HTTP status
	 * @return Generated mock /check response
	 */
	private MockResponse generateStatusResponse(String code) {
		return new MockResponse().setStatus(String.format("HTTP/1.1 %s", code));
	
	}

	private void givenResponse(MockResponse response) {
		((QueueDispatcher) mockBackEnd.getDispatcher()).enqueueResponse(response);
	}

	@Test
	void shouldReturnError_componentNotRegistered() {
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria
		givenResponse(generateStatusResponse("401"));
		
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		assertThrows(SwitcherRemoteException.class, switcher::isItOn);
	}

}
