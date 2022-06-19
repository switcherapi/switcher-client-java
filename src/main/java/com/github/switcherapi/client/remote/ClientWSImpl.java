package com.github.switcherapi.client.remote;

import java.util.Optional;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.SwitcherContext;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherKeyNotAvailableForComponentException;
import com.github.switcherapi.client.exception.SwitcherKeyNotFoundException;
import com.github.switcherapi.client.exception.SwitcherSnapshoException;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.SwitcherProperties;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.github.switcherapi.client.model.criteria.SwitchersCheck;
import com.github.switcherapi.client.model.response.AuthRequest;
import com.github.switcherapi.client.model.response.AuthResponse;
import com.github.switcherapi.client.model.response.CriteriaResponse;
import com.github.switcherapi.client.model.response.SnapshotVersionResponse;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class ClientWSImpl implements ClientWS {
	
	private static final Logger logger = LogManager.getLogger(ClientWSImpl.class);
	
	public static final String QUERY = 
			"{\"query\":\"{ domain(name: \\\"%s\\\", environment: \\\"%s\\\", _component: \\\"%s\\\") { " +
			"name version description activated " +
			"group { name description activated " +
			"config { key description activated " +
			"strategies { strategy activated operation values } " +
			"components } } } }\"}";
	
	private Client client;
	
	public ClientWSImpl() {
		this.client = ClientBuilder.newClient();
	}
	
	@Override
	public CriteriaResponse executeCriteriaService(final Switcher switcher, final String token) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("switcher: %s", switcher));
		}
		
		final String url = SwitcherContext.getProperties().getUrl();
		final WebTarget myResource = client.target(String.format(CRITERIA_URL, url))
				.queryParam(Switcher.KEY, switcher.getSwitcherKey())
				.queryParam(Switcher.SHOW_REASON, switcher.isShowReason())
				.queryParam(Switcher.BYPASS_METRIC, switcher.isBypassMetrics());
		
		final Response response = myResource.request(MediaType.APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, String.format(TOKEN_TEXT, token))
				.post(Entity.json(switcher.getInputRequest()));
		
		if (response.getStatus() == 401) {
			throw new SwitcherKeyNotAvailableForComponentException(
					SwitcherContext.getProperties().getComponent(), switcher.getSwitcherKey());
		} else if (response.getStatus() != 200) {
			throw new SwitcherKeyNotFoundException(switcher.getSwitcherKey());
		}
		
		final CriteriaResponse criteriaReponse = response.readEntity(CriteriaResponse.class);
		criteriaReponse.setSwitcherKey(switcher.getSwitcherKey());
		criteriaReponse.setEntry(switcher.getEntry());
		response.close();
		
		return criteriaReponse;
	}
	
	@Override
	public Optional<AuthResponse> auth() {
		final SwitcherProperties properties = SwitcherContext.getProperties();
		final AuthRequest authRequest = new AuthRequest();
		authRequest.setDomain(properties.getDomain());
		authRequest.setComponent(properties.getComponent());
		authRequest.setEnvironment(properties.getEnvironment());

		final WebTarget myResource = client.target(String.format(AUTH_URL, properties.getUrl()));
		
		final Response response = myResource.request(MediaType.APPLICATION_JSON)
				.header(HEADER_APIKEY, properties.getApiKey())
				.post(Entity.json(authRequest));
		
		if (response.getStatus() == 401) {
			throw new SwitcherException("Unauthorized API access", null); 
		}
		
		Optional<AuthResponse> authResponse = Optional.of(response.readEntity(AuthResponse.class));
		response.close();
		
		return authResponse;
	}
	
	@Override
	public Snapshot resolveSnapshot(final String token) {
		final SwitcherProperties properties = SwitcherContext.getProperties();
		final WebTarget myResource = client.target(String.format(SNAPSHOT_URL, properties.getUrl()));
		
		final Response response = myResource.request(MediaType.APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, String.format(TOKEN_TEXT, token))
				.post(Entity.json(String.format(QUERY, 
						properties.getDomain(), properties.getEnvironment(), properties.getComponent())));
		
		if (response.getStatus() != 200) {
			throw new SwitcherSnapshoException("resolveSnapshot");
		}
		
		final Snapshot snapshot = response.readEntity(Snapshot.class);
		response.close();
		
		return snapshot;
	}
	
	@Override
	public SnapshotVersionResponse checkSnapshotVersion(final long version, final String token) {
		final SwitcherProperties properties = SwitcherContext.getProperties();
		final WebTarget myResource = 
				client.target(String.format(SNAPSHOT_VERSION_CHECK, properties.getUrl(), version));
		
		final Response response = myResource.request(MediaType.APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, String.format(TOKEN_TEXT, token))
				.get();
		
		if (response.getStatus() != 200) {
			throw new SwitcherSnapshoException("resolveSnapshot");
		}
		
		final SnapshotVersionResponse snapshotVersionResponse = response.readEntity(SnapshotVersionResponse.class);
		response.close();
		
		return snapshotVersionResponse;
	}
	
	@Override
	public boolean isAlive() {
		try {
			final SwitcherProperties properties = SwitcherContext.getProperties();
			final WebTarget myResource = client.target(String.format(CHECK_URL, properties.getUrl()));
			final Response response = myResource.request(MediaType.APPLICATION_JSON).get();
			return response.getStatus() == 200;
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public SwitchersCheck checkSwitchers(Set<String> switchers, final String token) {
		final SwitcherProperties properties = SwitcherContext.getProperties();
		final WebTarget myResource = client.target(String.format(CHECK_SWITCHERS, properties.getUrl()));
		
		final Response response = myResource.request(MediaType.APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, String.format(TOKEN_TEXT, token))
				.post(Entity.json(new SwitchersCheck(switchers)));
		
		if (response.getStatus() != 200) {
			throw new SwitcherException(
					String.format("API returned an HTTP/1.1 %s", response.getStatus()), null); 
		}
			
		final SwitchersCheck switchersResponse = response.readEntity(SwitchersCheck.class);
		response.close();
		
		return switchersResponse;
	}

}
