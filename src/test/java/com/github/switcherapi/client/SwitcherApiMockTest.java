package com.github.switcherapi.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.configuration.SwitcherContext;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherInvalidDateTimeArgumentException;
import com.github.switcherapi.client.exception.SwitcherSnapshotLoadException;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.Criteria;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.github.switcherapi.client.utils.SnapshotLoader;
import com.github.switcherapi.client.utils.SwitcherUtils;
import com.google.gson.Gson;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class SwitcherApiMockTest {
	
	private final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	private static MockWebServer mockBackEnd;
	
	@BeforeAll
	static void setup() throws IOException {
		if (mockBackEnd != null) {
			return;
		}
		
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        
        final String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        
        Switchers.getProperties().setSnapshotFile(null);
        Switchers.getProperties().setUrl(baseUrl);
        Switchers.getProperties().setApiKey("API_KEY");
        Switchers.getProperties().setEnvironment("default");
        Switchers.getProperties().setOfflineMode(false);
        Switchers.initializeClient();
    }
	
	@AfterAll
	static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }
	
	private MockResponse generateMockAuth(String token, int secondsAhead) 
			throws SwitcherInvalidDateTimeArgumentException {
		return new MockResponse()
				.setBody(String.format("{ \"token\": \"%s\", \"exp\": \"%s\" }", 
						token, SwitcherUtils.addTimeDuration(secondsAhead + "s", new Date()).getTime()/1000))
				.addHeader("Content-Type", "application/json");
	}
	
	private MockResponse generateCriteriaResponse(String result) {
		return new MockResponse()
			.setBody(String.format("{ \"result\": \"%s\" }", result))
			.addHeader("Content-Type", "application/json");
	}
	
	private MockResponse generateCheckSnapshotVersionResponse(String status) {
		return new MockResponse()
			.setBody(String.format("{ \"status\": \"%s\" }", status))
			.addHeader("Content-Type", "application/json");
	}
	
	private MockResponse generateSnapshotResponse() throws SwitcherSnapshotLoadException {
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
	public void shouldReturnTrue() throws SwitcherException {
		//mock /auth
		mockBackEnd.enqueue(generateMockAuth("token", 10));
		
		//mock /criteria
		mockBackEnd.enqueue(generateCriteriaResponse("true"));
		
		//test
		final Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void shouldReturnFalse() throws SwitcherException {
		//mock /auth
		mockBackEnd.enqueue(generateMockAuth("token", 10));
		
		//mock /criteria
		mockBackEnd.enqueue(generateCriteriaResponse("false"));
		
		//test
		final Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		assertFalse(switcher.isItOn());
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
		SwitcherContext.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL);;
		SwitcherContext.initializeClient();
		
		try {
			SwitcherContext.validateSnapshot();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			assertEquals("Something went wrong", e.getMessage());
			throw e;
		}
	}

}
