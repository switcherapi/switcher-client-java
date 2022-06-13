package com.github.switcherapi.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.guava.Sets;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.exception.SwitcherAPIConnectionException;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherKeyNotAvailableForComponentException;
import com.github.switcherapi.client.exception.SwitcherKeyNotFoundException;
import com.github.switcherapi.client.exception.SwitcherSnapshoException;
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

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SwitcherApiMockTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	private static MockWebServer mockBackEnd;
	
	@BeforeAll
	static void setup() throws IOException {
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "/not_accessable"));
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "/new_folder/generated_on_new_folder.json"));
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "/new_folder"));
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "/generated_mock_default.json"));
		
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        
        Switchers.loadProperties();
        Switchers.getProperties().setUrl(String.format("http://localhost:%s", mockBackEnd.getPort()));
        Switchers.initializeClient();
    }
	
	@AfterAll
	static void tearDown() throws IOException {
        mockBackEnd.shutdown();
        
        //clean generated outputs
    	SwitcherContext.stopWatchingSnapshot();
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "/new_folder/generated_on_new_folder.json"));
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "/new_folder"));
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "/generated_mock_default.json"));
		
    }
	
	@BeforeEach
	void resetSwitcherContextState() {
		ClientRemoteService.getInstance().clearAuthResponse();
		
		Switchers.getProperties().setOfflineMode(false);
		Switchers.getProperties().setSnapshotLocation(null);
		Switchers.getProperties().setEnvironment("default");
		Switchers.getProperties().setSilentMode(false);
		Switchers.getProperties().setSnapshotAutoLoad(false);
		Switchers.getProperties().setRetryAfter(null);
		Switchers.initializeClient();
	}
	
	/**
	 * @see {@link ClientWSImpl#auth()}
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
	 * @see {@link ClientWSImpl#executeCriteriaService(Switcher, String)}
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
	 * @see {@link ClientWSImpl#isAlive()}
	 * 
	 * @param code HTTP status
	 * @return Generated mock /check response
	 */
	private MockResponse generateStatusResponse(String code) {
		return new MockResponse().setStatus(String.format("HTTP/1.1 %s", code));
	
	}
	
	/**
	 * @see {@link ClientWSImpl#checkSnapshotVersion(long, String)}
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
	 * @see {@link ClientWSImpl#resolveSnapshot(String)}
	 * 
	 * @return Generated mock /graphql respose based on src/test/resources/default.json
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
	
	/**
	 * @see {@link ClientWSImpl#checkSwitchers(Set, String)}
	 * 
	 * @param switchersNotFound Switcher Keys forced to be not found
	 * @return Generated mock /criteria/check_switchers
	 */
	private MockResponse generateCheckSwitchersResponse(Set<String> switchersNotFound) {
		SwitchersCheck switchersCheckNotFound = new SwitchersCheck();
		switchersCheckNotFound.setNotFound(
				switchersNotFound.toArray(new String[switchersNotFound.size()]));
		
		Gson gson = new Gson();
		return new MockResponse()
				.setBody(gson.toJson(switchersCheckNotFound))
				.addHeader("Content-Type", "application/json");
	}
	
	@Test
	void shouldReturnTrue() {
		//auth
		mockBackEnd.enqueue(generateMockAuth(10));
		
		//criteria
		mockBackEnd.enqueue(generateCriteriaResponse("true", false));
		
		//test
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void shouldReturnFalse() {
		//auth
		mockBackEnd.enqueue(generateMockAuth(10));
		
		//criteria
		mockBackEnd.enqueue(generateCriteriaResponse("false", false));
		
		//test
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void shouldHideExecutionReason() {
		//auth
		mockBackEnd.enqueue(generateMockAuth(10));
		
		//criteria
		mockBackEnd.enqueue(generateCriteriaResponse("true", false));
		
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
		mockBackEnd.enqueue(generateMockAuth(10));
		
		//criteria
		mockBackEnd.enqueue(generateCriteriaResponse("true", true));
				
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
		mockBackEnd.enqueue(generateMockAuth(10));
		
		//criteria
		mockBackEnd.enqueue(generateStatusResponse("404"));
		
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		assertThrows(SwitcherKeyNotFoundException.class, () ->
			switcher.isItOn()
		);
	}
	
	@Test
	void shouldReturnError_componentNotregistered() {
		//auth
		mockBackEnd.enqueue(generateMockAuth(10));
		
		//criteria
		mockBackEnd.enqueue(generateStatusResponse("401"));
		
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		assertThrows(SwitcherKeyNotAvailableForComponentException.class, () ->
			switcher.isItOn()
		);
	}
	
	
	@Test
	void shouldReturnError_unauthorizedAPIaccess() {
		//auth
		mockBackEnd.enqueue(generateStatusResponse("401"));
		
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		Exception ex = assertThrows(SwitcherException.class, () -> {
			switcher.isItOn();
		});
		
		assertEquals("Something went wrong: Unauthorized API access", ex.getMessage());
	}
	
	@Test
	void shouldReturnTrue_silentMode() throws InterruptedException {
		//given
		Switchers.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL);
		Switchers.getProperties().setEnvironment("snapshot_fixture1");
		Switchers.getProperties().setSilentMode(true);
		Switchers.getProperties().setRetryAfter("5s");
		Switchers.initializeClient();
		
		//auth
		mockBackEnd.enqueue(generateMockAuth(10));
		
		//criteria
		mockBackEnd.enqueue(generateCriteriaResponse("true", false));		
		
		//test
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE11);
		assertTrue(switcher.isItOn());
		
		CountDownLatch waiter = new CountDownLatch(1);
		waiter.await(2, TimeUnit.SECONDS);
		
		//isAlive - service unavailable
		mockBackEnd.enqueue(generateStatusResponse("503"));
		
		//test will trigger silent
		assertTrue(switcher.isItOn());
		
		//test will use silent (read from snapshot)
		assertTrue(switcher.isItOn());
	}
	
	
	@Test
	void shouldReturnTrue_tokenExpired() throws InterruptedException {
		//auth
		mockBackEnd.enqueue(generateMockAuth(2));
		
		//criteria
		mockBackEnd.enqueue(generateCriteriaResponse("true", false));	
		
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		
		//test
		assertTrue(switcher.isItOn());
		
		CountDownLatch waiter = new CountDownLatch(1);
		waiter.await(2, TimeUnit.SECONDS);
		
		//isAlive
		mockBackEnd.enqueue(generateStatusResponse("200"));
		
		//auth
		mockBackEnd.enqueue(generateMockAuth(2));
		
		//criteria
		mockBackEnd.enqueue(generateCriteriaResponse("true", false));	
				
		//test
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void shouldValidateAndUpdateSnapshot() {
		//auth
		mockBackEnd.enqueue(generateMockAuth(10));
		
		//criteria/snapshot_check
		mockBackEnd.enqueue(generateCheckSnapshotVersionResponse("false"));
		
		//auth isAlive
		mockBackEnd.enqueue(generateMockAuth(10));
		
		//graphql
		mockBackEnd.enqueue(generateSnapshotResponse());
		
		//test
		Switchers.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL);
		
		assertDoesNotThrow(() -> {
			Switchers.initializeClient();
			assertTrue(Switchers.validateSnapshot());
		});
	}
	
	@Test
	void shouldSkipValidatSnapshot() {
		//given
		Switchers.getProperties().setSnapshotSkipValidation(true);
		
		//test
		assertDoesNotThrow(() -> {
			Switchers.initializeClient();
			assertFalse(Switchers.validateSnapshot());
		});
	}
	
	@Test
	void shouldLookupForSnapshot() {
		//given
		Switchers.getProperties().setSnapshotAutoLoad(true);
		Switchers.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL + "/new_folder");
		Switchers.getProperties().setEnvironment("generated_on_new_folder");
		
		//auth
		mockBackEnd.enqueue(generateMockAuth(10));
		
		//graphql
		mockBackEnd.enqueue(generateSnapshotResponse());
		
		//test
		assertDoesNotThrow(() -> Switchers.initializeClient());
	}
	
	@Test
	void shouldNotLookupForSnapshot_serviceUnavailable() {
		//given
		Switchers.getProperties().setSnapshotAutoLoad(true);
		Switchers.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL + "/new_folder");
		Switchers.getProperties().setEnvironment("generated_on_new_folder");
		
		//auth
		mockBackEnd.enqueue(generateMockAuth(10));
		
		//graphql
		mockBackEnd.enqueue(generateStatusResponse("503"));
		
		//test
		Exception ex = assertThrows(SwitcherSnapshoException.class, () ->
			Switchers.initializeClient()
		);
		
		assertEquals("Something went wrong: Unable to execute resolveSnapshot", 
			ex.getMessage());
	}
	
	@Test
	void shouldLookupForSnapshot_whenNotAutoLoad() {
		//given
		Switchers.getProperties().setSnapshotAutoLoad(false);
		Switchers.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL);
		Switchers.getProperties().setEnvironment("generated_mock_default");
		Switchers.initializeClient();
		
		//auth
		mockBackEnd.enqueue(generateMockAuth(10));
		
		//graphql
		mockBackEnd.enqueue(generateSnapshotResponse());
		
		//test
		assertDoesNotThrow(() -> Switchers.validateSnapshot());
	}
	
	@Test
	void shouldValidateAndLoadSnapshot_whenOffline() {
		//given
		Switchers.getProperties().setOfflineMode(true);
		Switchers.getProperties().setSnapshotAutoLoad(false);
		Switchers.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL);
		Switchers.getProperties().setEnvironment("default");
		Switchers.initializeClient();
		
		//auth
		mockBackEnd.enqueue(generateMockAuth(10));
		
		//criteria/snapshot_check
		mockBackEnd.enqueue(generateCheckSnapshotVersionResponse("false"));
		
		//auth isAlive
		mockBackEnd.enqueue(generateMockAuth(10));
		
		//graphql
		mockBackEnd.enqueue(generateSnapshotResponse());
		
		//test
		assertDoesNotThrow(() -> Switchers.validateSnapshot());
	}
	
	@Test
	void shouldNotValidateAndLoadSnapshot_serviceUnavailable() {
		//given
		Switchers.getProperties().setOfflineMode(true);
		Switchers.getProperties().setSnapshotAutoLoad(false);
		Switchers.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL);
		Switchers.getProperties().setEnvironment("default");
		Switchers.initializeClient();
		
		//auth
		mockBackEnd.enqueue(generateMockAuth(10));
		
		//criteria/snapshot_check
		mockBackEnd.enqueue(generateStatusResponse("503"));
		
		//test
		assertThrows(SwitcherSnapshoException.class, () ->
			Switchers.validateSnapshot()
		);
	}
	
	@Test
	@Order(value = 1)
	void shouldNotLookupForSnapshot_invalidLocation() {
		//given
		Switchers.getProperties().setSnapshotAutoLoad(true);
		Switchers.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL + "/not_accessible");
		
		//test
		assertDoesNotThrow(() -> {
			try (final RandomAccessFile raFile = 
					new RandomAccessFile(SNAPSHOTS_LOCAL + "/not_accessible", "rw")) {
				
				//given an inaccessible folder
				raFile.getChannel().lock();
				
				//auth
				mockBackEnd.enqueue(generateMockAuth(10));
				
				//graphql
				mockBackEnd.enqueue(generateSnapshotResponse());
				
				//test
				assertThrows(SwitcherSnapshotWriteException.class, () ->
					Switchers.initializeClient()
				);
			} catch (IOException e) {
				throw e;
			}
		});

	}
	
	@Test
	@Order(value = 2)
	void shouldNotLookupForSnapshot_invalidFolderLocation() {
		//given
		Switchers.getProperties().setSnapshotAutoLoad(true);
		Switchers.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL + "/not_accessible/folder");
		
		//test
		assertDoesNotThrow(() -> {
			try (final RandomAccessFile raFile = 
					new RandomAccessFile(SNAPSHOTS_LOCAL + "/not_accessible", "rw")) {
				
				//given an inaccessible folder
				raFile.getChannel().lock();
				
				//auth
				mockBackEnd.enqueue(generateMockAuth(10));
				
				//graphql
				mockBackEnd.enqueue(generateSnapshotResponse());
				
				//test
				assertThrows(SwitcherSnapshotWriteException.class, () -> Switchers.initializeClient());
			} catch (IOException e) {
				throw e;
			}
		});
	}
	
	@Test
	void shouldValidateSwitchers() {
		//given
		final Set<String> notFound = Sets.newHashSet();
		
		//auth
		mockBackEnd.enqueue(generateMockAuth(10));
		
		//criteria/check_switchers
		mockBackEnd.enqueue(generateCheckSwitchersResponse(notFound));
		
		//test
		assertDoesNotThrow(() -> Switchers.checkSwitchers());
	}
	
	@Test
	void shouldValidateSwitchers_notConfiguredSwitcherBeingUsed() {
		//given
		final Set<String> notFound = Sets.newHashSet();
		notFound.add("NOT_FOUND_1");
		
		//auth
		mockBackEnd.enqueue(generateMockAuth(10));
		
		//criteria/check_switchers
		mockBackEnd.enqueue(generateCheckSwitchersResponse(notFound));
		
		//test
		Exception ex = assertThrows(SwitchersValidationException.class, () ->
			Switchers.checkSwitchers()
		);
		
		assertEquals(String.format(
				"Something went wrong: Unable to load the following Switcher Key(s): %s", notFound), 
				ex.getMessage());
	}
	
	@Test
	void shouldNotValidateSwitchers_serviceUnavailable() {
		//auth
		mockBackEnd.enqueue(generateMockAuth(10));
		
		//criteria/check_switchers
		mockBackEnd.enqueue(generateStatusResponse("503"));
		
		//test
		assertThrows(SwitcherAPIConnectionException.class, () ->
			Switchers.checkSwitchers()
		);
	}
	
	@Test
	void shouldReturnTrue_withThrottle() throws InterruptedException {
		// First call
		mockBackEnd.enqueue(generateMockAuth(10)); //auth
		mockBackEnd.enqueue(generateCriteriaResponse("true", false)); //criteria
		
		// Async call
		mockBackEnd.enqueue(generateMockAuth(10)); //auth
		mockBackEnd.enqueue(generateCriteriaResponse("true", false)); //criteria
		
		
		//test
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		switcher.throttle(1000);
		
		for (int i = 0; i < 10; i++) {
			assertTrue(switcher.isItOn());			
		}
		
		// Async call
		mockBackEnd.enqueue(generateMockAuth(10)); //auth
		mockBackEnd.enqueue(generateCriteriaResponse("true", false)); //criteria
		
		CountDownLatch waiter = new CountDownLatch(1);
		waiter.await(1, TimeUnit.SECONDS);
		assertTrue(switcher.isItOn());
	}

}
