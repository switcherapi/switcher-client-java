package com.github.switcherapi.client;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherRemoteException;
import com.github.switcherapi.client.exception.SwitcherSnapshotWriteException;
import com.github.switcherapi.client.exception.SwitchersValidationException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.StrategyValidator;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.Criteria;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.github.switcherapi.client.model.criteria.SwitchersCheck;
import com.github.switcherapi.client.remote.ClientWSImpl;
import com.github.switcherapi.client.service.remote.ClientRemoteService;
import com.github.switcherapi.client.utils.SnapshotLoader;
import com.github.switcherapi.client.utils.SwitcherUtils;
import com.google.gson.Gson;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.QueueDispatcher;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.guava.Sets;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SwitcherApiMockTest {

	private static final String RESOURCES_PATH = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources";
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources/snapshot";
	
	private static MockWebServer mockBackEnd;
	
	@BeforeAll
	static void setup() throws IOException {
		Files.deleteIfExists(Paths.get(RESOURCES_PATH + "/not_accessible"));
		Files.deleteIfExists(Paths.get(RESOURCES_PATH + "/new_folder/generated_on_new_folder.json"));
		Files.deleteIfExists(Paths.get(RESOURCES_PATH + "/new_folder"));
		Files.deleteIfExists(Paths.get(RESOURCES_PATH + "/generated_mock_default.json"));
		
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
		((QueueDispatcher) mockBackEnd.getDispatcher()).setFailFast(true);
        
        Switchers.loadProperties();
        Switchers.configure(ContextBuilder.builder().url(String.format("http://localhost:%s", mockBackEnd.getPort())));
        Switchers.initializeClient();
    }
	
	@AfterAll
	static void tearDown() throws IOException {
        mockBackEnd.shutdown();
        
        //clean generated outputs
    	SwitcherContext.stopWatchingSnapshot();
		Files.deleteIfExists(Paths.get(RESOURCES_PATH + "/new_folder/generated_on_new_folder.json"));
		Files.deleteIfExists(Paths.get(RESOURCES_PATH + "/new_folder"));
		Files.deleteIfExists(Paths.get(RESOURCES_PATH + "/generated_mock_default.json"));
    }
	
	@BeforeEach
	void resetSwitcherContextState() {
		((QueueDispatcher) mockBackEnd.getDispatcher()).clear();
		ClientRemoteService.getInstance().clearAuthResponse();
		
		Switchers.configure(ContextBuilder.builder()
				.offlineMode(false)
				.snapshotLocation(null)
				.snapshotSkipValidation(false)
				.environment("default")
				.silentMode(false)
				.snapshotAutoLoad(false)
				.snapshotAutoUpdateInterval(null)
				.retryAfter(null));
		
		Switchers.initializeClient();
	}
	
	/**
	 * @see ClientWSImpl#auth()
	 * 
	 * @param secondsAhead time to expire the token
	 * @return Generated mock /auth response
	 */
	private MockResponse generateMockAuth(int secondsAhead) {
		return new MockResponse()
				.setBody(String.format("{ \"token\": \"%s\", \"exp\": \"%s\" }", 
						"mocked_token", SwitcherUtils.addTimeDuration(secondsAhead + "s", new Date()).getTime()/1000))
				.addHeader("Content-Type", "application/json");
	}
	
	/**
	 * @see ClientWSImpl#executeCriteriaService(Switcher, String)
	 * 
	 * @param result returned by the criteria execution
	 * @param reason if want to display along with the result
	 * @return Generated mock /criteria response
	 */
	private MockResponse generateCriteriaResponse(String result, boolean reason) {
		String response;
		if (reason)
			response = "{ \"result\": \"%s\", \"reason\": \"Success\" }";
		else
			response = "{ \"result\": \"%s\" }";
			
		return new MockResponse()
			.setBody(String.format(response, result))
			.addHeader("Content-Type", "application/json");
	}
	
	/**
	 * @see ClientWSImpl#isAlive()
	 * 
	 * @param code HTTP status
	 * @return Generated mock /check response
	 */
	private MockResponse generateStatusResponse(String code) {
		return new MockResponse().setStatus(String.format("HTTP/1.1 %s", code));
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
		criteria.setDomain(SnapshotLoader.loadSnapshot(RESOURCES_PATH + "/default.json"));
		mockedSnapshot.setData(criteria);
		
		Gson gson = new Gson();
		return new MockResponse()
				.setBody(gson.toJson(mockedSnapshot))
				.addHeader("Content-Type", "application/json");
	}
	
	/**
	 * @see ClientWSImpl#checkSwitchers(Set, String)
	 * 
	 * @param switchersNotFound Switcher Keys forced to be not found
	 * @return Generated mock /criteria/check_switchers
	 */
	private MockResponse generateCheckSwitchersResponse(Set<String> switchersNotFound) {
		SwitchersCheck switchersCheckNotFound = new SwitchersCheck();
		switchersCheckNotFound.setNotFound(
				switchersNotFound.toArray(new String[0]));
		
		Gson gson = new Gson();
		return new MockResponse()
				.setBody(gson.toJson(switchersCheckNotFound))
				.addHeader("Content-Type", "application/json");
	}

	private void givenResponse(MockResponse response) {
		((QueueDispatcher) mockBackEnd.getDispatcher()).enqueueResponse(response);
	}
	
	@Test
	void shouldReturnTrue() {
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria
		givenResponse(generateCriteriaResponse("true", false));
		
		//test
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void shouldReturnFalse() {
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria
		givenResponse(generateCriteriaResponse("false", false));
		
		//test
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void shouldHideExecutionReason() {
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria
		givenResponse(generateCriteriaResponse("true", false));
		
		//given
		List<Entry> entries = new ArrayList<>();
		entries.add(Entry.build(StrategyValidator.DATE, "2019-12-10"));
		
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		
		//test
		assertTrue(switcher.isItOn(entries));
		assertNotNull(switcher.getHistoryExecution());
		assertNull(switcher.getHistoryExecution().stream().findFirst().get().getReason());
	}
	
	@Test
	void shouldShowExecutionReason() {
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria
		givenResponse(generateCriteriaResponse("true", true));
				
		//given
		List<Entry> entries = new ArrayList<>();
		entries.add(Entry.build(StrategyValidator.DATE, "2019-12-10"));
		
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		switcher.setShowReason(true);
		
		//test
		assertTrue(switcher.isItOn(entries));
		assertNotNull(switcher.getHistoryExecution());
		assertNotNull(switcher.getHistoryExecution().stream().findFirst().get().getReason());
		assertEquals(switcher.getSwitcherKey(), switcher.getHistoryExecution().stream().findFirst().get().getSwitcherKey());
	}
	
	@Test
	void shouldReturnError_keyNotFound() {
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria
		givenResponse(generateStatusResponse("404"));
		
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		assertThrows(SwitcherRemoteException.class, switcher::isItOn);
	}
	
	@Test
	void shouldReturnError_unauthorizedAPIAccess() {
		//auth
		givenResponse(generateStatusResponse("401"));
		
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		assertThrows(SwitcherException.class, switcher::isItOn);
	}
	
	@Test
	void shouldReturnTrue_silentMode() throws InterruptedException {
		//given
		Switchers.configure(ContextBuilder.builder()
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.environment("fixture1")
				.silentMode(true)
				.retryAfter("5s"));
		
		Switchers.initializeClient();
		
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria
		givenResponse(generateCriteriaResponse("true", false));
		
		//test
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE11);
		assertTrue(switcher.isItOn());
		
		CountDownLatch waiter = new CountDownLatch(1);
		waiter.await(2, TimeUnit.SECONDS);
		
		//isAlive - service unavailable
		givenResponse(generateStatusResponse("503"));
		
		//test will trigger silent
		assertTrue(switcher.isItOn());
		
		//test will use silent (read from snapshot)
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void shouldReturnTrue_tokenExpired() throws InterruptedException {
		//auth
		givenResponse(generateMockAuth(2));
		
		//criteria
		givenResponse(generateCriteriaResponse("true", false));
		
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		
		//test
		assertTrue(switcher.isItOn());
		
		CountDownLatch waiter = new CountDownLatch(1);
		waiter.await(2, TimeUnit.SECONDS);
		
		//auth
		givenResponse(generateMockAuth(2));
		
		//criteria
		givenResponse(generateCriteriaResponse("true", false));
				
		//test
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void shouldValidateAndUpdateSnapshot() {
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria/snapshot_check
		givenResponse(generateCheckSnapshotVersionResponse("false"));
		
		//graphql
		givenResponse(generateSnapshotResponse());
		
		//test
		Switchers.configure(ContextBuilder.builder().snapshotLocation(RESOURCES_PATH));
		
		assertDoesNotThrow(() -> {
			Switchers.initializeClient();
			assertTrue(Switchers.validateSnapshot());
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
		givenResponse(generateSnapshotResponse());
		
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
		givenResponse(generateSnapshotResponse());
		
		//test
		assertDoesNotThrow(Switchers::validateSnapshot);
	}
	
	@Test
	void shouldValidateAndLoadSnapshot_whenOffline() {
		//given
		Switchers.configure(ContextBuilder.builder()
				.offlineMode(true)
				.snapshotAutoLoad(false)
				.snapshotLocation(RESOURCES_PATH)
				.environment("default"));
		
		Switchers.initializeClient();
		
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria/snapshot_check
		givenResponse(generateCheckSnapshotVersionResponse("false"));
		
		//graphql
		givenResponse(generateSnapshotResponse());
		
		//test
		assertDoesNotThrow(Switchers::validateSnapshot);
	}
	
	@Test
	void shouldNotValidateAndLoadSnapshot_serviceUnavailable() {
		//given
		Switchers.configure(ContextBuilder.builder()
				.offlineMode(true)
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
				givenResponse(generateSnapshotResponse());
				
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
				givenResponse(generateSnapshotResponse());
				
				//test
				assertThrows(SwitcherSnapshotWriteException.class, Switchers::initializeClient);
			}
		});
	}
	
	@Test
	void shouldValidateSwitchers() {
		//given
		final Set<String> notFound = Sets.newHashSet();
		
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria/check_switchers
		givenResponse(generateCheckSwitchersResponse(notFound));
		
		//test
		assertDoesNotThrow(Switchers::checkSwitchers);
	}
	
	@Test
	void shouldValidateSwitchers_notConfiguredSwitcherBeingUsed() {
		//given
		final Set<String> notFound = Sets.newHashSet();
		notFound.add("NOT_FOUND_1");
		
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria/check_switchers
		givenResponse(generateCheckSwitchersResponse(notFound));
		
		//test
		Exception ex = assertThrows(SwitchersValidationException.class,
				Switchers::checkSwitchers);
		
		assertEquals(String.format(
				"Something went wrong: Unable to load the following Switcher Key(s): %s", notFound), 
				ex.getMessage());
	}
	
	@Test
	void shouldNotValidateSwitchers_serviceUnavailable() {
		//auth
		givenResponse(generateMockAuth(10));
		
		//criteria/check_switchers
		givenResponse(generateStatusResponse("503"));
		
		//test
		assertThrows(SwitcherRemoteException.class, Switchers::checkSwitchers);
	}
	
	@Test
	void shouldReturnTrue_withThrottle() throws InterruptedException {
		// First call
		givenResponse(generateMockAuth(10)); //auth
		givenResponse(generateCriteriaResponse("true", false)); //criteria
		
		// Async call
		givenResponse(generateMockAuth(10)); //auth
		givenResponse(generateCriteriaResponse("true", false)); //criteria
		
		
		//test
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		switcher.throttle(1000);
		
		for (int i = 0; i < 10; i++) {
			assertTrue(switcher.isItOn());			
		}
		
		// Async call
		givenResponse(generateMockAuth(10)); //auth
		givenResponse(generateCriteriaResponse("true", false)); //criteria
		
		CountDownLatch waiter = new CountDownLatch(1);
		waiter.await(1, TimeUnit.SECONDS);
		assertTrue(switcher.isItOn());
	}

}
