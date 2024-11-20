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
import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class ClientWSImpl implements ClientWS {
	
	public static final String QUERY =
			"{\"query\":\"{ domain(name: \\\"%s\\\", environment: \\\"%s\\\", _component: \\\"%s\\\") { " +
			"name version description activated " +
			"group { name description activated " +
			"config { key description activated " +
			"strategies { strategy activated operation values } " +
			"components } } } }\"}";
	
	private final HttpClient client;

	private final int timeoutMs;

	private final Gson gson = new Gson();
	
	public ClientWSImpl() {
		timeoutMs = Integer.parseInt(Objects.nonNull(SwitcherContextBase.contextStr(ContextKey.TIMEOUT_MS)) ?
				SwitcherContextBase.contextStr(ContextKey.TIMEOUT_MS) : String.valueOf(DEFAULT_TIMEOUT));

		this.client = ClientWSBuilder.builder()
				.connectTimeout(Duration.ofMillis(timeoutMs))
				.build();
	}
	
	@Override
	public CriteriaResponse executeCriteriaService(final Switcher switcher, final String token) {
		final String url = SwitcherContextBase.contextStr(ContextKey.URL);

		try {
			final URI uri = new URI(url)
					.resolve(String.format(CRITERIA_URL, url,
							Switcher.KEY, switcher.getSwitcherKey(),
							Switcher.SHOW_REASON, Boolean.TRUE,
							Switcher.BYPASS_METRIC, switcher.isBypassMetrics()));

			final HttpResponse<String> response = client.send(HttpRequest.newBuilder()
					.uri(uri)
					.headers(HEADER_AUTHORIZATION, String.format(TOKEN_TEXT, token),
							CONTENT_TYPE[0], CONTENT_TYPE[1])
					.timeout(Duration.ofMillis(timeoutMs))
					.POST(HttpRequest.BodyPublishers.ofString(gson.toJson(switcher.getInputRequest())))
					.build(), HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200) {
				throw new SwitcherRemoteException(url, response.statusCode());
			}

			final CriteriaResponse criteriaResponse = gson.fromJson(response.body(), CriteriaResponse.class);
			criteriaResponse.setSwitcherKey(switcher.getSwitcherKey());
			criteriaResponse.setEntry(switcher.getEntry());
			return criteriaResponse;
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

		try {
			final HttpResponse<String> response = client.send(HttpRequest.newBuilder()
					.uri(URI.create(String.format(AUTH_URL, url)))
					.headers(HEADER_APIKEY, SwitcherContextBase.contextStr(ContextKey.APIKEY),
							CONTENT_TYPE[0], CONTENT_TYPE[1])
					.timeout(Duration.ofMillis(timeoutMs))
					.POST(HttpRequest.BodyPublishers.ofString(gson.toJson(authRequest)))
					.build(), HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200) {
				throw new SwitcherRemoteException(url, response.statusCode());
			}

			return Optional.of(gson.fromJson(response.body(), AuthResponse.class));
		} catch (Exception e) {
			throw new SwitcherRemoteException(url, e);
		}
	}
	
	@Override
	public Snapshot resolveSnapshot(final String token) {
		final String url = SwitcherContextBase.contextStr(ContextKey.URL);

		try {
			final HttpResponse<String> response = client.send(HttpRequest.newBuilder()
					.uri(URI.create(String.format(SNAPSHOT_URL, url)))
					.headers(HEADER_AUTHORIZATION, String.format(TOKEN_TEXT, token),
							CONTENT_TYPE[0], CONTENT_TYPE[1])
					.timeout(Duration.ofMillis(timeoutMs))
					.POST(HttpRequest.BodyPublishers.ofString(String.format(QUERY,
							SwitcherContextBase.contextStr(ContextKey.DOMAIN),
							SwitcherContextBase.contextStr(ContextKey.ENVIRONMENT),
							SwitcherContextBase.contextStr(ContextKey.COMPONENT)))
					).build(), HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200) {
				throw new SwitcherRemoteException(url, response.statusCode());
			}

			return gson.fromJson(response.body(), Snapshot.class);
		} catch (Exception e) {
			throw new SwitcherRemoteException(url, e);
		}
	}
	
	@Override
	public SnapshotVersionResponse checkSnapshotVersion(final long version, final String token) {
		final String url = SwitcherContextBase.contextStr(ContextKey.URL);

		try {
			final HttpResponse<String> response = client.send(HttpRequest.newBuilder()
					.uri(URI.create(String.format(SNAPSHOT_VERSION_CHECK, url, version)))
					.headers(HEADER_AUTHORIZATION, String.format(TOKEN_TEXT, token),
							CONTENT_TYPE[0], CONTENT_TYPE[1])
					.timeout(Duration.ofMillis(timeoutMs))
					.GET().build(), HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200) {
				throw new SwitcherRemoteException(url, response.statusCode());
			}

			return gson.fromJson(response.body(), SnapshotVersionResponse.class);
		} catch (Exception e) {
			throw new SwitcherRemoteException(url, e);
		}
	}
	
	@Override
	public boolean isAlive() {
		try {
			HttpResponse<String> response = client.send(HttpRequest.newBuilder()
					.uri(URI.create(String.format(CHECK_URL, SwitcherContextBase.contextStr(ContextKey.URL))))
					.timeout(Duration.ofMillis(timeoutMs))
					.GET().build(), HttpResponse.BodyHandlers.ofString());

			return response.statusCode() == 200;
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public SwitchersCheck checkSwitchers(Set<String> switchers, final String token) {
		final String url = SwitcherContextBase.contextStr(ContextKey.URL);

		try {
			final HttpResponse<String> response = client.send(HttpRequest.newBuilder()
					.uri(URI.create(String.format(CHECK_SWITCHERS, url)))
					.headers(HEADER_AUTHORIZATION, String.format(TOKEN_TEXT, token),
							CONTENT_TYPE[0], CONTENT_TYPE[1])
					.POST(HttpRequest.BodyPublishers.ofString(gson.toJson(new SwitchersCheck(switchers)))
					).build(), HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200) {
				throw new SwitcherRemoteException(url, response.statusCode());
			}

			return gson.fromJson(response.body(), SwitchersCheck.class);
		} catch (Exception e) {
			throw new SwitcherRemoteException(url, e);
		}
	}

}
