package com.github.switcherapi.client.utils;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.switcherapi.client.model.criteria.Domain;

public class SnapshotLoaderTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	private Map<String, Object> properties;
	
	@BeforeAll
	public void setupContext() {

	}
	
	/**
	 * The premisses for this mock scenario are:
	 * 
	 * <li> Auth token will expire in 2s.
	 * <li> Snapshot will be loaded from {@link #SNAPSHOTS_LOCAL}/default.json file.
	 */
	private void generateLoaderMock(final int authStatus) throws Exception {
		
//		final ClientWS mockClientServiceImpl = PowerMockito.mock(ClientWS.class);
//		final Response mockResponseAuth = PowerMockito.mock(Response.class);
//		final Response mockResponseResolveSnapshot = PowerMockito.mock(Response.class);
//		
//		final AuthResponse authResponse = new AuthResponse();
//		authResponse.setExp(SwitcherUtils.addTimeDuration("2s", new Date()).getTime()/1000);
//		authResponse.setToken("123lkjsuoi23487skjfh28dskjn29");
//		
//		PowerMockito.when(mockClientServiceImpl.auth()).thenReturn(mockResponseAuth);
//		PowerMockito.when(mockResponseAuth.getStatus()).thenReturn(authStatus);
//		PowerMockito.when(mockResponseAuth.readEntity(AuthResponse.class)).thenReturn(authResponse);
//		
//		final Snapshot mockedSnapshot = new Snapshot();
//		final Criteria criteria = new Criteria();
//		criteria.setDomain(SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/default.json"));
//		mockedSnapshot.setData(criteria);
//		
//		PowerMockito.when(mockClientServiceImpl.resolveSnapshot(authResponse.getToken())).thenReturn(mockResponseResolveSnapshot);
//		PowerMockito.when(mockResponseResolveSnapshot.readEntity(Snapshot.class)).thenReturn(mockedSnapshot);
//		
//		ClientServiceFacade.getInstance().setClientWS(mockClientServiceImpl);
	}
	
	/**
	 * The premisses for this mock scenario are:
	 * 
	 * <li> Auth token will expire in 2s.
	 * <li> Snapshot will be loaded from {@link #SNAPSHOTS_LOCAL}/default.json file.
	 * <li> Status can be false for outdate or true for update.
	 * <li> The snapshot version coming from the file should be compared with 1588557288037.
	 */
	private void generateSnapshotVersionMock(boolean status) throws Exception {
		
//		final ClientWS mockClientServiceImpl = PowerMockito.mock(ClientWS.class);
//		final Response mockResponseAuth = PowerMockito.mock(Response.class);
//		final Response mockResponseResolveSnapshot = PowerMockito.mock(Response.class);
//		final Response mockResponseSnapshotVersion = PowerMockito.mock(Response.class);
//		
//		final AuthResponse authResponse = new AuthResponse();
//		authResponse.setExp(SwitcherUtils.addTimeDuration("2s", new Date()).getTime()/1000);
//		authResponse.setToken("123lkjsuoi23487skjfh28dskjn29");
//		
//		PowerMockito.when(mockClientServiceImpl.isAlive()).thenReturn(true);
//		
//		PowerMockito.when(mockClientServiceImpl.auth()).thenReturn(mockResponseAuth);
//		PowerMockito.when(mockResponseAuth.readEntity(AuthResponse.class)).thenReturn(authResponse);
//		
//		final Snapshot mockedSnapshot = new Snapshot();
//		final Criteria criteria = new Criteria();
//		criteria.setDomain(SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/default.json"));
//		mockedSnapshot.setData(criteria);
//		
//		PowerMockito.when(mockClientServiceImpl.resolveSnapshot(authResponse.getToken())).thenReturn(mockResponseResolveSnapshot);
//		PowerMockito.when(mockResponseResolveSnapshot.readEntity(Snapshot.class)).thenReturn(mockedSnapshot);
//		
//		final SnapshotVersionResponse mockedSnapshotVersion = new SnapshotVersionResponse();
//		mockedSnapshotVersion.setStatus(status);
//		
//		PowerMockito.when(mockClientServiceImpl.checkSnapshotVersion(1588557288037l, authResponse.getToken())).thenReturn(mockResponseSnapshotVersion);
//		PowerMockito.when(mockResponseSnapshotVersion.readEntity(SnapshotVersionResponse.class)).thenReturn(mockedSnapshotVersion);
//		
//		ClientServiceFacade.getInstance().setClientWS(mockClientServiceImpl);
	}
	
	private void removeFixture() {
		final File generatedFixture = new File(SNAPSHOTS_LOCAL + "/generated_default.json");
		
		if (generatedFixture.exists()) {
			generatedFixture.delete();
		}
	}
	
	@Test
	public void shouldInvokeResolveSnapshotWithNoErrors() throws Exception {
//		//given
//		ClientWSImpl clientService = new ClientWSImpl();
//		
//		final AuthResponse authResponse = new AuthResponse();
//		authResponse.setExp(SwitcherUtils.addTimeDuration("60s", new Date()).getTime()/1000);
//		authResponse.setToken("123lkjsuoi23487skjfh28dskjn29");
//		
//		Client clientMock = PowerMockito.mock(Client.class);
//		WebTarget webTargetMock = PowerMockito.mock(WebTarget.class);
//		Builder buildHeaderMock = PowerMockito.mock(Builder.class);
//		Builder buildPostMock = PowerMockito.mock(Builder.class);
//		Response responseMock = PowerMockito.mock(Response.class);
//
//		PowerMockito.when(clientMock.target(String.format(ClientWS.SNAPSHOT_URL, this.properties.get(SwitcherContextParam.URL)))).thenReturn(webTargetMock);
//		PowerMockito.when(webTargetMock.request(MediaType.APPLICATION_JSON)).thenReturn(buildHeaderMock);
//		PowerMockito.when(buildHeaderMock.header(
//				ClientWS.HEADER_AUTHORIZATION, String.format(ClientWS.TOKEN_TEXT, authResponse.getToken()))).thenReturn(buildPostMock);
//		
//		final StringBuilder query = new StringBuilder();
//		query.append("{\"query\":\"{ domain(name: \\\"%s\\\", environment: \\\"%s\\\", _component: \\\"%s\\\") { ");
//		query.append("name version description activated ");
//		query.append("group { name description activated ");
//		query.append("config { key description activated ");
//		query.append("strategies { strategy activated operation values } ");
//		query.append("components } } } }\"}");
//		
//		final String mockQuery = String.format(query.toString(), "switcher-domain", "generated_default", "switcher-client");
//		final Entity<String> mockEntityQuery = Entity.json(mockQuery);
//		PowerMockito.when(buildPostMock.post(mockEntityQuery)).thenReturn(responseMock);
//		PowerMockito.when(responseMock.getStatus()).thenReturn(200);
//		
//		PowerMockito.mockStatic(Entity.class);
//		PowerMockito.when(Entity.json(mockQuery)).thenReturn(mockEntityQuery);
//		
//		clientService.setClient(clientMock);
//
//		//test
//		assertEquals(200, clientService.resolveSnapshot(authResponse.getToken()).getStatus());
	}
	
	@Test
	public void shouldInvokeResolveSnapshotWithErrors() throws Exception {
//		//given
//		ClientWSImpl clientService = new ClientWSImpl();
//		
//		final AuthResponse authResponse = new AuthResponse();
//		authResponse.setExp(SwitcherUtils.addTimeDuration("2s", new Date()).getTime()/1000);
//		authResponse.setToken("123lkjsuoi23487skjfh28dskjn29");
//		
//		WebTarget webTargetMock = PowerMockito.mock(WebTarget.class);
//		Client clientMock = PowerMockito.mock(Client.class);
//		Builder builderMock = PowerMockito.mock(Builder.class);
//
//		PowerMockito.when(clientMock.target(String.format(ClientWS.SNAPSHOT_URL, properties.get(SwitcherContextParam.URL)))).thenReturn(webTargetMock);
//		PowerMockito.when(webTargetMock.request(MediaType.APPLICATION_JSON)).thenReturn(builderMock);
//		PowerMockito.when(builderMock.header(
//				ClientWS.HEADER_AUTHORIZATION, String.format(ClientWS.TOKEN_TEXT, authResponse.getToken()))).thenReturn(builderMock);
//		
//		final StringBuilder query = new StringBuilder();
//		query.append("{\"query\":\"{ domain(name: \\\"%s\\\", environment: \\\"%s\\\") { ");
//		query.append("name version description activated ");
//		query.append("group { name description activated ");
//		query.append("config { key description activated ");
//		query.append("strategies { strategy activated operation values } ");
//		query.append("components } } } }\"}");
//		
//		PowerMockito.when(builderMock.post(Entity.json(String.format(query.toString(), "switcher-domain", "generated_default"))))
//			.thenThrow(ResponseProcessingException.class);
//		clientService.setClient(clientMock);
//		
//		ClientServiceFacade.getInstance().setClientService(clientService);
//		
//		//test
//		SwitcherFactory.buildContext(properties, true);
	}
	
	@Test
	public void offlineShouldLoadSnapshotFromAPIBeforeExecuting() throws Exception {
//		this.generateLoaderMock(200);
//		SwitcherFactory.buildContext(properties, true);
//		final Switcher switcher = SwitcherFactory.getSwitcher("USECASE11");
//		
//		assertTrue(switcher.isItOn());
//		this.removeFixture();
	}
	
	@Test
	public void offlineShouldNotLoadSnapshotFromAPI_unauthorizedAPIaccess() throws Exception {
//		this.generateLoaderMock(401);
//		SwitcherFactory.buildContext(properties, true);
//		final Switcher switcher = SwitcherFactory.getSwitcher("USECASE11");
//		
//		try {
//			switcher.isItOn();
//		} catch (Exception e) {
//			assertEquals("Something went wrong: Unauthorized API access", e.getMessage());
//			throw e;
//		}
	}
	
	@Test
	public void shouldLoadDomainFromSnapshot() throws Exception {
		final Domain domain = SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		assertNotNull(domain);
		assertNotNull(domain.toString());
	}
	
	@Test
	public void shouldNotLoadDomainFromDefectSnapshot_byFile() throws Exception {
		SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/defect_default.json");
	}
	
	@Test
	public void shouldNotLoadDomainFromDefectSnapshot_byEnv() throws Exception {
		SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL, "defect_default");
	}
	
	@Test
	public void shouldReturnSnapshotUpdated() throws Exception {
//		this.generateSnapshotVersionMock(true);
//		SwitcherFactory.buildContext(properties, true);
//		SwitcherFactory.validateSnapshot();
//		final Switcher switcher = SwitcherFactory.getSwitcher("USECASE11");
//		
//		assertTrue(switcher.isItOn());
//		this.removeFixture();
	}
	
	@Test
	public void shouldReturnSnapshotNotUpdated() throws Exception {
//		this.generateSnapshotVersionMock(false);
//		SwitcherFactory.buildContext(properties, true);
//		SwitcherFactory.validateSnapshot();
//		final Switcher switcher = SwitcherFactory.getSwitcher("USECASE11");
//		
//		assertTrue(switcher.isItOn());
//		this.removeFixture();
	}
	
	@Test
	public void shouldNotSaveSnapshot() throws Exception {
//		this.properties.put(SwitcherContextParam.ENVIRONMENT, "cause_error/generated_default");
//		this.generateSnapshotVersionMock(false);
//		SwitcherFactory.buildContext(properties, true);
	}
	
	@Test
	public void shouldReturnException_ContextNotInitialized() throws Exception {
//		Field field = PowerMockito.field(SwitcherFactory.class, "instance");
//        field.set(PowerMockito.class, null);
//		SwitcherFactory.validateSnapshot();
	}
	
	@Test
	public void shouldInvokeCheckSnapshotVersionWithNoErrors() throws Exception {
//		//given
//		ClientWSImpl clientService = new ClientWSImpl();
//		
//		final AuthResponse authResponse = new AuthResponse();
//		authResponse.setExp(SwitcherUtils.addTimeDuration("60s", new Date()).getTime()/1000);
//		authResponse.setToken("123lkjsuoi23487skjfh28dskjn29");
//		this.properties.put(ClientWS.AUTH_RESPONSE, authResponse);
//		
//		Client clientMock = PowerMockito.mock(Client.class);
//		WebTarget webTargetMock = PowerMockito.mock(WebTarget.class);
//		Builder buildHeaderMock = PowerMockito.mock(Builder.class);
//		Builder builderMock = PowerMockito.mock(Builder.class);
//		Response responseMock = PowerMockito.mock(Response.class);
//
//		PowerMockito.when(clientMock.target(String.format(ClientWS.SNAPSHOT_VERSION_CHECK, properties.get(SwitcherContextParam.URL), 1l))).thenReturn(webTargetMock);
//		PowerMockito.when(webTargetMock.request(MediaType.APPLICATION_JSON)).thenReturn(buildHeaderMock);
//		PowerMockito.when(buildHeaderMock.header(
//				ClientWS.HEADER_AUTHORIZATION, String.format(ClientWS.TOKEN_TEXT, 
//						((AuthResponse) this.properties.get(ClientWS.AUTH_RESPONSE)).getToken()))).thenReturn(builderMock);
//		
//		PowerMockito.when(builderMock.get()).thenReturn(responseMock);
//		
//		clientService.setClient(clientMock);
//		ClientServiceFacade.getInstance().setClientWS(clientService);
//
//		//test
//		assertNotNull(clientService.checkSnapshotVersion(this.properties, 1l));
	}
	
	@Test
	public void shouldInvokeCheckSnapshotVersionWithErrors() throws Exception {
//		//given
//		final ClientWS mockClientServiceImpl = PowerMockito.mock(ClientWS.class);
//		final Response mockResponseAuth = PowerMockito.mock(Response.class);
//		final Response mockResponseResolveSnapshot = PowerMockito.mock(Response.class);
//		
//		final AuthResponse authResponse = new AuthResponse();
//		authResponse.setExp(SwitcherUtils.addTimeDuration("2s", new Date()).getTime()/1000);
//		authResponse.setToken("123lkjsuoi23487skjfh28dskjn29");
//		
//		PowerMockito.when(mockClientServiceImpl.isAlive(properties)).thenReturn(true);
//		
//		PowerMockito.when(mockClientServiceImpl.auth(this.properties)).thenReturn(mockResponseAuth);
//		PowerMockito.when(mockResponseAuth.readEntity(AuthResponse.class)).thenReturn(authResponse);
//		
//		final Snapshot mockedSnapshot = new Snapshot();
//		final Criteria criteria = new Criteria();
//		criteria.setDomain(SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/default.json"));
//		mockedSnapshot.setData(criteria);
//		
//		PowerMockito.when(mockClientServiceImpl.resolveSnapshot(this.properties)).thenReturn(mockResponseResolveSnapshot);
//		PowerMockito.when(mockResponseResolveSnapshot.readEntity(Snapshot.class)).thenReturn(mockedSnapshot);
//		PowerMockito.when(mockClientServiceImpl.checkSnapshotVersion(this.properties, 1588557288037l)).thenThrow(ResponseProcessingException.class);
//		
//		ClientServiceFacade.getInstance().setClientWS(mockClientServiceImpl);
//		
//		SwitcherFactory.buildContext(this.properties, true);
//		this.removeFixture();
//		
//		//test
//		try {
//			SwitcherFactory.validateSnapshot();
//		} catch (Exception e) {
//			assertEquals(
//					String.format(
//							"Something went wrong: It was not possible to reach the Switcher-API on this endpoint: %s", 
//							properties.get(SwitcherContextParam.URL)), 
//					e.getMessage());
//			throw e;
//		} finally {
//			ClientServiceFacade.getInstance().setClientWS(null);
//		}
	}
	
	@Test
	public void shouldInvokeCheckSnapshotVersionWithException() throws Exception {
//		//given
//		final ClientWS mockClientServiceImpl = PowerMockito.mock(ClientWS.class);
//		final Response mockResponseAuth = PowerMockito.mock(Response.class);
//		
//		final AuthResponse authResponse = new AuthResponse();
//		authResponse.setExp(SwitcherUtils.addTimeDuration("2s", new Date()).getTime()/1000);
//		authResponse.setToken("123lkjsuoi23487skjfh28dskjn29");
//		
//		PowerMockito.when(mockClientServiceImpl.isAlive(properties)).thenReturn(true);
//		
//		PowerMockito.when(mockClientServiceImpl.auth(this.properties)).thenReturn(mockResponseAuth);
//		PowerMockito.when(mockResponseAuth.readEntity(AuthResponse.class)).thenReturn(authResponse);
//		
//		final Snapshot mockedSnapshot = new Snapshot();
//		final Criteria criteria = new Criteria();
//		criteria.setDomain(SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/default.json"));
//		mockedSnapshot.setData(criteria);
//		
//		PowerMockito.when(mockClientServiceImpl.resolveSnapshot(this.properties)).thenThrow(ResponseProcessingException.class);
//		ClientServiceFacade.getInstance().setClientWS(mockClientServiceImpl);
//		
//		SwitcherFactory.buildContext(this.properties, true);
//		this.removeFixture();
//		
//		//test
//		try {
//			SwitcherFactory.validateSnapshot();
//		} catch (Exception e) {
//			assertEquals(
//					String.format(
//							"Something went wrong: It was not possible to reach the Switcher-API on this endpoint: %s", 
//							properties.get(SwitcherContextParam.URL)), 
//					e.getMessage());
//			throw e;
//		} finally {
//			ClientServiceFacade.getInstance().setClientWS(null);
//		}
	}
	
	@Test
	public void shouldInvokeCheckSnapshotVersionWithErrors_unauthorizedAPIaccess() throws Exception {
//		//given
//		ClientWS mockClientServiceImpl = PowerMockito.mock(ClientWS.class);
//		Response mockResponseAuth = PowerMockito.mock(Response.class);
//		final Response mockResponseResolveSnapshot = PowerMockito.mock(Response.class);
//		
//		final AuthResponse authResponse = new AuthResponse();
//		authResponse.setExp(SwitcherUtils.addTimeDuration("2s", new Date()).getTime()/1000);
//		authResponse.setToken("123lkjsuoi23487skjfh28dskjn29");
//		
//		PowerMockito.when(mockClientServiceImpl.isAlive(properties)).thenReturn(true);
//		
//		PowerMockito.when(mockClientServiceImpl.auth(this.properties)).thenReturn(mockResponseAuth);
//		PowerMockito.when(mockResponseAuth.readEntity(AuthResponse.class)).thenReturn(authResponse);
//		
//		final Snapshot mockedSnapshot = new Snapshot();
//		final Criteria criteria = new Criteria();
//		criteria.setDomain(SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/default.json"));
//		mockedSnapshot.setData(criteria);
//		
//		PowerMockito.when(mockClientServiceImpl.resolveSnapshot(this.properties)).thenReturn(mockResponseResolveSnapshot);
//		PowerMockito.when(mockResponseResolveSnapshot.readEntity(Snapshot.class)).thenReturn(mockedSnapshot);
//		PowerMockito.when(mockClientServiceImpl.checkSnapshotVersion(this.properties, 1588557288037l)).thenThrow(ResponseProcessingException.class);
//		
//		ClientServiceFacade.getInstance().setClientWS(mockClientServiceImpl);
//		
//		SwitcherFactory.buildContext(this.properties, true);
//		this.removeFixture();
//		
//		PowerMockito.when(mockResponseAuth.getStatus()).thenReturn(401);
//		
//		ClientServiceFacade.getInstance().setClientWS(mockClientServiceImpl);
//		Awaitility.await().pollDelay(2, TimeUnit.SECONDS).until(() -> true);
//		
//		//test
//		try {
//			SwitcherFactory.validateSnapshot();
//		} catch (Exception e) {
//			assertEquals("Something went wrong: Unauthorized API access", e.getMessage());
//			throw e;
//		} finally {
//			ClientServiceFacade.getInstance().setClientWS(null);
//		}
	}

}
