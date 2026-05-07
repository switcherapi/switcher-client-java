package com.switcherapi.client;

import com.switcherapi.Switchers;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

class SwitcherSnapshotAutoUpdate2Test extends MockWebServerHelper {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources/update";

	@BeforeAll
	static void setup() throws IOException {
        setupMockServer();
        Switchers.loadProperties();
    }

	@AfterAll
	static void tearDown() {
		tearDownMockServer();
		SwitcherContextBase.terminateSnapshotAutoUpdateWorker();
    }

	@BeforeEach
	void restoreStubs() {
		((QueueDispatcher) mockBackEnd.getDispatcher()).clear();
		SwitcherContextBase.terminateSnapshotAutoUpdateWorker();

		CountDownHelper.wait(1);
	}

	@Test
	void shouldNotKillThread_whenAPI_wentLocal() {
		//given
		givenResponse(generateMockAuth(10)); //auth
		givenResponse(generateSnapshotResponse("default_outdated.json", SNAPSHOTS_LOCAL)); //graphql

		//that
		Switchers.configure(ContextBuilder.builder(true)
				.context(Switchers.class.getName())
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.apiKey("[API_KEY]")
				.environment("generated_mock_default_6")
				.local(true)
				.snapshotAutoLoad(true)
				.snapshotAutoUpdateInterval("1s"));

		Switchers.initializeClient();
		assertEquals(1, Switchers.getSnapshotVersion());

		CountDownHelper.wait(1);

		//given - API is remote again
		givenResponse(generateCheckSnapshotVersionResponse(Boolean.toString(false))); //criteria/snapshot_check
		givenResponse(generateSnapshotResponse("default.json", SNAPSHOTS_LOCAL)); //graphql

		//test
		CountDownHelper.wait(2);
		assertEquals(2, Switchers.getSnapshotVersion());
	}

}
