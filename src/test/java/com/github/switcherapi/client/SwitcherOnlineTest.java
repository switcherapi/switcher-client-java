package com.github.switcherapi.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherKeyNotAvailableForComponentException;
import com.github.switcherapi.client.exception.SwitcherKeyNotFoundException;
import com.github.switcherapi.client.facade.ClientServiceFacade;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.response.AuthResponse;
import com.github.switcherapi.client.model.response.CriteriaResponse;
import com.github.switcherapi.client.service.ClientService;
import com.github.switcherapi.client.service.ClientServiceImpl;
import com.github.switcherapi.client.utils.SwitcherContextParam;
import com.github.switcherapi.client.utils.SwitcherUtils;

@PowerMockIgnore({"javax.management.*", "org.apache.log4j.*", "javax.xml.*", "javax.script.*"})
@RunWith(PowerMockRunner.class)
public class SwitcherOnlineTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	private Map<String, Object> properties;
	
	@Before
	public void setupContext() {

		properties = new HashMap<String, Object>();
		properties.put(SwitcherContextParam.URL, "http://localhost:3000");
		properties.put(SwitcherContextParam.APIKEY, "$2b$08$S2Wj/wG/Rfs3ij0xFbtgveDtyUAjML1/TOOhocDg5dhOaU73CEXfK");
		properties.put(SwitcherContextParam.DOMAIN, "switcher-domain");
		properties.put(SwitcherContextParam.COMPONENT, "switcher-client");
		properties.put(SwitcherContextParam.ENVIRONMENT, "default");
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
	}
	
	private Switcher generateSwitcherMockTrue(final int executionStatus) throws Exception {
		return generateSwitcherMockTrue(executionStatus, 200, false);
	}
	
	/**
	 * The premisses for this mock scenario are:
	 * 
	 * <li> Auth token will expire in 2s.
	 * <li> Criteria response will return true as result, in case executionStatus code = 200, otherwise, exceptions
	 * might be rised
	 * 
	 * @param executionStatus
	 * @param noExecutionReason
	 * @return
	 * @throws Exception
	 */
	private Switcher generateSwitcherMockTrue(final int executionStatus, final int authStatus, boolean noExecutionReason) 
			throws Exception {
		
		SwitcherFactory.buildContext(properties, false);
		final Switcher switcher = SwitcherFactory.getSwitcher("ONLINE_KEY");
		
		final ClientService mockClientServiceImpl = PowerMockito.mock(ClientService.class);
		final Response mockResponseAuth = PowerMockito.mock(Response.class);
		final Response mockResponseExecute = PowerMockito.mock(Response.class);
		
		final AuthResponse authResponse = new AuthResponse();
		authResponse.setExp(SwitcherUtils.addTimeDuration("2s", new Date()).getTime()/1000);
		authResponse.setToken("123lkjsuoi23487skjfh28dskjn29");
		
		final CriteriaResponse criteriaResponse = new CriteriaResponse();
		if (noExecutionReason)
			criteriaResponse.setReason("Success");
		criteriaResponse.setResult(true);
		
		PowerMockito.when(mockClientServiceImpl.isAlive(properties)).thenReturn(true);
		
		PowerMockito.when(mockResponseAuth.getStatus()).thenReturn(authStatus);
		PowerMockito.when(mockResponseAuth.readEntity(AuthResponse.class)).thenReturn(authResponse);
		PowerMockito.when(mockClientServiceImpl.auth(this.properties)).thenReturn(mockResponseAuth);
		
		PowerMockito.when(mockResponseExecute.getStatus()).thenReturn(executionStatus);
		PowerMockito.when(mockResponseExecute.readEntity(CriteriaResponse.class)).thenReturn(criteriaResponse);
		PowerMockito.when(mockClientServiceImpl.executeCriteriaService(this.properties, switcher)).thenReturn(mockResponseExecute);
		
		ClientServiceFacade.getInstance().setClientService(mockClientServiceImpl);
		
		return switcher;
	}
	
	@Test
	public void shouldReturnTrue() throws Exception {
		Switcher switcher = generateSwitcherMockTrue(200);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void shouldReturnTrue_differentCalls() throws Exception {
		//given
		List<Entry> entries = new ArrayList<>();
		entries.add(new Entry(Entry.DATE, "2019-12-10"));
		
		Switcher switcher = generateSwitcherMockTrue(200);
		switcher.setBypassMetrics(true);
		
		//test
		assertTrue(switcher.isItOn(entries));
		assertTrue(switcher.isItOn("NEW_KEY"));
		assertTrue(switcher.isItOn("NEW_KEY", new Entry(Entry.VALUE, "Value"), false));
		assertNotNull(switcher.getHistoryExecution());
		assertEquals(3, switcher.getHistoryExecution().size());
	}
	
	@Test
	public void shouldHideExecutionReason() throws Exception {
		//given
		List<Entry> entries = new ArrayList<>();
		entries.add(new Entry(Entry.DATE, "2019-12-10"));
		
		Switcher switcher = generateSwitcherMockTrue(200);
		
		//test
		assertTrue(switcher.isItOn(entries));
		assertNotNull(switcher.getHistoryExecution());
		assertNull(switcher.getHistoryExecution().get(0).getReason());
	}
	
	@Test
	public void shouldShowExecutionReason() throws Exception {
		//given
		List<Entry> entries = new ArrayList<>();
		entries.add(new Entry(Entry.DATE, "2019-12-10"));
		
		Switcher switcher = generateSwitcherMockTrue(200, 200, true);
		switcher.setShowReason(true); // this has no effect in here because the mock has been already generated
		
		//test
		assertTrue(switcher.isItOn(entries));
		assertNotNull(switcher.getHistoryExecution());
		assertNotNull(switcher.getHistoryExecution().get(0).getReason());
		assertEquals(switcher.getSwitcherKey(), switcher.getHistoryExecution().get(0).getSwitcherKey());
	}
	
	@Test
	public void shouldReturnInputRequest() throws Exception {
		List<Entry> entries = new ArrayList<>();
		entries.add(new Entry(Entry.DATE, "2019-12-10"));
		
		Switcher switcher = generateSwitcherMockTrue(200);
		switcher.prepareEntry(entries);
		assertTrue(SwitcherUtils.isJson(switcher.getInputRequest().toString()));
	}
	
	@Test(expected = SwitcherKeyNotFoundException.class)
	public void shouldReturnError_keyNotFound() throws Exception {
		Switcher switcher = generateSwitcherMockTrue(404);
		assertTrue(switcher.isItOn());
	}
	
	@Test(expected = SwitcherKeyNotAvailableForComponentException.class)
	public void shouldReturnError_componentNotregistered() throws Exception {
		Switcher switcher = generateSwitcherMockTrue(401);
		assertTrue(switcher.isItOn());
	}
	
	@Test(expected = SwitcherException.class)
	public void shouldReturnError_unauthorizedAPIaccess() throws Exception {
		Switcher switcher = generateSwitcherMockTrue(200, 401, true);
		
		try {
			switcher.isItOn();
		} catch (Exception e) {
			assertEquals("Something went wrong: Unauthorized API access", e.getMessage());
			throw e;
		}
	}
	
	@Test
	public void shouldReturnTrue_silentMode() throws Exception {
		properties.put(SwitcherContextParam.SILENT_MODE, true);
		properties.put(SwitcherContextParam.RETRY_AFTER, "2s");
		
		SwitcherFactory.buildContext(properties, false);
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE11");
		assertTrue(switcher.isItOn());
		assertTrue(switcher.isItOn());
		
		Awaitility.await().pollDelay(2, TimeUnit.SECONDS).until(() -> true);
		
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void shouldReturnTrue_tokenExpired() throws Exception {
		// Generate token with 2 secods expiration time
		Switcher switcher = generateSwitcherMockTrue(200);
		assertTrue(switcher.isItOn());
		Awaitility.await().pollDelay(500, TimeUnit.MILLISECONDS).until(() -> true);
		assertTrue(switcher.isItOn());
		
		// It should renew token here
		Awaitility.await().pollDelay(2, TimeUnit.SECONDS).until(() -> true);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void shouldInvokeClientExecuteCriteriaWithNoErrors() throws Exception {
		//given
		ClientServiceImpl clientService = new ClientServiceImpl();
		final Switcher switcher = new Switcher("ONLINE_KEY", null);
		
		final AuthResponse authResponse = new AuthResponse();
		authResponse.setExp(SwitcherUtils.addTimeDuration("2s", new Date()).getTime()/1000);
		authResponse.setToken("123lkjsuoi23487skjfh28dskjn29");
		properties.put(ClientService.AUTH_RESPONSE, authResponse);
		
		WebTarget webTargetMock = PowerMockito.mock(WebTarget.class);
		Client clientMock = PowerMockito.mock(Client.class);
		Response responseMock = PowerMockito.mock(Response.class);
		Builder builderMock = PowerMockito.mock(Builder.class);

		PowerMockito.when(clientMock.target(String.format(ClientService.CRITERIA_URL, properties.get(SwitcherContextParam.URL)))).thenReturn(webTargetMock);
		PowerMockito.when(webTargetMock.queryParam(Switcher.KEY, switcher.getSwitcherKey())).thenReturn(webTargetMock);
		PowerMockito.when(webTargetMock.queryParam(Switcher.SHOW_REASON, properties.containsKey(Switcher.SHOW_REASON) ? 
				properties.get(Switcher.SHOW_REASON) : Boolean.FALSE)).thenReturn(webTargetMock);
		PowerMockito.when(webTargetMock.queryParam(Switcher.BYPASS_METRIC, properties.containsKey(Switcher.BYPASS_METRIC) ? 
				properties.get(Switcher.BYPASS_METRIC) : Boolean.FALSE)).thenReturn(webTargetMock);
		PowerMockito.when(webTargetMock.request(MediaType.APPLICATION_JSON)).thenReturn(builderMock);
		PowerMockito.when(builderMock.header(
				ClientService.HEADER_AUTHORIZATION, String.format(ClientService.TOKEN_TEXT, 
						((AuthResponse) properties.get(ClientService.AUTH_RESPONSE)).getToken()))).thenReturn(builderMock);
		PowerMockito.when(builderMock.post(Entity.json(switcher.getInputRequest()))).thenReturn(responseMock);
		
		clientService.setClient(clientMock);
		
		//test
		// It returns null but the idea is to check if no exception is thrown during this process
		clientService.executeCriteriaService(properties, switcher);
	}
	
	@Test
	public void shouldInvokeClientAuthWithNoErrors() throws Exception {
		//given
		ClientServiceImpl clientService = new ClientServiceImpl();
		
		Client clientMock = PowerMockito.mock(Client.class);
		WebTarget webTargetMock = PowerMockito.mock(WebTarget.class);
		Builder builderMock = PowerMockito.mock(Builder.class);
		Response responseMock = PowerMockito.mock(Response.class);
		
		PowerMockito.when(clientMock.target(String.format(ClientService.AUTH_URL, properties.get(SwitcherContextParam.URL)))).thenReturn(webTargetMock);
		PowerMockito.when(webTargetMock.request(MediaType.APPLICATION_JSON)).thenReturn(builderMock);
		PowerMockito.when(builderMock.header(ClientService.HEADER_APIKEY, properties.get(SwitcherContextParam.APIKEY))).thenReturn(builderMock);
		PowerMockito.when(builderMock.post(Entity.json(new Object()))).thenReturn(responseMock);
		
		clientService.setClient(clientMock);
		
		//test
		clientService.auth(properties);
	}
	
	@Test
	public void shouldInvokeClientCheckWithNoErrors() throws Exception {
		//given
		ClientServiceImpl clientService = new ClientServiceImpl();
		
		Client clientMock = PowerMockito.mock(Client.class);
		WebTarget webTargetMock = PowerMockito.mock(WebTarget.class);
		Builder builderMock = PowerMockito.mock(Builder.class);
		Response responseMock = PowerMockito.mock(Response.class);
		
		PowerMockito.when(clientMock.target(String.format(ClientService.CHECK_URL, properties.get(SwitcherContextParam.URL)))).thenReturn(webTargetMock);
		PowerMockito.when(webTargetMock.request(MediaType.APPLICATION_JSON)).thenReturn(builderMock);
		PowerMockito.when(builderMock.get()).thenReturn(responseMock);
		PowerMockito.when(responseMock.getStatus()).thenReturn(200);
		
		clientService.setClient(clientMock);
		
		//test
		assertTrue(clientService.isAlive(properties));
	}


}
