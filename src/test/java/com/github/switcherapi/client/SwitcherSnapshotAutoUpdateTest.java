package com.github.switcherapi.client;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.model.criteria.Criteria;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.github.switcherapi.client.remote.ClientWSImpl;
import com.github.switcherapi.client.utils.SnapshotLoader;
import com.github.switcherapi.client.utils.SwitcherUtils;
import com.google.gson.Gson;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SwitcherSnapshotAutoUpdateTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources";

	private static MockWebServer mockBackEnd;

	@BeforeAll
	static void setup() throws IOException {
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "/generated_mock_default_2.json"));

        mockBackEnd = new MockWebServer();
        mockBackEnd.start();

        Switchers.loadProperties();
    }

	@AfterAll
	static void tearDown() throws IOException, InterruptedException {
        mockBackEnd.shutdown();

        //clean generated outputs
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "/generated_mock_default_2.json"));

		//time to release resources
		CountDownLatch waiter = new CountDownLatch(1);
		waiter.await(10, TimeUnit.SECONDS);
    }

	@BeforeEach
	void resetSwitcherContextState() {
		generateFixture();
		SwitcherContextBase.terminateSnapshotAutoUpdateWorker();
	}

	static void generateFixture() {
		final Snapshot mockedSnapshot = new Snapshot();
		final Criteria criteria = new Criteria();
		criteria.setDomain(SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/snapshot_auto_update.json"));
		mockedSnapshot.setData(criteria);

		SnapshotLoader.saveSnapshot(mockedSnapshot, SNAPSHOTS_LOCAL, "generated_mock_default_2");
	}

	/**
	 * @return Generated mock /auth response
	 * @see ClientWSImpl#auth()
	 */
	private MockResponse generateMockAuth() {
		return new MockResponse()
				.setBody(String.format("{ \"token\": \"%s\", \"exp\": \"%s\" }",
						"mocked_token", SwitcherUtils.addTimeDuration(10 + "s", new Date()).getTime()/1000))
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
	private MockResponse generateSnapshotResponse() {
		final Snapshot mockedSnapshot = new Snapshot();
		final Criteria criteria = new Criteria();
		criteria.setDomain(SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/default.json"));
		mockedSnapshot.setData(criteria);

		Gson gson = new Gson();
		return new MockResponse()
				.setBody(gson.toJson(mockedSnapshot))
				.addHeader("Content-Type", "application/json");
	}

	private void givenSnapshotUpdateResponse(boolean isUpdated) {
		//auth
		mockBackEnd.enqueue(generateMockAuth());

		//criteria/snapshot_check
		mockBackEnd.enqueue(generateCheckSnapshotVersionResponse(Boolean.toString(isUpdated)));

		if (!isUpdated) {
			//auth isAlive
			mockBackEnd.enqueue(generateMockAuth());

			//graphql
			mockBackEnd.enqueue(generateSnapshotResponse());
		}
	}

	@Test
	@Order(1)
	void shouldUpdateSnapshot() throws InterruptedException {
		//given
		givenSnapshotUpdateResponse(false);

		//that
		Switchers.configure(ContextBuilder.builder()
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.environment("generated_mock_default_2")
				.offlineMode(true)
				.snapshotAutoLoad(false)
				.snapshotAutoUpdateInterval("1s"));

		assertDoesNotThrow(Switchers::initializeClient);
		assertEquals(1, Switchers.getSnapshotVersion());

		//test
		CountDownLatch waiter = new CountDownLatch(1);
		waiter.await(2, TimeUnit.SECONDS);
		assertEquals(1588557288037L, Switchers.getSnapshotVersion());
	}

	@Test
	@Order(2)
	void shouldNotUpdateSnapshot_whenNoUpdateAvailable() throws InterruptedException {
		//given
		givenSnapshotUpdateResponse(true);

		//that
		Switchers.configure(ContextBuilder.builder()
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.environment("generated_mock_default_2")
				.offlineMode(true)
				.snapshotAutoLoad(true)
				.snapshotAutoUpdateInterval("1s"));

		assertDoesNotThrow(Switchers::initializeClient);
		assertEquals(1, Switchers.getSnapshotVersion());

		//test - still the same version
		CountDownLatch waiter = new CountDownLatch(1);
		waiter.await(2, TimeUnit.SECONDS);
		assertEquals(1, Switchers.getSnapshotVersion());
	}

}
