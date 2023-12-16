package com.github.switcherapi.client;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.exception.SwitcherRemoteException;
import com.github.switcherapi.client.exception.SwitcherSnapshotWriteException;
import com.github.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SwitcherSnapshotValidationTest extends MockWebServerHelper {

	private static final String RESOURCES_PATH = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources";
	
	@BeforeAll
	static void setup() throws IOException {
		Files.deleteIfExists(Paths.get(RESOURCES_PATH + "/not_accessible"));
		Files.deleteIfExists(Paths.get(RESOURCES_PATH + "/new_folder/generated_on_new_folder.json"));
		Files.deleteIfExists(Paths.get(RESOURCES_PATH + "/new_folder"));
		Files.deleteIfExists(Paths.get(RESOURCES_PATH + "/generated_mock_default.json"));

		MockWebServerHelper.setupMockServer();
        
        Switchers.loadProperties();
        Switchers.configure(ContextBuilder.builder().url(String.format("http://localhost:%s", mockBackEnd.getPort())));
        Switchers.initializeClient();
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
				.environment("default")
				.silentMode(null)
				.snapshotAutoLoad(false)
				.snapshotAutoUpdateInterval(null));
		
		Switchers.initializeClient();
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
	
	@Test
	@Order(value = 1)
	void shouldNotLookupForSnapshot_invalidLocation() {
		//given
		Switchers.configure(ContextBuilder.builder()
				.snapshotAutoLoad(true)
				.snapshotLocation(RESOURCES_PATH + "/not_accessible"));
		
		//test
		assertDoesNotThrow(() -> {
			try (final RandomAccessFile raFile = 
					new RandomAccessFile(RESOURCES_PATH + "/not_accessible", "rw")) {
				
				//given an inaccessible folder
				raFile.getChannel().lock();
				
				//auth
				givenResponse(generateMockAuth(10));
				
				//graphql
				givenResponse(generateSnapshotResponse(RESOURCES_PATH));
				
				//test
				assertThrows(SwitcherSnapshotWriteException.class, Switchers::initializeClient);
			}
		});
	}
	
	@Test
	@Order(value = 2)
	void shouldNotLookupForSnapshot_invalidFolderLocation() {
		//given
		Switchers.configure(ContextBuilder.builder()
				.snapshotAutoLoad(true)
				.snapshotLocation(RESOURCES_PATH + "/not_accessible/folder"));
		
		//test
		assertDoesNotThrow(() -> {
			try (final RandomAccessFile raFile = 
					new RandomAccessFile(RESOURCES_PATH + "/not_accessible", "rw")) {
				
				//given an inaccessible folder
				raFile.getChannel().lock();
				
				//auth
				givenResponse(generateMockAuth(10));
				
				//graphql
				givenResponse(generateSnapshotResponse(RESOURCES_PATH));
				
				//test
				assertThrows(SwitcherSnapshotWriteException.class, Switchers::initializeClient);
			}
		});
	}

}
