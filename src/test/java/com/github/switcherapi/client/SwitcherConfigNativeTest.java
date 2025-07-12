package com.github.switcherapi.client;

import com.github.switcherapi.SwitchersBaseNative;
import com.github.switcherapi.fixture.MockWebServerHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SwitcherConfigNativeTest extends MockWebServerHelper {

	@BeforeAll
	static void setup() throws IOException {
		MockWebServerHelper.setupMockServer();
	}

	@AfterAll
	static void tearDown() {
		MockWebServerHelper.tearDownMockServer();
	}

	@Test
	void shouldUseNativeContext() {
		SwitchersBaseNative context = SwitchersBaseNative.buildSwitcherClientConfigMinimal(String.format("http://localhost:%s", mockBackEnd.getPort()));
		context.configureClient();

		givenResponse(generateMockAuth(10));  //auth
		givenResponse(generateCriteriaResponse("true", false)); //criteria

		assertTrue(SwitchersBaseNative.getSwitcher(SwitchersBaseNative.USECASE11).isItOn());
	}

	@Test
	void shouldUseNativeAndValidateSwitchers() {
		SwitchersBaseNative context = SwitchersBaseNative.buildSwitcherClientConfigMinimal(String.format("http://localhost:%s", mockBackEnd.getPort()));
		context.configureClient();

		final Set<String> notFound = new HashSet<>();
		givenResponse(generateMockAuth(10)); //auth
		givenResponse(generateCheckSwitchersResponse(notFound)); //criteria/check_switchers

		assertDoesNotThrow(SwitchersBaseNative::checkSwitchers);
	}

	@Test
	void shouldUseNativeContextFromProperties() {
		SwitchersBaseNative context = new SwitchersBaseNative();
		context.registerSwitcherKeys(SwitchersBaseNative.USECASE11);
		context.configureClient("switcherapi-native");

		assertTrue(SwitchersBaseNative.getSwitcher(SwitchersBaseNative.USECASE11).isItOn());
		assertEquals("switcher-client", context.component);
		assertEquals("switcher-domain", context.domain);
		assertEquals("[API_KEY]", context.apikey);
		assertEquals("http://localhost:3000", context.url);
		assertEquals("fixture1", context.environment);
	}

}
