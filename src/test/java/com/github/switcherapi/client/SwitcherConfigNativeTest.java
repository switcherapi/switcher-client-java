package com.github.switcherapi.client;

import com.github.switcherapi.SwitchersBase;
import com.github.switcherapi.fixture.MockWebServerHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SwitcherConfigNativeTest extends MockWebServerHelper {

	@BeforeAll
	static void setup() throws IOException {
		MockWebServerHelper.setupMockServer();
	}

	@AfterAll
	static void tearDown() throws IOException {
		MockWebServerHelper.tearDownMockServer();
	}

	@Test
	void shouldUseNativeContext() {
		SwitchersBase context = buildSwitcherClientConfigMinimal(new SwitchersBase(), String.format("http://localhost:%s", mockBackEnd.getPort()));
		context.configureClient();

		givenResponse(generateMockAuth(10));
		givenResponse(generateCriteriaResponse("true", false));

		assertTrue(SwitchersBase.getSwitcher(SwitchersBase.USECASE11).isItOn());
	}

	@Test
	void shouldUseNativeContextFromProperties() {
		SwitchersBase context = new SwitchersBase();
		context.configureClient("switcherapi-native");

		assertTrue(SwitchersBase.getSwitcher(SwitchersBase.USECASE11).isItOn());
	}

	private <T extends SwitcherConfig> T buildSwitcherClientConfigMinimal(T classConfig, String url) {
		classConfig.setUrl(url);
		classConfig.setApikey("[API-KEY]");
		classConfig.setDomain("domain");
		classConfig.setComponent("component");
		return classConfig;
	}

}
