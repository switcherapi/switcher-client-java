package com.switcherapi.client.remote;

import com.switcherapi.client.SwitcherProperties;
import com.switcherapi.client.exception.SwitcherRemoteException;
import com.switcherapi.client.model.ContextKey;
import com.switcherapi.client.model.criteria.Snapshot;
import com.switcherapi.client.remote.dto.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.switcherapi.client.remote.Constants.*;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class ClientWSImpl implements ClientWS {

	private final SwitcherProperties switcherProperties;

	private final Client client;
	
	public ClientWSImpl(SwitcherProperties switcherProperties, Client client) {
		this.switcherProperties = switcherProperties;
		this.client = client;
	}

	public static ClientWS build(SwitcherProperties switcherProperties, ExecutorService executorService, int timeoutMs) {
		Client client = ClientWSBuilder.builder(executorService, switcherProperties)
				.readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
				.connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
				.build();

		return new ClientWSImpl(switcherProperties, client);
	}

	@Override
	public CriteriaResponse executeCriteria(final CriteriaRequest criteriaRequest, final String token) {
		final String url = switcherProperties.getValue(ContextKey.URL);
		final WebTarget myResource = client.target(String.format(CRITERIA_URL, url))
				.queryParam(KEY, criteriaRequest.getSwitcherKey())
				.queryParam(SHOW_REASON, Boolean.TRUE)
				.queryParam(BYPASS_METRIC, criteriaRequest.isBypassMetric());

		try {
			final Response response = myResource.request(MediaType.APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, String.format(TOKEN_TEXT, token))
					.post(Entity.json(criteriaRequest.getInputRequest()));

			if (response.getStatus() == 200) {
				final CriteriaResponse criteriaResponse = response.readEntity(CriteriaResponse.class);
				criteriaResponse.setSwitcherKey(criteriaRequest.getSwitcherKey());
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
		authRequest.setDomain(switcherProperties.getValue(ContextKey.DOMAIN));
		authRequest.setComponent(switcherProperties.getValue(ContextKey.COMPONENT));
		authRequest.setEnvironment(switcherProperties.getValue(ContextKey.ENVIRONMENT));

		final String url = switcherProperties.getValue(ContextKey.URL);
		final WebTarget myResource = client.target(String.format(AUTH_URL, url));

		try {
			final Response response = myResource.request(MediaType.APPLICATION_JSON)
					.header(HEADER_APIKEY, switcherProperties.getValue(ContextKey.APIKEY))
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
		final String url = switcherProperties.getValue(ContextKey.URL);
		final WebTarget myResource = client.target(String.format(SNAPSHOT_URL, url));

		final Response response = myResource.request(MediaType.APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, String.format(TOKEN_TEXT, token))
				.post(Entity.json(String.format(QUERY,
						switcherProperties.getValue(ContextKey.DOMAIN),
						switcherProperties.getValue(ContextKey.ENVIRONMENT),
						switcherProperties.getValue(ContextKey.COMPONENT))));

		if (response.getStatus() == 200) {
			final Snapshot snapshot = response.readEntity(SnapshotDataResponse.class).getData();
			response.close();

			return snapshot;
		}

		throw new SwitcherRemoteException(url, response.getStatus());
	}

	@Override
	public SnapshotVersionResponse checkSnapshotVersion(final long version, final String token) {
		final String url = switcherProperties.getValue(ContextKey.URL);
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
					switcherProperties.getValue(ContextKey.URL)));
			final Response response = myResource.request(MediaType.APPLICATION_JSON).get();
			return response.getStatus() == 200;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public SwitchersCheck checkSwitchers(Set<String> switchers, final String token) {
		final String url = switcherProperties.getValue(ContextKey.URL);
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
