package com.github.switcherapi.client;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.SwitchersBase;
import com.github.switcherapi.client.exception.SwitcherRemoteException;
import com.github.switcherapi.client.model.SwitcherRequest;
import com.github.switcherapi.client.service.WorkerName;
import com.github.switcherapi.fixture.MockWebServerHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SwitcherContextRemoteExecutorTest extends MockWebServerHelper {

	@BeforeAll
	static void setup() throws IOException {
		MockWebServerHelper.setupMockServer();
	}

	@AfterAll
	static void tearDown() throws IOException {
		MockWebServerHelper.tearDownMockServer();
	}

	@Test
	void shouldConfigureRemotePoolSize() {
		//auth
		givenResponse(generateMockAuth(10));

		//criteria
		givenResponse(generateCriteriaResponse("true", false));

		//given
		SwitchersBase.configure(ContextBuilder.builder(true)
				.context(SwitchersBase.class.getCanonicalName())
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.apiKey("API_KEY")
				.domain("switcher-domain")
				.component("switcher-client-pool-test")
				.environment("default")
				.poolConnectionSize(1)
				.local(false));

		SwitchersBase.initializeClient();

		//test
		SwitcherRequest switcher = SwitchersBase.getSwitcher(Switchers.USECASE11);
		assertTrue(switcher.isItOn());

		//assert pool size
		long count = Thread.getAllStackTraces().keySet().stream().filter(thread ->
				thread.getName().contains(String.format("%s-%s", WorkerName.SWITCHER_REMOTE_WORKER, "switcher-client-pool-test"))).count();
		assertEquals(1, count);
	}

	@Test
	void shouldConfigureRemoteTimeout() {
		//auth
		givenResponse(generateMockAuth(10));

		//criteria
		givenResponse(generateTimeOut(1000));

		//given
		SwitchersBase.configure(ContextBuilder.builder(true)
				.context(SwitchersBase.class.getCanonicalName())
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.apiKey("API_KEY")
				.domain("switcher-domain")
				.component("switcher-client-timeout-test")
				.environment("default")
				.timeoutMs(500)
				.local(false));

		SwitchersBase.initializeClient();

		//test
		SwitcherRequest switcher = SwitchersBase.getSwitcher(Switchers.USECASE11);
		Exception ex = assertThrows(SwitcherRemoteException.class, switcher::isItOn);
		assertEquals("request timed out", ex.getCause().getMessage());
	}

}
