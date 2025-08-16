package com.switcherapi.client;

import com.switcherapi.Switchers;
import com.switcherapi.client.model.criteria.Data;
import com.switcherapi.client.model.criteria.Domain;
import com.switcherapi.client.model.criteria.Snapshot;
import com.switcherapi.client.utils.SnapshotLoader;
import com.switcherapi.fixture.CountDownHelper;
import com.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SwitcherSnapshotAutoUpdateTest extends MockWebServerHelper {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources/update";
	private static final Domain DOMAIN_OUTDATED = SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/default_outdated.json");

	@BeforeAll
	static void setup() throws IOException {
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "/generated_mock_default_2.json"));
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "/generated_mock_default_3.json"));
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "/generated_mock_default_4.json"));

		generateFixture("generated_mock_default_2");
		generateFixture("generated_mock_default_3");
		generateFixture("generated_mock_default_4");

        setupMockServer();
        Switchers.loadProperties();
    }

	@AfterAll
	static void tearDown() throws IOException {
		tearDownMockServer();

        //clean generated outputs
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "/generated_mock_default_2.json"));
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "/generated_mock_default_3.json"));
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "/generated_mock_default_4.json"));

		SwitcherContextBase.terminateSnapshotAutoUpdateWorker();
    }

	@BeforeEach
	void restoreStubs() {
		((QueueDispatcher) mockBackEnd.getDispatcher()).clear();
		SwitcherContextBase.terminateSnapshotAutoUpdateWorker();

		CountDownHelper.wait(1);
	}

	static void generateFixture(String environment) {
		final Snapshot mockedSnapshot = new Snapshot();
		final Data data = new Data();
		data.setDomain(DOMAIN_OUTDATED);
		mockedSnapshot.setData(data);

		SnapshotLoader.saveSnapshot(mockedSnapshot, SNAPSHOTS_LOCAL, environment);
	}

	private void givenSnapshotUpdateResponse(boolean isUpdated) {
		//auth
		givenResponse(generateMockAuth(10));

		//criteria/snapshot_check
		givenResponse(generateCheckSnapshotVersionResponse(Boolean.toString(isUpdated)));

		if (!isUpdated) {
			//graphql
			givenResponse(generateSnapshotResponse("default.json", SNAPSHOTS_LOCAL));
		}
	}

	@Test
	@Order(1)
	void shouldUpdateSnapshot_local() {
		//given
		givenSnapshotUpdateResponse(false);

		//that
		Switchers.configure(ContextBuilder.builder(true)
				.context(Switchers.class.getCanonicalName())
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.apiKey("[API_KEY]")
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.environment("generated_mock_default_2")
				.local(true)
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
	void shouldUpdateSnapshot_remote() {
		//given
		givenSnapshotUpdateResponse(false);

		//that
		Switchers.configure(ContextBuilder.builder(true)
				.context(Switchers.class.getCanonicalName())
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.apiKey("[API_KEY]")
				.domain("Test")
				.component("switcher-test")
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.environment("generated_mock_default_3")
				.local(false)
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
		Switchers.configure(ContextBuilder.builder(true)
				.context(Switchers.class.getCanonicalName())
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.apiKey("[API_KEY]")
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.environment("generated_mock_default_4")
				.local(true)
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
	void shouldUpdateSnapshot_remote_inMemory() {
		//given
		givenResponse(generateMockAuth(10)); //auth
		givenResponse(generateSnapshotResponse("default_outdated.json", SNAPSHOTS_LOCAL)); //graphql
		givenResponse(generateCheckSnapshotVersionResponse(Boolean.toString(false))); //criteria/snapshot_check
		givenResponse(generateSnapshotResponse("default.json", SNAPSHOTS_LOCAL)); //graphql

		//that
		Switchers.configure(ContextBuilder.builder(true)
				.context(Switchers.class.getCanonicalName())
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.apiKey("[API_KEY]")
				.environment("generated_mock_default_5")
				.local(true)
				.snapshotLocation("")
				.snapshotAutoLoad(true)
				.snapshotAutoUpdateInterval("1m"));

		Switchers.initializeClient();
		assertEquals(1, Switchers.getSnapshotVersion());

		//test
		CountDownHelper.wait(2);
		assertEquals(2, Switchers.getSnapshotVersion());
		assertTrue(Files.notExists(Paths.get("/generated_mock_default_5.json")));
	}

	@Test
	@Order(5)
	void shouldNotKillThread_whenAPI_wentLocal() {
		//given
		givenResponse(generateMockAuth(10)); //auth
		givenResponse(generateSnapshotResponse("default_outdated.json", SNAPSHOTS_LOCAL)); //graphql

		//that
		Switchers.configure(ContextBuilder.builder(true)
				.context(Switchers.class.getCanonicalName())
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

	@Test
	@Order(6)
	void shouldRestartSnapshotAutoUpdate_whenAlreadySetup() {
		//given - initialize (snapshot autoload)
		givenResponse(generateMockAuth(10)); //auth
		givenResponse(generateSnapshotResponse("default.json", SNAPSHOTS_LOCAL)); //graphql

		//given - snapshot auto update
		givenResponse(generateCheckSnapshotVersionResponse(Boolean.toString(false))); //criteria/snapshot_check
		givenResponse(generateSnapshotResponse("default.json", SNAPSHOTS_LOCAL)); //graphql

		//that
		Switchers.configure(ContextBuilder.builder(true)
				.context(Switchers.class.getCanonicalName())
				.url(String.format("http://localhost:%s", mockBackEnd.getPort()))
				.apiKey("[API_KEY]")
				.environment("generated_mock_default_6")
				.local(true)
				.snapshotAutoLoad(true)
				.snapshotAutoUpdateInterval("1s"));

		Switchers.initializeClient();

		ScheduledFuture<?> snapshotUpdater = Switchers.scheduleSnapshotAutoUpdate("1m");
		assertNotNull(snapshotUpdater);
	}

}
