package com.github.switcherapi.client.ws;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.configuration.SwitcherContext;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.SwitcherProperties;
import com.github.switcherapi.client.model.response.AuthRequest;

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
		this.setClient(ClientBuilder.newClient());
	}
	
	@Override
	public Response executeCriteriaService(final Switcher switcher, final String token) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("switcher: %s", switcher));
		}
		
		final String url = SwitcherContext.getProperties().getUrl();
		final WebTarget myResource = client.target(String.format(CRITERIA_URL, url))
				.queryParam(Switcher.KEY, switcher.getSwitcherKey())
				.queryParam(Switcher.SHOW_REASON, switcher.isShowReason())
				.queryParam(Switcher.BYPASS_METRIC, switcher.isBypassMetrics());
		
		return myResource.request(MediaType.APPLICATION_JSON)
			.header(HEADER_AUTHORIZATION, String.format(TOKEN_TEXT, token))
			.post(Entity.json(switcher.getInputRequest()));
	}
	
	@Override
	public Response auth() {
		final SwitcherProperties properties = SwitcherContext.getProperties();
		final AuthRequest authRequest = new AuthRequest();
		authRequest.setDomain(properties.getDomain());
		authRequest.setComponent(properties.getComponent());
		authRequest.setEnvironment(properties.getEnvironment());

		final WebTarget myResource = client.target(String.format(AUTH_URL, properties.getUrl()));
		
		return myResource.request(MediaType.APPLICATION_JSON)
			.header(HEADER_APIKEY, properties.getApiKey())
			.post(Entity.json(authRequest));
	}
	
	@Override
	public Response resolveSnapshot(final String token) {
		final SwitcherProperties properties = SwitcherContext.getProperties();
		final WebTarget myResource = client.target(String.format(SNAPSHOT_URL, properties.getUrl()));
		
		return myResource.request(MediaType.APPLICATION_JSON)
			.header(HEADER_AUTHORIZATION, String.format(TOKEN_TEXT, token))
			.post(Entity.json(String.format(QUERY, 
					properties.getDomain(), properties.getEnvironment(), properties.getComponent())));
	}
	
	@Override
	public Response checkSnapshotVersion(final long version, final String token) {
		final SwitcherProperties properties = SwitcherContext.getProperties();
		final WebTarget myResource = 
				client.target(String.format(SNAPSHOT_VERSION_CHECK, properties.getUrl(), version));
		
		return myResource.request(MediaType.APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, String.format(TOKEN_TEXT, token))
				.get();
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

	public void setClient(Client client) {
		this.client = client;
	}

}
