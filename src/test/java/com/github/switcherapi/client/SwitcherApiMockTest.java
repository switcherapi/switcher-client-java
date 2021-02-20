package com.github.switcherapi.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.configuration.SwitcherContext;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherKeyNotAvailableForComponentException;
import com.github.switcherapi.client.exception.SwitcherKeyNotFoundException;
import com.github.switcherapi.client.exception.SwitcherSnapshotWriteException;
import com.github.switcherapi.client.facade.ClientServiceFacade;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.Criteria;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.github.switcherapi.client.utils.SnapshotLoader;
import com.github.switcherapi.client.utils.SwitcherUtils;
import com.google.gson.Gson;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class SwitcherApiMockTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	private static MockWebServer mockBackEnd;
	
	@BeforeAll
	static void setup() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        
        Switchers.loadProperties();
        Switchers.getProperties().setUrl(String.format("http://localhost:%s", mockBackEnd.getPort()));
        Switchers.initializeClient();
    }
	
	@AfterAll
	static void tearDown() throws IOException {
        mockBackEnd.shutdown();
        
    	SwitcherContext.stopWatchingSnapshot();
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "\\new_folder\\generated_on_new_folder.json"));
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "\\new_folder"));
		
    }
	
	@BeforeEach
	public void resetSwitcherState() {
		ClientServiceFacade.getInstance().clearAuthResponse();
		
		Switchers.getProperties().setSnapshotLocation(null);
		Switchers.getProperties().setEnvironment("default");
		Switchers.getProperties().setSilentMode(false);
		Switchers.getProperties().setSnapshotAutoLoad(false);
		Switchers.getProperties().setRetryAfter(null);
		Switchers.initializeClient();
	}
	
	private MockResponse generateMockAuth(String token, int secondsAhead) {
		return new MockResponse()
				.setBody(String.format("{ \"token\": \"%s\", \"exp\": \"%s\" }", 
						token, SwitcherUtils.addTimeDuration(secondsAhead + "s", new Date()).getTime()/1000))
				.addHeader("Content-Type", "application/json");
	}
	
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
	
	private MockResponse generateStatusResponse(String code) {
		return new MockResponse().setStatus(String.format("HTTP/1.1 %s", code));
	
	}
	
	private MockResponse generateCheckSnapshotVersionResponse(String status) {
		return new MockResponse()
			.setBody(String.format("{ \"status\": \"%s\" }", status))
			.addHeader("Content-Type", "application/json");
	}
	
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
	
	@Test
	public void shouldReturnTrue() {
		//mock /auth
		mockBackEnd.enqueue(generateMockAuth("token", 10));
		
		//mock /criteria
		mockBackEnd.enqueue(generateCriteriaResponse("true", false));
		
		//test
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void shouldReturnFalse() {
		//mock /auth
		mockBackEnd.enqueue(generateMockAuth("token", 10));
		
		//mock /criteria
		mockBackEnd.enqueue(generateCriteriaResponse("false", false));
		
		//test
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void shouldHideExecutionReason() {
		//mock /auth
		mockBackEnd.enqueue(generateMockAuth("token", 10));
		
		//mock /criteria
		mockBackEnd.enqueue(generateCriteriaResponse("true", false));
		
		//given
		List<Entry> entries = new ArrayList<>();
		entries.add(new Entry(Entry.DATE, "2019-12-10"));
		
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		
		//test
		assertTrue(switcher.isItOn(entries));
		assertNotNull(switcher.getHistoryExecution());
		
		System.out.println(switcher.getHistoryExecution());
		assertNull(switcher.getHistoryExecution().get(0).getReason());
	}
	
	@Test
	public void shouldShowExecutionReason() {
		//mock /auth
		mockBackEnd.enqueue(generateMockAuth("token", 10));
		
		//mock /criteria
		mockBackEnd.enqueue(generateCriteriaResponse("true", true));
				
		//given
		List<Entry> entries = new ArrayList<>();
		entries.add(new Entry(Entry.DATE, "2019-12-10"));
		
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		switcher.setShowReason(true);
		
		//test
		assertTrue(switcher.isItOn(entries));
		assertNotNull(switcher.getHistoryExecution());
		assertNotNull(switcher.getHistoryExecution().get(0).getReason());
		assertEquals(switcher.getSwitcherKey(), switcher.getHistoryExecution().get(0).getSwitcherKey());
	}
	
	@Test
	public void shouldReturnError_keyNotFound() {
		//mock /auth
		mockBackEnd.enqueue(generateMockAuth("token", 10));
		
		//mock /criteria
		mockBackEnd.enqueue(generateStatusResponse("404"));
		
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		assertThrows(SwitcherKeyNotFoundException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	public void shouldReturnError_componentNotregistered() {
		//mock /auth
		mockBackEnd.enqueue(generateMockAuth("token", 10));
		
		//mock /criteria
		mockBackEnd.enqueue(generateStatusResponse("401"));
		
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		assertThrows(SwitcherKeyNotAvailableForComponentException.class, () -> {
			switcher.isItOn();
		});
	}
	
	
	@Test
	public void shouldReturnError_unauthorizedAPIaccess() {
		//mock /auth
		mockBackEnd.enqueue(generateStatusResponse("401"));
		
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		Exception ex = assertThrows(SwitcherException.class, () -> {
			switcher.isItOn();
		});
		
		assertEquals("Something went wrong: Unauthorized API access", ex.getMessage());
	}
	
	@Test
	public void shouldReturnTrue_silentMode() throws InterruptedException {
		//given
		Switchers.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL);
		Switchers.getProperties().setEnvironment("snapshot_fixture1");
		Switchers.getProperties().setSilentMode(true);
		Switchers.getProperties().setRetryAfter("2s");
		Switchers.initializeClient();
		
		//mock /auth
		mockBackEnd.enqueue(generateMockAuth("token", 10));
		
		//mock /criteria
		mockBackEnd.enqueue(generateCriteriaResponse("true", false));		
		
		//test
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE11);
		assertTrue(switcher.isItOn());
		
		CountDownLatch waiter = new CountDownLatch(1);
		waiter.await(2, TimeUnit.SECONDS);
		
		//mock /isAlive - service unavailable
		mockBackEnd.enqueue(generateStatusResponse("503"));
		
		//test
		assertTrue(switcher.isItOn());
	}
	
	
	@Test
	public void shouldReturnTrue_tokenExpired() throws InterruptedException {
		//mock /auth
		mockBackEnd.enqueue(generateMockAuth("token", 2));
		
		//mock /criteria
		mockBackEnd.enqueue(generateCriteriaResponse("true", false));	
		
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		
		//test
		assertTrue(switcher.isItOn());
		
		CountDownLatch waiter = new CountDownLatch(1);
		waiter.await(2, TimeUnit.SECONDS);
		
		//mock /isAlive
		mockBackEnd.enqueue(generateStatusResponse("200"));
		
		//mock /auth
		mockBackEnd.enqueue(generateMockAuth("token", 2));
		
		//mock /criteria
		mockBackEnd.enqueue(generateCriteriaResponse("true", false));	
				
		//test
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void shouldValidateAndUpdateSnapshot() {
		//mock /auth
		mockBackEnd.enqueue(generateMockAuth("token", 10));
		
		//mock /criteria/snapshot_check
		mockBackEnd.enqueue(generateCheckSnapshotVersionResponse("false"));
		
		//mock /auth isAlive
		mockBackEnd.enqueue(generateMockAuth("token", 10));
		
		//mock /graphql
		mockBackEnd.enqueue(generateSnapshotResponse());
		
		//test
		Switchers.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL);
		
		assertDoesNotThrow(() -> {
			Switchers.initializeClient();
			Switchers.validateSnapshot();
		});
	}
	
	@Test
	public void shouldLookupForSnapshot() {
		Switchers.getProperties().setSnapshotAutoLoad(true);
		Switchers.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL + "/new_folder");
		Switchers.getProperties().setEnvironment("generated_on_new_folder");
		
		//mock /auth
		mockBackEnd.enqueue(generateMockAuth("token", 10));
		
		//mock /graphql
		mockBackEnd.enqueue(generateSnapshotResponse());
		
		//test
		assertDoesNotThrow(() -> {
			Switchers.initializeClient();
		});
	}
	
	@Test
	public void shouldNotLookupForSnapshot_invalidLocation() {
		Switchers.getProperties().setSnapshotAutoLoad(true);
		Switchers.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL + "/inv?&|:>//alid");
		
		//mock /auth
		mockBackEnd.enqueue(generateMockAuth("token", 10));
		
		//mock /graphql
		mockBackEnd.enqueue(generateSnapshotResponse());
		
		//test
		assertThrows(SwitcherSnapshotWriteException.class, () -> {
			Switchers.initializeClient();
		});
	}
	
	@Test
	public void shouldNotLookupForSnapshot_invalidEnvName() {
		Switchers.getProperties().setSnapshotAutoLoad(true);
		Switchers.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL + "/new_folder");
		Switchers.getProperties().setEnvironment("inv?&|:>//alid");
		
		//mock /auth
		mockBackEnd.enqueue(generateMockAuth("token", 10));
		
		//mock /graphql
		mockBackEnd.enqueue(generateSnapshotResponse());
		
		//test
		assertThrows(SwitcherSnapshotWriteException.class, () -> {
			Switchers.initializeClient();
		});
	}

}
