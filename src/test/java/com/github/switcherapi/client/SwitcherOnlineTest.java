package com.github.switcherapi.client;

import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;

public class SwitcherOnlineTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
//	@Before
//	public void setupContext() {
//		SwitcherContext.getProperties().setSnapshotFile(SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherContext.initializeClient();
//	}
//	
//	private Switcher generateSwitcherMockTrue(final int executionStatus) throws Exception {
//		return generateSwitcherMockTrue(executionStatus, 200, false);
//	}
//	
//	/**
//	 * The premisses for this mock scenario are:
//	 * 
//	 * <li> Auth token will expire in 2s.
//	 * <li> Criteria response will return true as result, in case executionStatus code = 200, otherwise, exceptions
//	 * might be rised
//	 * 
//	 * @param executionStatus
//	 * @param noExecutionReason
//	 * @return
//	 * @throws Exception
//	 */
//	private Switcher generateSwitcherMockTrue(final int executionStatus, final int authStatus, boolean noExecutionReason) 
//			throws Exception {
//		
//		final Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
//		
//		final ClientWS mockClientServiceImpl = PowerMockito.mock(ClientWS.class);
//		final Response mockResponseAuth = PowerMockito.mock(Response.class);
//		final Response mockResponseExecute = PowerMockito.mock(Response.class);
//		
//		final AuthResponse authResponse = new AuthResponse();
//		authResponse.setExp(SwitcherUtils.addTimeDuration("2s", new Date()).getTime()/1000);
//		authResponse.setToken("123lkjsuoi23487skjfh28dskjn29");
//		
//		final CriteriaResponse criteriaResponse = new CriteriaResponse();
//		if (noExecutionReason)
//			criteriaResponse.setReason("Success");
//		criteriaResponse.setResult(true);
//		
//		PowerMockito.when(mockClientServiceImpl.isAlive()).thenReturn(true);
//		
//		PowerMockito.when(mockResponseAuth.getStatus()).thenReturn(authStatus);
//		PowerMockito.when(mockResponseAuth.readEntity(AuthResponse.class)).thenReturn(authResponse);
//		PowerMockito.when(mockClientServiceImpl.auth()).thenReturn(mockResponseAuth);
//		
//		PowerMockito.when(mockResponseExecute.getStatus()).thenReturn(executionStatus);
//		PowerMockito.when(mockResponseExecute.readEntity(CriteriaResponse.class)).thenReturn(criteriaResponse);
//		PowerMockito.when(mockClientServiceImpl.executeCriteriaService(
//				switcher, authResponse.getToken())).thenReturn(mockResponseExecute);
//		
//		ClientServiceFacade.getInstance().setClientWS(mockClientServiceImpl);
//		
//		return switcher;
//	}
//	
//	@Test(expected = SwitcherAPIConnectionException.class)
//	public void shouldReturnError_noConnection() throws Exception {
//		//given
//		SwitcherContext.getProperties().setUrl("http://localhost:30");
//		SwitcherContext.initializeClient();
//		
//		//test
//		final Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
//		switcher.isItOn();
//	}
//	
//	@Test
//	public void shouldReturnTrue() throws Exception {
//		Switcher switcher = generateSwitcherMockTrue(200);
//		assertTrue(switcher.isItOn());
//	}
//	
//	@Test
//	public void shouldReturnTrue_differentCalls() throws Exception {
//		//given
//		List<Entry> entries = new ArrayList<>();
//		entries.add(new Entry(Entry.DATE, "2019-12-10"));
//		
//		Switcher switcher = generateSwitcherMockTrue(200);
//		switcher.setBypassMetrics(true);
//		
//		//test
//		assertTrue(switcher.isItOn(entries));
//		assertTrue(switcher.isItOn(Switchers.ONLINE_KEY));
//		assertTrue(switcher.isItOn(Switchers.ONLINE_KEY, new Entry(Entry.VALUE, "Value"), false));
//		assertNotNull(switcher.getHistoryExecution());
//		assertEquals(3, switcher.getHistoryExecution().size());
//	}
//	
//	@Test
//	public void shouldHideExecutionReason() throws Exception {
//		//given
//		List<Entry> entries = new ArrayList<>();
//		entries.add(new Entry(Entry.DATE, "2019-12-10"));
//		
//		Switcher switcher = generateSwitcherMockTrue(200);
//		
//		//test
//		assertTrue(switcher.isItOn(entries));
//		assertNotNull(switcher.getHistoryExecution());
//		assertNull(switcher.getHistoryExecution().get(0).getReason());
//	}
//	
//	@Test
//	public void shouldShowExecutionReason() throws Exception {
//		//given
//		List<Entry> entries = new ArrayList<>();
//		entries.add(new Entry(Entry.DATE, "2019-12-10"));
//		
//		Switcher switcher = generateSwitcherMockTrue(200, 200, true);
//		switcher.setShowReason(true); // this has no effect in here because the mock has been already generated
//		
//		//test
//		assertTrue(switcher.isItOn(entries));
//		assertNotNull(switcher.getHistoryExecution());
//		assertNotNull(switcher.getHistoryExecution().get(0).getReason());
//		assertEquals(switcher.getSwitcherKey(), switcher.getHistoryExecution().get(0).getSwitcherKey());
//	}
//	
//	@Test
//	public void shouldReturnInputRequest() throws Exception {
//		List<Entry> entries = new ArrayList<>();
//		entries.add(new Entry(Entry.DATE, "2019-12-10"));
//		
//		Switcher switcher = generateSwitcherMockTrue(200);
//		switcher.prepareEntry(entries);
//		assertTrue(SwitcherUtils.isJson(switcher.getInputRequest().toString()));
//	}
//	
//	@Test(expected = SwitcherKeyNotFoundException.class)
//	public void shouldReturnError_keyNotFound() throws Exception {
//		Switcher switcher = generateSwitcherMockTrue(404);
//		assertTrue(switcher.isItOn());
//	}
//	
//	@Test(expected = SwitcherKeyNotAvailableForComponentException.class)
//	public void shouldReturnError_componentNotregistered() throws Exception {
//		Switcher switcher = generateSwitcherMockTrue(401);
//		assertTrue(switcher.isItOn());
//	}
//	
//	@Test(expected = SwitcherException.class)
//	public void shouldReturnError_unauthorizedAPIaccess() throws Exception {
//		Switcher switcher = generateSwitcherMockTrue(200, 401, true);
//		
//		try {
//			switcher.isItOn();
//		} catch (Exception e) {
//			assertEquals("Something went wrong: Unauthorized API access", e.getMessage());
//			throw e;
//		}
//	}
//	
//	@Test
//	public void shouldReturnTrue_silentMode() throws Exception {
//		//given
//		SwitcherContext.getProperties().setSilentMode(true);
//		SwitcherContext.getProperties().setRetryAfter("2s");
//		SwitcherContext.initializeClient();
//		
//		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE11);
//		assertTrue(switcher.isItOn());
//		assertTrue(switcher.isItOn());
//		
//		Awaitility.await().pollDelay(2, TimeUnit.SECONDS).until(() -> true);
//		
//		assertTrue(switcher.isItOn());
//	}
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
