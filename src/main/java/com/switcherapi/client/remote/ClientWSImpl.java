package com.switcherapi.client.remote;

import com.switcherapi.client.SwitcherProperties;
import com.switcherapi.client.exception.SwitcherRemoteException;
import com.switcherapi.client.model.ContextKey;
import com.switcherapi.client.model.SwitcherRequest;
import com.switcherapi.client.model.criteria.Snapshot;
import com.switcherapi.client.remote.dto.*;
import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static com.switcherapi.client.remote.Constants.*;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class ClientWSImpl implements ClientWS {

	private final SwitcherProperties switcherProperties;

	private final HttpClient client;

	private final int timeoutMs;

	private final Gson gson = new Gson();
	
	public ClientWSImpl(SwitcherProperties switcherProperties, HttpClient client, int timeoutMs) {
		this.switcherProperties = switcherProperties;
		this.timeoutMs = timeoutMs;
		this.client = client;
	}

	public static ClientWS build(SwitcherProperties switcherProperties, ExecutorService executorService, int timeoutMs) {
		final HttpClient httpClient = ClientWSBuilder.builder(executorService, switcherProperties)
				.connectTimeout(Duration.ofMillis(timeoutMs))
				.build();

		return new ClientWSImpl(switcherProperties, httpClient, timeoutMs);
	}
	
	@Override
	public CriteriaResponse executeCriteria(final CriteriaRequest criteriaRequest, final String token) {
		final String url = switcherProperties.getValue(ContextKey.URL);

		try {
			final URI uri = new URI(url)
					.resolve(String.format(CRITERIA_URL, url,
							SwitcherRequest.KEY, criteriaRequest.getSwitcherKey(),
							SwitcherRequest.SHOW_REASON, Boolean.TRUE,
							SwitcherRequest.BYPASS_METRIC, criteriaRequest.isBypassMetric()));

			final HttpResponse<String> response = client.send(HttpRequest.newBuilder()
					.uri(uri)
					.headers(HEADER_AUTHORIZATION, String.format(TOKEN_TEXT, token),
							HEADER_CONTENT_TYPE, HEADER_JSON)
					.timeout(Duration.ofMillis(timeoutMs))
					.POST(HttpRequest.BodyPublishers.ofString(gson.toJson(criteriaRequest.getInputRequest())))
					.build(), HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200) {
				throw new SwitcherRemoteException(url, response.statusCode());
			}

			final CriteriaResponse criteriaResponse = gson.fromJson(response.body(), CriteriaResponse.class);
			criteriaResponse.setSwitcherKey(criteriaRequest.getSwitcherKey());
			return criteriaResponse;
		} catch (Exception e) {
			return exceptionHandler(e, url);
		}
	}
	
	@Override
	public Optional<AuthResponse> auth() {
		final AuthRequest authRequest = new AuthRequest();
		authRequest.setDomain(switcherProperties.getValue(ContextKey.DOMAIN));
		authRequest.setComponent(switcherProperties.getValue(ContextKey.COMPONENT));
		authRequest.setEnvironment(switcherProperties.getValue(ContextKey.ENVIRONMENT));

		final String url = switcherProperties.getValue(ContextKey.URL);

		try {
			final HttpResponse<String> response = client.send(HttpRequest.newBuilder()
					.uri(URI.create(String.format(AUTH_URL, url)))
					.headers(HEADER_APIKEY, switcherProperties.getValue(ContextKey.APIKEY),
							HEADER_CONTENT_TYPE, HEADER_JSON)
					.timeout(Duration.ofMillis(timeoutMs))
					.POST(HttpRequest.BodyPublishers.ofString(gson.toJson(authRequest))
					).build(), HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200) {
				throw new SwitcherRemoteException(url, response.statusCode());
			}

			return Optional.of(gson.fromJson(response.body(), AuthResponse.class));
		} catch (Exception e) {
			return exceptionHandler(e, url);
		}
	}
	
	@Override
	public Snapshot resolveSnapshot(final String token) {
		final String url = switcherProperties.getValue(ContextKey.URL);

		try {
			final HttpResponse<String> response = client.send(HttpRequest.newBuilder()
					.uri(URI.create(String.format(SNAPSHOT_URL, url)))
					.headers(HEADER_AUTHORIZATION, String.format(TOKEN_TEXT, token),
							HEADER_CONTENT_TYPE, HEADER_JSON)
					.timeout(Duration.ofMillis(timeoutMs))
					.POST(HttpRequest.BodyPublishers.ofString(String.format(QUERY,
							switcherProperties.getValue(ContextKey.DOMAIN),
							switcherProperties.getValue(ContextKey.ENVIRONMENT),
							switcherProperties.getValue(ContextKey.COMPONENT)))
					).build(), HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200) {
				throw new SwitcherRemoteException(url, response.statusCode());
			}

			return gson.fromJson(response.body(), SnapshotDataResponse.class).getData();
		} catch (Exception e) {
			return exceptionHandler(e, url);
		}
	}
	
	@Override
	public SnapshotVersionResponse checkSnapshotVersion(final long version, final String token) {
		final String url = switcherProperties.getValue(ContextKey.URL);

		try {
			final HttpResponse<String> response = client.send(HttpRequest.newBuilder()
					.uri(URI.create(String.format(SNAPSHOT_VERSION_CHECK, url, version)))
					.headers(HEADER_AUTHORIZATION, String.format(TOKEN_TEXT, token),
							HEADER_CONTENT_TYPE, HEADER_JSON)
					.timeout(Duration.ofMillis(timeoutMs))
					.GET().build(), HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200) {
				throw new SwitcherRemoteException(url, response.statusCode());
			}

			return gson.fromJson(response.body(), SnapshotVersionResponse.class);
		} catch (Exception e) {
			return exceptionHandler(e, url);
		}
	}
	
	@Override
	public boolean isAlive() {
		try {
			HttpResponse<String> response = client.send(HttpRequest.newBuilder()
					.uri(URI.create(String.format(CHECK_URL, switcherProperties.getValue(ContextKey.URL))))
					.timeout(Duration.ofMillis(timeoutMs))
					.GET().build(), HttpResponse.BodyHandlers.ofString());

			return response.statusCode() == 200;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public SwitchersCheck checkSwitchers(Set<String> switchers, final String token) {
		final String url = switcherProperties.getValue(ContextKey.URL);

		try {
			final HttpResponse<String> response = client.send(HttpRequest.newBuilder()
					.uri(URI.create(String.format(CHECK_SWITCHERS, url)))
					.timeout(Duration.ofMillis(timeoutMs))
					.headers(HEADER_AUTHORIZATION, String.format(TOKEN_TEXT, token),
							HEADER_CONTENT_TYPE, HEADER_JSON)
					.POST(HttpRequest.BodyPublishers.ofString(gson.toJson(new SwitchersCheck(switchers)))
					).build(), HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200) {
				throw new SwitcherRemoteException(url, response.statusCode());
			}

			return gson.fromJson(response.body(), SwitchersCheck.class);
		} catch (Exception e) {
			return exceptionHandler(e, url);
		}
	}

	private <T> T exceptionHandler(Exception e, String url) {
		if (e instanceof InterruptedException) {
			Thread.currentThread().interrupt();
		}
		throw new SwitcherRemoteException(url, e);
	}

}
