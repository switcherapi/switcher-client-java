package com.github.switcherapi.client;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.model.criteria.Criteria;
import com.github.switcherapi.client.model.criteria.Domain;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.github.switcherapi.client.remote.ClientWSImpl;
import com.github.switcherapi.client.utils.SnapshotLoader;
import com.github.switcherapi.client.utils.SwitcherUtils;
import com.github.switcherapi.fixture.CountDownHelper;
import com.google.gson.Gson;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.QueueDispatcher;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SwitcherSnapshotAutoUpdateTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources/update";
	private static final Domain DOMAIN_OUTDATED = SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/default_outdated.json");

	private static MockWebServer mockBackEnd;

	@BeforeAll
	static void setup() throws IOException {
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "/generated_mock_default_2.json"));
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "/generated_mock_default_3.json"));
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "/generated_mock_default_4.json"));

		generateFixture("generated_mock_default_2");
		generateFixture("generated_mock_default_3");
		generateFixture("generated_mock_default_4");

        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
		((QueueDispatcher) mockBackEnd.getDispatcher()).setFailFast(true);

        Switchers.loadProperties();
    }

	@AfterAll
	static void tearDown() throws IOException {
        mockBackEnd.shutdown();

        //clean generated outputs
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "/generated_mock_default_2.json"));
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "/generated_mock_default_3.json"));
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "/generated_mock_default_4.json"));

		CountDownHelper.wait(10);
    }

	@BeforeEach
	void resetSwitcherContextState() {
		((QueueDispatcher) mockBackEnd.getDispatcher()).clear();
		SwitcherContextBase.terminateSnapshotAutoUpdateWorker();
	}

	static void generateFixture(String environment) {
		final Snapshot mockedSnapshot = new Snapshot();
		final Criteria criteria = new Criteria();
		criteria.setDomain(DOMAIN_OUTDATED);
		mockedSnapshot.setData(criteria);

		SnapshotLoader.saveSnapshot(mockedSnapshot, SNAPSHOTS_LOCAL, environment);
	}

	/**
	 * @return Generated mock /auth response
	 * @see ClientWSImpl#auth()
	 */
	private MockResponse generateMockAuth() {
		return new MockResponse()
				.setBody(String.format("{ \"token\": \"%s\", \"exp\": \"%s\" }",
						"mocked_token", SwitcherUtils.addTimeDuration(60 + "s", new Date()).getTime()/1000))
				.addHeader("Content-Type", "application/json");
	}

	/**
	 * @see ClientWSImpl#checkSnapshotVersion(long, String)
	 *
	 * @param status is true when snapshot version is updated
	 * @return Generated mock /criteria/snapshot_check response
	 */
	private MockResponse generateCheckSnapshotVersionResponse(String status) {
		return new MockResponse()
				.setBody(String.format("{ \"status\": \"%s\" }", status))
				.addHeader("Content-Type", "application/json");
	}

	/**
	 * @see ClientWSImpl#resolveSnapshot(String)
	 *
	 * @return Generated mock /graphql response based on src/test/resources/default.json
	 */
	private MockResponse generateSnapshotResponse(String fromFile) {
		final Snapshot mockedSnapshot = new Snapshot();
		final Criteria criteria = new Criteria();
		criteria.setDomain(SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/" + fromFile));
		mockedSnapshot.setData(criteria);

		Gson gson = new Gson();
		return new MockResponse()
				.setBody(gson.toJson(mockedSnapshot))
				.addHeader("Content-Type", "application/json");
	}

	private void givenSnapshotUpdateResponse(boolean isUpdated) {
		//auth
		givenResponse(generateMockAuth());

		//criteria/snapshot_check
		givenResponse(generateCheckSnapshotVersionResponse(Boolean.toString(isUpdated)));

		if (!isUpdated) {
			//graphql
			givenResponse(generateSnapshotResponse("default.json"));
		}
	}

	private void givenResponse(MockResponse response) {
		((QueueDispatcher) mockBackEnd.getDispatcher()).enqueueResponse(response);
	}

	@Test
	@Order(1)
	void shouldUpdateSnapshot_offline() {
		//given
		givenSnapshotUpdateResponse(false);

		//that
		Switchers.configure(ContextBuilder.builder()
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.environment("generated_mock_default_2")
				.offlineMode(true)
				.snapshotAutoLoad(false)
				.snapshotAutoUpdateInterval("1m"));

		Switchers.initializeClient();
		assertEquals(1, Switchers.getSnapshotVersion());

		//test
		CountDownHelper.wait(2);
		assertEquals(2, Switchers.getSnapshotVersion());
	}

	@Test
	@Order(2)
	void shouldUpdateSnapshot_online() {
		//given
		givenSnapshotUpdateResponse(false);

		//that
		Switchers.configure(ContextBuilder.builder()
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.environment("generated_mock_default_3")
				.offlineMode(false)
				.snapshotAutoLoad(false)
				.snapshotAutoUpdateInterval("1m"));

		Switchers.initializeClient();
		assertEquals(1, Switchers.getSnapshotVersion());

		//test
		CountDownHelper.wait(2);
		assertEquals(2, Switchers.getSnapshotVersion());
	}

	@Test
	@Order(3)
	void shouldNotUpdateSnapshot_whenNoUpdateAvailable() {
		//given
		givenSnapshotUpdateResponse(true);

		//that
		Switchers.configure(ContextBuilder.builder()
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.environment("generated_mock_default_4")
				.offlineMode(true)
				.snapshotAutoLoad(true)
				.snapshotAutoUpdateInterval("1m"));

		assertDoesNotThrow(Switchers::initializeClient);
		assertEquals(1, Switchers.getSnapshotVersion());

		//test - still the same version
		CountDownHelper.wait(1);
		assertEquals(1, Switchers.getSnapshotVersion());
	}

	@Test
	@Order(4)
	void shouldUpdateSnapshot_online_inMemory() {
		//given
		givenResponse(generateMockAuth()); //auth
		givenResponse(generateSnapshotResponse("default_outdated.json")); //graphql
		givenResponse(generateCheckSnapshotVersionResponse(Boolean.toString(false))); //criteria/snapshot_check
		givenResponse(generateSnapshotResponse("default.json")); //graphql

		//that
		Switchers.configure(ContextBuilder.builder()
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.snapshotLocation(null)
				.environment("generated_mock_default_5")
				.offlineMode(true)
				.snapshotAutoLoad(true)
				.snapshotAutoUpdateInterval("1m"));

		Switchers.initializeClient();
		assertEquals(1, Switchers.getSnapshotVersion());

		//test
		CountDownHelper.wait(2);
		assertEquals(2, Switchers.getSnapshotVersion());
	}

	@Test
	@Order(5)
	void shouldNotKillThread_whenAPI_wentOffline() {
		//given
		givenResponse(generateMockAuth()); //auth
		givenResponse(generateSnapshotResponse("default_outdated.json")); //graphql

		//that
		Switchers.configure(ContextBuilder.builder()
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.snapshotLocation(null)
				.environment("generated_mock_default_5")
				.offlineMode(true)
				.snapshotAutoLoad(true)
				.snapshotAutoUpdateInterval("1s"));

		Switchers.initializeClient();
		assertEquals(1, Switchers.getSnapshotVersion());

		CountDownHelper.wait(1);

		//given - API is online again
		givenResponse(generateCheckSnapshotVersionResponse(Boolean.toString(false))); //criteria/snapshot_check
		givenResponse(generateSnapshotResponse("default.json")); //graphql

		//test
		CountDownHelper.wait(2);
		assertEquals(2, Switchers.getSnapshotVersion());
	}

}
