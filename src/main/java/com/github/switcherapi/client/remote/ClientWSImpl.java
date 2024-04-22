package com.github.switcherapi.client.remote;

import com.github.switcherapi.client.SwitcherContextBase;
import com.github.switcherapi.client.exception.SwitcherRemoteException;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.github.switcherapi.client.model.criteria.SwitchersCheck;
import com.github.switcherapi.client.model.response.AuthRequest;
import com.github.switcherapi.client.model.response.AuthResponse;
import com.github.switcherapi.client.model.response.CriteriaResponse;
import com.github.switcherapi.client.model.response.SnapshotVersionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
	
	private final Client client;
	
	public ClientWSImpl() {
		final int timeoutMs = Integer.parseInt(SwitcherContextBase.contextStr(ContextKey.TIMEOUT_MS));
		this.client = ClientWSBuilder.builder()
				.readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
				.connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
				.build();
	}
	
	@Override
	public CriteriaResponse executeCriteriaService(final Switcher switcher, final String token) {
		SwitcherUtils.debug(logger, "switcher: {}", switcher);
		
		final String url = SwitcherContextBase.contextStr(ContextKey.URL);
		final WebTarget myResource = client.target(String.format(CRITERIA_URL, url))
				.queryParam(Switcher.KEY, switcher.getSwitcherKey())
				.queryParam(Switcher.SHOW_REASON, switcher.isShowReason())
				.queryParam(Switcher.BYPASS_METRIC, switcher.isBypassMetrics());

		try {
			final Response response = myResource.request(MediaType.APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, String.format(TOKEN_TEXT, token))
					.post(Entity.json(switcher.getInputRequest()));

			if (response.getStatus() == 200) {
				final CriteriaResponse criteriaResponse = response.readEntity(CriteriaResponse.class);
				criteriaResponse.setSwitcherKey(switcher.getSwitcherKey());
				criteriaResponse.setEntry(switcher.getEntry());
				response.close();

				return criteriaResponse;
			}

			throw new SwitcherRemoteException(url, response.getStatus());
		} catch (Exception e) {
			throw new SwitcherRemoteException(url, e);
		}
	}
	
	@Override
	public Optional<AuthResponse> auth() {
		final AuthRequest authRequest = new AuthRequest();
		authRequest.setDomain(SwitcherContextBase.contextStr(ContextKey.DOMAIN));
		authRequest.setComponent(SwitcherContextBase.contextStr(ContextKey.COMPONENT));
		authRequest.setEnvironment(SwitcherContextBase.contextStr(ContextKey.ENVIRONMENT));

		final String url = SwitcherContextBase.contextStr(ContextKey.URL);
		final WebTarget myResource = client.target(String.format(AUTH_URL, url));

		try {
			final Response response = myResource.request(MediaType.APPLICATION_JSON)
					.header(HEADER_APIKEY, SwitcherContextBase.contextStr(ContextKey.APIKEY))
					.post(Entity.json(authRequest));

			if (response.getStatus() == 200) {
				Optional<AuthResponse> authResponse = Optional.of(response.readEntity(AuthResponse.class));
				response.close();

				return authResponse;
			}

			throw new SwitcherRemoteException(url, response.getStatus());
		} catch (Exception e) {
			throw new SwitcherRemoteException(url, e);
		}
	}
	
	@Override
	public Snapshot resolveSnapshot(final String token) {
		final String url = SwitcherContextBase.contextStr(ContextKey.URL);
		final WebTarget myResource = client.target(String.format(SNAPSHOT_URL, url));
		
		final Response response = myResource.request(MediaType.APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, String.format(TOKEN_TEXT, token))
				.post(Entity.json(String.format(QUERY, 
						SwitcherContextBase.contextStr(ContextKey.DOMAIN),
						SwitcherContextBase.contextStr(ContextKey.ENVIRONMENT), 
						SwitcherContextBase.contextStr(ContextKey.COMPONENT))));

		if (response.getStatus() == 200) {
			final Snapshot snapshot = response.readEntity(Snapshot.class);
			response.close();

			return snapshot;
		}

		throw new SwitcherRemoteException(url, response.getStatus());
	}
	
	@Override
	public SnapshotVersionResponse checkSnapshotVersion(final long version, final String token) {
		final String url = SwitcherContextBase.contextStr(ContextKey.URL);
		final WebTarget myResource = client.target(String.format(SNAPSHOT_VERSION_CHECK, url, version));
		
		final Response response = myResource.request(MediaType.APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, String.format(TOKEN_TEXT, token))
				.get();

		if (response.getStatus() == 200) {
			final SnapshotVersionResponse snapshotVersionResponse = response.readEntity(SnapshotVersionResponse.class);
			response.close();

			return snapshotVersionResponse;
		}

		throw new SwitcherRemoteException(url, response.getStatus());
	}
	
	@Override
	public boolean isAlive() {
		try {
			final WebTarget myResource = client.target(String.format(CHECK_URL, 
					SwitcherContextBase.contextStr(ContextKey.URL)));
			final Response response = myResource.request(MediaType.APPLICATION_JSON).get();
			return response.getStatus() == 200;
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public SwitchersCheck checkSwitchers(Set<String> switchers, final String token) {
		final String url = SwitcherContextBase.contextStr(ContextKey.URL);
		final WebTarget myResource = client.target(String.format(CHECK_SWITCHERS, url));
		
		final Response response = myResource.request(MediaType.APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, String.format(TOKEN_TEXT, token))
				.post(Entity.json(new SwitchersCheck(switchers)));

		if (response.getStatus() == 200) {
			final SwitchersCheck switchersResponse = response.readEntity(SwitchersCheck.class);
			response.close();

			return switchersResponse;
		}

		throw new SwitcherRemoteException(url, response.getStatus());
	}

}
