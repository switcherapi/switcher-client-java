package com.github.switcherapi.client;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.configuration.SwitcherContext;
import com.github.switcherapi.client.exception.SwitcherAPIConnectionException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.utils.SwitcherUtils;

public class SwitcherOnlineTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	@BeforeEach
	public void setupContext() {
		SwitcherContext.loadProperties();
	}
	
	@Test
	public void shouldReturnError_noConnection() {
		//given
		SwitcherContext.getProperties().setUrl("http://localhost:30");
		SwitcherContext.initializeClient();
		
		//test
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		assertThrows(SwitcherAPIConnectionException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	public void shouldReturnInputRequest() {
		List<Entry> entries = new ArrayList<>();
		entries.add(new Entry(Entry.DATE, "2019-12-10"));
		
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		switcher.prepareEntry(entries);
		assertTrue(SwitcherUtils.isJson(switcher.getInputRequest().toString()));
	}
	
//	
//	@Test
//	public void shouldReturnTrue_tokenExpired() throws Exception {
//		// Generate token with 2 secods expiration time
//		Switcher switcher = generateSwitcherMockTrue(200);
//		assertTrue(switcher.isItOn());
//		Awaitility.await().pollDelay(500, TimeUnit.MILLISECONDS).until(() -> true);
//		assertTrue(switcher.isItOn());
//		
//		// It should renew token here
//		Awaitility.await().pollDelay(2, TimeUnit.SECONDS).until(() -> true);
//		assertTrue(switcher.isItOn());
//	}
//	
//	@Test
//	public void shouldInvokeClientExecuteCriteriaWithNoErrors() throws Exception {
//		//given
//		ClientWSImpl clientService = new ClientWSImpl();
//		final Switcher switcher = new Switcher("ONLINE_KEY", null);
//		
//		final AuthResponse authResponse = new AuthResponse();
//		authResponse.setExp(SwitcherUtils.addTimeDuration("2s", new Date()).getTime()/1000);
//		authResponse.setToken("123lkjsuoi23487skjfh28dskjn29");
//		properties.put(ClientWS.AUTH_RESPONSE, authResponse);
//		
//		WebTarget webTargetMock = PowerMockito.mock(WebTarget.class);
//		Client clientMock = PowerMockito.mock(Client.class);
//		Response responseMock = PowerMockito.mock(Response.class);
//		Builder builderMock = PowerMockito.mock(Builder.class);
//
//		PowerMockito.when(clientMock.target(String.format(ClientWS.CRITERIA_URL, properties.get(SwitcherContextParam.URL)))).thenReturn(webTargetMock);
//		PowerMockito.when(webTargetMock.queryParam(Switcher.KEY, switcher.getSwitcherKey())).thenReturn(webTargetMock);
//		PowerMockito.when(webTargetMock.queryParam(Switcher.SHOW_REASON, properties.containsKey(Switcher.SHOW_REASON) ? 
//				properties.get(Switcher.SHOW_REASON) : Boolean.FALSE)).thenReturn(webTargetMock);
//		PowerMockito.when(webTargetMock.queryParam(Switcher.BYPASS_METRIC, properties.containsKey(Switcher.BYPASS_METRIC) ? 
//				properties.get(Switcher.BYPASS_METRIC) : Boolean.FALSE)).thenReturn(webTargetMock);
//		PowerMockito.when(webTargetMock.request(MediaType.APPLICATION_JSON)).thenReturn(builderMock);
//		PowerMockito.when(builderMock.header(
//				ClientWS.HEADER_AUTHORIZATION, String.format(ClientWS.TOKEN_TEXT, 
//						((AuthResponse) properties.get(ClientWS.AUTH_RESPONSE)).getToken()))).thenReturn(builderMock);
//		PowerMockito.when(builderMock.post(Entity.json(switcher.getInputRequest()))).thenReturn(responseMock);
//		
//		clientService.setClient(clientMock);
//		
//		//test
//		// It returns null but the idea is to check if no exception is thrown during this process
//		clientService.executeCriteriaService(properties, switcher);
//	}
//	
//	@Test
//	public void shouldInvokeClientAuthWithNoErrors() throws Exception {
//		//given
//		ClientWSImpl clientService = new ClientWSImpl();
//		
//		Client clientMock = PowerMockito.mock(Client.class);
//		WebTarget webTargetMock = PowerMockito.mock(WebTarget.class);
//		Builder builderMock = PowerMockito.mock(Builder.class);
//		Response responseMock = PowerMockito.mock(Response.class);
//		
//		PowerMockito.when(clientMock.target(String.format(ClientWS.AUTH_URL, properties.get(SwitcherContextParam.URL)))).thenReturn(webTargetMock);
//		PowerMockito.when(webTargetMock.request(MediaType.APPLICATION_JSON)).thenReturn(builderMock);
//		PowerMockito.when(builderMock.header(ClientWS.HEADER_APIKEY, properties.get(SwitcherContextParam.APIKEY))).thenReturn(builderMock);
//		PowerMockito.when(builderMock.post(Entity.json(new Object()))).thenReturn(responseMock);
//		
//		clientService.setClient(clientMock);
//		
//		//test
//		clientService.auth(properties);
//	}
//	
//	@Test
//	public void shouldInvokeClientCheckWithNoErrors() throws Exception {
//		//given
//		ClientWSImpl clientService = new ClientWSImpl();
//		
//		Client clientMock = PowerMockito.mock(Client.class);
//		WebTarget webTargetMock = PowerMockito.mock(WebTarget.class);
//		Builder builderMock = PowerMockito.mock(Builder.class);
//		Response responseMock = PowerMockito.mock(Response.class);
//		
//		PowerMockito.when(clientMock.target(String.format(ClientWS.CHECK_URL, properties.get(SwitcherContextParam.URL)))).thenReturn(webTargetMock);
//		PowerMockito.when(webTargetMock.request(MediaType.APPLICATION_JSON)).thenReturn(builderMock);
//		PowerMockito.when(builderMock.get()).thenReturn(responseMock);
//		PowerMockito.when(responseMock.getStatus()).thenReturn(200);
//		
//		clientService.setClient(clientMock);
//		
//		//test
//		assertTrue(clientService.isAlive(properties));
//	}


}
