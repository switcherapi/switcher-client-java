package com.github.switcherapi.client;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.exception.SwitcherRemoteException;
import com.github.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class SwitcherSnapshotValidationTest extends MockWebServerHelper {

	private static final String RESOURCES_PATH = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources";
	
	@BeforeAll
	static void setup() throws IOException {
		MockWebServerHelper.setupMockServer();
        
        Switchers.loadProperties();
        Switchers.configure(ContextBuilder.builder().url(String.format("http://localhost:%s", mockBackEnd.getPort())));
    }
	
	@AfterAll
	static void tearDown() throws IOException {
		MockWebServerHelper.tearDownMockServer();
        
        //clean generated outputs
    	SwitcherContext.stopWatchingSnapshot();
    }
	
	@BeforeEach
	void resetSwitcherContextState() {
		((QueueDispatcher) mockBackEnd.getDispatcher()).clear();

		Switchers.configure(ContextBuilder.builder()
				.local(false)
				.snapshotLocation(null)
				.snapshotSkipValidation(false)
				.environment("default")
				.silentMode(null)
				.snapshotAutoLoad(false)
				.snapshotAutoUpdateInterval(null));
	}

	@Test
	void shouldValidateAndUpdateSnapshot() {
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria/snapshot_check
		givenResponse(generateCheckSnapshotVersionResponse("false"));
		
		//graphql
		givenResponse(generateSnapshotResponse(RESOURCES_PATH));
		
		//test
		Switchers.configure(ContextBuilder.builder().snapshotLocation(RESOURCES_PATH));
		
		assertDoesNotThrow(() -> {
			Switchers.initializeClient();
			assertTrue(Switchers.validateSnapshot());
		});
	}

	@Test
	void shouldValidateAndNotUpdateSnapshot() {
		//auth
		givenResponse(generateMockAuth(10));

		//criteria/snapshot_check
		givenResponse(generateCheckSnapshotVersionResponse("true"));

		//test
		Switchers.configure(ContextBuilder.builder().snapshotLocation(RESOURCES_PATH));

		assertDoesNotThrow(() -> {
			Switchers.initializeClient();
			assertFalse(Switchers.validateSnapshot());
		});
	}

	@Test
	void shouldSkipValidateSnapshot() {
		//given
		Switchers.configure(ContextBuilder.builder().snapshotSkipValidation(true));

		//test
		assertDoesNotThrow(() -> {
			Switchers.initializeClient();
			assertFalse(Switchers.validateSnapshot());
		});
	}
	
	@Test
	void shouldValidateAndLoadSnapshot_whenLocal() {
		//given
		Switchers.configure(ContextBuilder.builder()
				.local(true)
				.snapshotAutoLoad(false)
				.snapshotLocation(RESOURCES_PATH)
				.environment("default"));
		
		Switchers.initializeClient();
		
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria/snapshot_check
		givenResponse(generateCheckSnapshotVersionResponse("false"));
		
		//graphql
		givenResponse(generateSnapshotResponse(RESOURCES_PATH));
		
		//test
		assertDoesNotThrow(Switchers::validateSnapshot);
	}
	
	@Test
	void shouldNotValidateAndLoadSnapshot_serviceUnavailable() {
		//given
		Switchers.configure(ContextBuilder.builder()
				.local(true)
				.snapshotAutoLoad(false)
				.snapshotLocation(RESOURCES_PATH)
				.environment("default"));
		
		Switchers.initializeClient();
		
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria/snapshot_check
		givenResponse(generateStatusResponse("503"));
		
		//test
		assertThrows(SwitcherRemoteException.class, Switchers::validateSnapshot);
	}

}
