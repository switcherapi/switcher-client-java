package com.switcherapi.client;

import com.switcherapi.Switchers;
import com.switcherapi.client.exception.SwitcherSnapshotWriteException;
import com.switcherapi.fixture.MockWebServerHelper;
import mockwebserver3.QueueDispatcher;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.switcherapi.client.remote.Constants.DEFAULT_ENV;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SwitcherSnapshotValidationFailTest extends MockWebServerHelper {

	private static final String RESOURCES_PATH = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources";
	
	@BeforeAll
	static void setup() throws IOException {
		Files.deleteIfExists(Paths.get(RESOURCES_PATH + "/not_accessible"));

		MockWebServerHelper.setupMockServer();
        
        Switchers.loadProperties();
        Switchers.configure(ContextBuilder.builder().url(String.format("http://localhost:%s", mockBackEnd.getPort())));
    }
	
	@AfterAll
	static void tearDown() {
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
				.environment(DEFAULT_ENV)
				.silentMode(null)
				.snapshotAutoLoad(false)
				.snapshotAutoUpdateInterval(null));
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
