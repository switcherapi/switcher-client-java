package com.github.switcherapi.client.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.ResponseProcessingException;
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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.github.switcherapi.client.SwitcherFactory;
import com.github.switcherapi.client.exception.SwitcherAPIConnectionException;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherFactoryContextException;
import com.github.switcherapi.client.exception.SwitcherSnapshotLoadException;
import com.github.switcherapi.client.exception.SwitcherSnapshotWriteException;
import com.github.switcherapi.client.facade.ClientServiceFacade;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.Criteria;
import com.github.switcherapi.client.model.criteria.Domain;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.github.switcherapi.client.model.response.AuthResponse;
import com.github.switcherapi.client.model.response.SnapshotVersionResponse;
import com.github.switcherapi.client.service.ClientService;
import com.github.switcherapi.client.service.ClientServiceImpl;

@PowerMockIgnore({"javax.management.*", "org.apache.log4j.*", "javax.xml.*", "javax.script.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({Entity.class})
public class SnapshotLoaderTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	private Map<String, Object> properties;
	
	@Before
	public void setupContext() {

		properties = new HashMap<String, Object>();
		properties.put(SwitcherContextParam.URL, "http://localhost:3000");
		properties.put(SwitcherContextParam.APIKEY, "$2b$08$S2Wj/wG/Rfs3ij0xFbtgveDtyUAjML1/TOOhocDg5dhOaU73CEXfK");
		properties.put(SwitcherContextParam.DOMAIN, "switcher-domain");
		properties.put(SwitcherContextParam.COMPONENT, "switcher-client");
		properties.put(SwitcherContextParam.ENVIRONMENT, "generated_default");
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL);
		properties.put(SwitcherContextParam.SNAPSHOT_AUTO_LOAD, true);
		
		this.removeFixture();
	}
	
	/**
	 * The premisses for this mock scenario are:
	 * 
	 * <li> Auth token will expire in 2s.
	 * <li> Snapshot will be loaded from {@link #SNAPSHOTS_LOCAL}/default.json file.
	 */
	private void generateLoaderMock(final int authStatus) throws Exception {
		
		final ClientService mockClientServiceImpl = PowerMockito.mock(ClientService.class);
		final Response mockResponseAuth = PowerMockito.mock(Response.class);
		final Response mockResponseResolveSnapshot = PowerMockito.mock(Response.class);
		
		final AuthResponse authResponse = new AuthResponse();
		authResponse.setExp(SwitcherUtils.addTimeDuration("2s", new Date()).getTime()/1000);
		authResponse.setToken("123lkjsuoi23487skjfh28dskjn29");
		
		PowerMockito.when(mockClientServiceImpl.auth(this.properties)).thenReturn(mockResponseAuth);
		PowerMockito.when(mockResponseAuth.getStatus()).thenReturn(authStatus);
		PowerMockito.when(mockResponseAuth.readEntity(AuthResponse.class)).thenReturn(authResponse);
		
		final Snapshot mockedSnapshot = new Snapshot();
		final Criteria criteria = new Criteria();
		criteria.setDomain(SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/default.json"));
		mockedSnapshot.setData(criteria);
		
		PowerMockito.when(mockClientServiceImpl.resolveSnapshot(this.properties)).thenReturn(mockResponseResolveSnapshot);
		PowerMockito.when(mockResponseResolveSnapshot.readEntity(Snapshot.class)).thenReturn(mockedSnapshot);
		
		ClientServiceFacade.getInstance().setClientService(mockClientServiceImpl);
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
		
		final ClientService mockClientServiceImpl = PowerMockito.mock(ClientService.class);
		final Response mockResponseAuth = PowerMockito.mock(Response.class);
		final Response mockResponseResolveSnapshot = PowerMockito.mock(Response.class);
		final Response mockResponseSnapshotVersion = PowerMockito.mock(Response.class);
		
		final AuthResponse authResponse = new AuthResponse();
		authResponse.setExp(SwitcherUtils.addTimeDuration("2s", new Date()).getTime()/1000);
		authResponse.setToken("123lkjsuoi23487skjfh28dskjn29");
		
		PowerMockito.when(mockClientServiceImpl.auth(this.properties)).thenReturn(mockResponseAuth);
		PowerMockito.when(mockResponseAuth.readEntity(AuthResponse.class)).thenReturn(authResponse);
		
		final Snapshot mockedSnapshot = new Snapshot();
		final Criteria criteria = new Criteria();
		criteria.setDomain(SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/default.json"));
		mockedSnapshot.setData(criteria);
		
		PowerMockito.when(mockClientServiceImpl.resolveSnapshot(this.properties)).thenReturn(mockResponseResolveSnapshot);
		PowerMockito.when(mockResponseResolveSnapshot.readEntity(Snapshot.class)).thenReturn(mockedSnapshot);
		
		final SnapshotVersionResponse mockedSnapshotVersion = new SnapshotVersionResponse();
		mockedSnapshotVersion.setStatus(status);
		
		PowerMockito.when(mockClientServiceImpl.checkSnapshotVersion(this.properties, 1588557288037l)).thenReturn(mockResponseSnapshotVersion);
		PowerMockito.when(mockResponseSnapshotVersion.readEntity(SnapshotVersionResponse.class)).thenReturn(mockedSnapshotVersion);
		
		ClientServiceFacade.getInstance().setClientService(mockClientServiceImpl);
	}
	
	private void removeFixture() {
		final File generatedFixture = new File(SNAPSHOTS_LOCAL + "/generated_default.json");
		
		if (generatedFixture.exists()) {
			generatedFixture.delete();
		}
	}
	
	@Test
	public void shouldInvokeResolveSnapshotWithNoErrors() throws Exception {
		//given
		ClientServiceImpl clientService = new ClientServiceImpl();
		
		final AuthResponse authResponse = new AuthResponse();
		authResponse.setExp(SwitcherUtils.addTimeDuration("60s", new Date()).getTime()/1000);
		authResponse.setToken("123lkjsuoi23487skjfh28dskjn29");
		this.properties.put(ClientService.AUTH_RESPONSE, authResponse);
		
		Client clientMock = PowerMockito.mock(Client.class);
		WebTarget webTargetMock = PowerMockito.mock(WebTarget.class);
		Builder buildHeaderMock = PowerMockito.mock(Builder.class);
		Builder buildPostMock = PowerMockito.mock(Builder.class);
		Response responseMock = PowerMockito.mock(Response.class);

		PowerMockito.when(clientMock.target(String.format(ClientService.SNAPSHOT_URL, this.properties.get(SwitcherContextParam.URL)))).thenReturn(webTargetMock);
		PowerMockito.when(webTargetMock.request(MediaType.APPLICATION_JSON)).thenReturn(buildHeaderMock);
		PowerMockito.when(buildHeaderMock.header(
				ClientService.HEADER_AUTHORIZATION, String.format(ClientService.TOKEN_TEXT, 
						((AuthResponse) this.properties.get(ClientService.AUTH_RESPONSE)).getToken()))).thenReturn(buildPostMock);
		
		final StringBuilder query = new StringBuilder();
		query.append("{\"query\":\"{ domain(name: \\\"%s\\\", environment: \\\"%s\\\") { ");
		query.append("name version description activated ");
		query.append("group { name description activated ");
		query.append("config { key description activated ");
		query.append("strategies { strategy activated operation values } ");
		query.append("components } } } }\"}");
		
		final String mockQuery = String.format(query.toString(), "switcher-domain", "generated_default");
		final Entity<String> mockEntityQuery = Entity.json(mockQuery);
		PowerMockito.when(buildPostMock.post(mockEntityQuery)).thenReturn(responseMock);
		PowerMockito.when(responseMock.getStatus()).thenReturn(200);
		
		PowerMockito.mockStatic(Entity.class);
		PowerMockito.when(Entity.json(mockQuery)).thenReturn(mockEntityQuery);
		
		clientService.setClient(clientMock);

		//test
		assertEquals(200, clientService.resolveSnapshot(this.properties).getStatus());
	}
	
	@Test(expected = SwitcherAPIConnectionException.class)
	public void shouldInvokeResolveSnapshotWithErrors() throws Exception {
		//given
		ClientServiceImpl clientService = new ClientServiceImpl();
		
		final AuthResponse authResponse = new AuthResponse();
		authResponse.setExp(SwitcherUtils.addTimeDuration("2s", new Date()).getTime()/1000);
		authResponse.setToken("123lkjsuoi23487skjfh28dskjn29");
		properties.put(ClientService.AUTH_RESPONSE, authResponse);
		
		WebTarget webTargetMock = PowerMockito.mock(WebTarget.class);
		Client clientMock = PowerMockito.mock(Client.class);
		Builder builderMock = PowerMockito.mock(Builder.class);

		PowerMockito.when(clientMock.target(String.format(ClientService.SNAPSHOT_URL, properties.get(SwitcherContextParam.URL)))).thenReturn(webTargetMock);
		PowerMockito.when(webTargetMock.request(MediaType.APPLICATION_JSON)).thenReturn(builderMock);
		PowerMockito.when(builderMock.header(
				ClientService.HEADER_AUTHORIZATION, String.format(ClientService.TOKEN_TEXT, 
						((AuthResponse) properties.get(ClientService.AUTH_RESPONSE)).getToken()))).thenReturn(builderMock);
		
		final StringBuilder query = new StringBuilder();
		query.append("{\"query\":\"{ domain(name: \\\"%s\\\", environment: \\\"%s\\\") { ");
		query.append("name version description activated ");
		query.append("group { name description activated ");
		query.append("config { key description activated ");
		query.append("strategies { strategy activated operation values } ");
		query.append("components } } } }\"}");
		
		PowerMockito.when(builderMock.post(Entity.json(String.format(query.toString(), "switcher-domain", "generated_default"))))
			.thenThrow(ResponseProcessingException.class);
		clientService.setClient(clientMock);
		
		ClientServiceFacade.getInstance().setClientService(clientService);
		
		//test
		SwitcherFactory.buildContext(properties, true);
	}
	
	@Test
	public void offlineShouldLoadSnapshotFromAPIBeforeExecuting() throws Exception {
		this.generateLoaderMock(200);
		SwitcherFactory.buildContext(properties, true);
		final Switcher switcher = SwitcherFactory.getSwitcher("USECASE11");
		
		assertTrue(switcher.isItOn());
		this.removeFixture();
	}
	
	@Test(expected = SwitcherException.class)
	public void offlineShouldNotLoadSnapshotFromAPI_unauthorizedAPIaccess() throws Exception {
		this.generateLoaderMock(401);
		SwitcherFactory.buildContext(properties, true);
		final Switcher switcher = SwitcherFactory.getSwitcher("USECASE11");
		
		try {
			switcher.isItOn();
		} catch (Exception e) {
			assertEquals("Something went wrong: Unauthorized API access", e.getMessage());
			throw e;
		}
	}
	
	@Test
	public void shouldLoadDomainFromSnapshot() throws Exception {
		final Domain domain = SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		assertNotNull(domain);
		assertNotNull(domain.toString());
	}
	
	@Test(expected = SwitcherSnapshotLoadException.class)
	public void shouldNotLoadDomainFromDefectSnapshot_byFile() throws Exception {
		SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/defect_default.json");
	}
	
	@Test(expected = SwitcherSnapshotLoadException.class)
	public void shouldNotLoadDomainFromDefectSnapshot_byEnv() throws Exception {
		SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL, "defect_default");
	}
	
	@Test
	public void shouldReturnSnapshotUpdated() throws Exception {
		this.generateSnapshotVersionMock(true);
		SwitcherFactory.buildContext(properties, true);
		SwitcherFactory.validateSnapshot();
		final Switcher switcher = SwitcherFactory.getSwitcher("USECASE11");
		
		assertTrue(switcher.isItOn());
		this.removeFixture();
	}
	
	@Test
	public void shouldReturnSnapshotNotUpdated() throws Exception {
		this.generateSnapshotVersionMock(false);
		SwitcherFactory.buildContext(properties, true);
		SwitcherFactory.validateSnapshot();
		final Switcher switcher = SwitcherFactory.getSwitcher("USECASE11");
		
		assertTrue(switcher.isItOn());
		this.removeFixture();
	}
	
	@Test(expected = SwitcherSnapshotWriteException.class)
	public void shouldNotSaveSnapshot() throws Exception {
		this.properties.put(SwitcherContextParam.ENVIRONMENT, "cause_error/generated_default");
		this.generateSnapshotVersionMock(false);
		SwitcherFactory.buildContext(properties, true);
	}
	
	@Test(expected = SwitcherFactoryContextException.class)
	public void shouldReturnException_ContextNotInitialized() throws Exception {
		Field field = PowerMockito.field(SwitcherFactory.class, "instance");
        field.set(PowerMockito.class, null);
		SwitcherFactory.validateSnapshot();
	}
	
	@Test
	public void shouldInvokeCheckSnapshotVersionWithNoErrors() throws Exception {
		//given
		ClientServiceImpl clientService = new ClientServiceImpl();
		
		final AuthResponse authResponse = new AuthResponse();
		authResponse.setExp(SwitcherUtils.addTimeDuration("60s", new Date()).getTime()/1000);
		authResponse.setToken("123lkjsuoi23487skjfh28dskjn29");
		this.properties.put(ClientService.AUTH_RESPONSE, authResponse);
		
		Client clientMock = PowerMockito.mock(Client.class);
		WebTarget webTargetMock = PowerMockito.mock(WebTarget.class);
		Builder buildHeaderMock = PowerMockito.mock(Builder.class);
		Builder builderMock = PowerMockito.mock(Builder.class);
		Response responseMock = PowerMockito.mock(Response.class);

		PowerMockito.when(clientMock.target(String.format(ClientService.SNAPSHOT_VERSION_CHECK, properties.get(SwitcherContextParam.URL), 1l))).thenReturn(webTargetMock);
		PowerMockito.when(webTargetMock.request(MediaType.APPLICATION_JSON)).thenReturn(buildHeaderMock);
		PowerMockito.when(buildHeaderMock.header(
				ClientService.HEADER_AUTHORIZATION, String.format(ClientService.TOKEN_TEXT, 
						((AuthResponse) this.properties.get(ClientService.AUTH_RESPONSE)).getToken()))).thenReturn(builderMock);
		
		PowerMockito.when(builderMock.get()).thenReturn(responseMock);
		
		clientService.setClient(clientMock);
		ClientServiceFacade.getInstance().setClientService(clientService);

		//test
		assertNotNull(clientService.checkSnapshotVersion(this.properties, 1l));
	}
	
	@Test(expected = SwitcherAPIConnectionException.class)
	public void shouldInvokeCheckSnapshotVersionWithErrors() throws Exception {
		//given
		final ClientService mockClientServiceImpl = PowerMockito.mock(ClientService.class);
		final Response mockResponseAuth = PowerMockito.mock(Response.class);
		final Response mockResponseResolveSnapshot = PowerMockito.mock(Response.class);
		
		final AuthResponse authResponse = new AuthResponse();
		authResponse.setExp(SwitcherUtils.addTimeDuration("2s", new Date()).getTime()/1000);
		authResponse.setToken("123lkjsuoi23487skjfh28dskjn29");
		
		PowerMockito.when(mockClientServiceImpl.auth(this.properties)).thenReturn(mockResponseAuth);
		PowerMockito.when(mockResponseAuth.readEntity(AuthResponse.class)).thenReturn(authResponse);
		
		final Snapshot mockedSnapshot = new Snapshot();
		final Criteria criteria = new Criteria();
		criteria.setDomain(SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/default.json"));
		mockedSnapshot.setData(criteria);
		
		PowerMockito.when(mockClientServiceImpl.resolveSnapshot(this.properties)).thenReturn(mockResponseResolveSnapshot);
		PowerMockito.when(mockResponseResolveSnapshot.readEntity(Snapshot.class)).thenReturn(mockedSnapshot);
		PowerMockito.when(mockClientServiceImpl.checkSnapshotVersion(this.properties, 1588557288037l)).thenThrow(ResponseProcessingException.class);
		
		ClientServiceFacade.getInstance().setClientService(mockClientServiceImpl);
		
		SwitcherFactory.buildContext(this.properties, true);
		this.removeFixture();
		
		//test
		try {
			SwitcherFactory.validateSnapshot();
		} catch (Exception e) {
			assertEquals(
					String.format(
							"Something went wrong: It was not possible to reach the Switcher-API on this endpoint: %s", 
							properties.get(SwitcherContextParam.URL)), 
					e.getMessage());
			throw e;
		} finally {
			ClientServiceFacade.getInstance().setClientService(null);
		}
	}
	
	@Test(expected = SwitcherException.class)
	public void shouldInvokeCheckSnapshotVersionWithErrors_unauthorizedAPIaccess() throws Exception {
		//given
		ClientService mockClientServiceImpl = PowerMockito.mock(ClientService.class);
		Response mockResponseAuth = PowerMockito.mock(Response.class);
		final Response mockResponseResolveSnapshot = PowerMockito.mock(Response.class);
		
		final AuthResponse authResponse = new AuthResponse();
		authResponse.setExp(SwitcherUtils.addTimeDuration("2s", new Date()).getTime()/1000);
		authResponse.setToken("123lkjsuoi23487skjfh28dskjn29");
		
		PowerMockito.when(mockClientServiceImpl.auth(this.properties)).thenReturn(mockResponseAuth);
		PowerMockito.when(mockResponseAuth.readEntity(AuthResponse.class)).thenReturn(authResponse);
		
		final Snapshot mockedSnapshot = new Snapshot();
		final Criteria criteria = new Criteria();
		criteria.setDomain(SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/default.json"));
		mockedSnapshot.setData(criteria);
		
		PowerMockito.when(mockClientServiceImpl.resolveSnapshot(this.properties)).thenReturn(mockResponseResolveSnapshot);
		PowerMockito.when(mockResponseResolveSnapshot.readEntity(Snapshot.class)).thenReturn(mockedSnapshot);
		PowerMockito.when(mockClientServiceImpl.checkSnapshotVersion(this.properties, 1588557288037l)).thenThrow(ResponseProcessingException.class);
		
		ClientServiceFacade.getInstance().setClientService(mockClientServiceImpl);
		
		SwitcherFactory.buildContext(this.properties, true);
		this.removeFixture();
		
		PowerMockito.when(mockResponseAuth.getStatus()).thenReturn(401);
		
		ClientServiceFacade.getInstance().setClientService(mockClientServiceImpl);
		Awaitility.await().pollDelay(2, TimeUnit.SECONDS).until(() -> true);
		
		//test
		try {
			SwitcherFactory.validateSnapshot();
		} catch (Exception e) {
			assertEquals("Something went wrong: Unauthorized API access", e.getMessage());
			throw e;
		} finally {
			ClientServiceFacade.getInstance().setClientService(null);
		}
	}

}
