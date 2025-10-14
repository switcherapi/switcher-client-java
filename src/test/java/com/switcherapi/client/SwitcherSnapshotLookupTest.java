package com.switcherapi.client;

import com.switcherapi.Switchers;
import com.switcherapi.client.exception.SwitcherRemoteException;
import com.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.switcherapi.client.remote.Constants.DEFAULT_ENV;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SwitcherSnapshotLookupTest extends MockWebServerHelper {

	private static final String RESOURCES_PATH = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources";

	@BeforeAll
	static void setup() throws IOException {
		Files.deleteIfExists(Paths.get(RESOURCES_PATH + "/new_folder/generated_on_new_folder.json"));
		Files.deleteIfExists(Paths.get(RESOURCES_PATH + "/new_folder"));
		Files.deleteIfExists(Paths.get(RESOURCES_PATH + "/generated_mock_default.json"));

		MockWebServerHelper.setupMockServer();

		Switchers.loadProperties();
		Switchers.configure(ContextBuilder.builder().url(String.format("http://localhost:%s", mockBackEnd.getPort())));
	}

	@AfterAll
	static void tearDown() throws IOException {
		MockWebServerHelper.tearDownMockServer();

		//clean generated outputs
		SwitcherContext.stopWatchingSnapshot();
		Files.deleteIfExists(Paths.get(RESOURCES_PATH + "/new_folder/generated_on_new_folder.json"));
		Files.deleteIfExists(Paths.get(RESOURCES_PATH + "/new_folder"));
		Files.deleteIfExists(Paths.get(RESOURCES_PATH + "/generated_mock_default.json"));
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
	}

	@Test
	void shouldLookupForSnapshot() {
		//given
		Switchers.configure(ContextBuilder.builder()
				.snapshotAutoLoad(true)
				.snapshotLocation(RESOURCES_PATH + "/new_folder")
				.environment("generated_on_new_folder"));

		//auth
		givenResponse(generateMockAuth(10));

		//graphql
		givenResponse(generateSnapshotResponse(RESOURCES_PATH));

		//test
		assertDoesNotThrow(Switchers::initializeClient);
	}

	@Test
	void shouldLookupForSnapshot_whenNotAutoLoad() {
		//given
		Switchers.configure(ContextBuilder.builder()
				.snapshotAutoLoad(false)
				.snapshotLocation(RESOURCES_PATH)
				.environment("generated_mock_default"));

		Switchers.initializeClient();

		//auth
		givenResponse(generateMockAuth(10));

		//graphql
		givenResponse(generateSnapshotResponse(RESOURCES_PATH));

		//test
		assertDoesNotThrow(Switchers::validateSnapshot);
	}

	@Test
	void shouldNotLookupForSnapshot_serviceUnavailable() {
		//given
		Switchers.configure(ContextBuilder.builder()
				.snapshotAutoLoad(true)
				.snapshotLocation(RESOURCES_PATH + "/new_folder")
				.environment("generated_on_new_folder"));

		//auth
		givenResponse(generateMockAuth(10));

		//graphql
		givenResponse(generateStatusResponse("503"));

		//test
		assertThrows(SwitcherRemoteException.class, Switchers::initializeClient);
	}

}
