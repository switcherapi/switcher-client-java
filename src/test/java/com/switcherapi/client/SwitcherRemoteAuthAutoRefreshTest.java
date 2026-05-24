package com.switcherapi.client;

import com.switcherapi.SwitchersBase;
import com.switcherapi.client.exception.SwitcherRemoteException;
import com.switcherapi.client.model.SwitcherRequest;
import com.switcherapi.fixture.CountDownHelper;
import com.switcherapi.fixture.MockWebServerHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwitcherRemoteAuthAutoRefreshTest extends MockWebServerHelper {

	@BeforeEach
	void setup() throws IOException {
		setupMockServer();
	}

	@AfterEach
	void tearDown() {
		tearDownMockServer();
		SwitchersBase.terminateTokenRefreshWorker();
	}

	/**
	 * The scheduled refresh worker operates using a 5s buffer to trigger the refresh before the token expires,
	 * so we need to set the token expiration time accordingly to test the auto-refresh behavior.
	 */
	@Test
	void shouldAutoRefreshAuthToken() {
		//auth - 1st (regular auth) - 2nd (scheduled refresh)
		givenResponse(generateMockAuth(7)); // 5s buffer - 7s exp = 2s for async refresh
		givenResponse(generateCriteriaResponse("true", false));
		givenResponse(generateMockAuth(60));
		givenResponse(generateCriteriaResponse("true", false));

		//when
		givenAuthAutoRefresh(true);

		//test
		SwitcherRequest switcher = SwitchersBase.getSwitcher(SwitchersBase.USECASE11);
		assertTrue(switcher.isItOn());
		CountDownHelper.wait(2);
		assertTrue(switcher.isItOn());
	}

	@Test
	void shouldNotAutoRefreshAuthTokenWhenDisabled() {
		//auth - 1st (regular auth) - 2nd (regular auth)
		givenResponse(generateMockAuth(1));
		givenResponse(generateCriteriaResponse("true", false));
		givenResponse(generateMockAuth(60));
		givenResponse(generateCriteriaResponse("true", false));

		//when
		givenAuthAutoRefresh(false);

		//test
		SwitcherRequest switcher = SwitchersBase.getSwitcher(SwitchersBase.USECASE11);
		assertTrue(switcher.isItOn());
		CountDownHelper.wait(1);
		assertTrue(switcher.isItOn());
	}

	@Test
	void shouldNotAutoRefreshAuthTokenWhenAuthFails() {
		//auth - 1st (regular auth) - 2nd (scheduled refresh)
		givenResponse(generateMockAuth(7)); // 5s buffer - 6s exp = 1s for async refresh
		givenResponse(generateCriteriaResponse("true", false));
		givenResponse(generateStatusResponse("500"));

		//when
		givenAuthAutoRefresh(true);

		//test
		SwitcherRequest switcher = SwitchersBase.getSwitcher(SwitchersBase.USECASE11);
		assertTrue(switcher.isItOn());
		CountDownHelper.wait(2);
		assertThrows(SwitcherRemoteException.class, switcher::isItOn);
	}

	private void givenAuthAutoRefresh(boolean enabled) {
		SwitchersBase.configure(ContextBuilder.builder(true)
				.context(SwitchersBase.class.getName())
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.domain("domain")
				.apiKey("apiKey")
				.component("component")
				.authAutoRefresh(enabled));

		SwitchersBase.initializeClient();
	}
}
