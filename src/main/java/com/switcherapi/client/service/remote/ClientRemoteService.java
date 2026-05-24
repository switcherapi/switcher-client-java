package com.switcherapi.client.service.remote;

import com.switcherapi.client.SwitcherProperties;
import com.switcherapi.client.exception.SwitcherException;
import com.switcherapi.client.exception.SwitcherInvalidDateTimeArgumentException;
import com.switcherapi.client.exception.SwitcherRemoteException;
import com.switcherapi.client.model.ContextKey;
import com.switcherapi.client.model.criteria.Snapshot;
import com.switcherapi.client.remote.ClientWS;
import com.switcherapi.client.remote.dto.AuthResponse;
import com.switcherapi.client.remote.dto.CriteriaRequest;
import com.switcherapi.client.remote.dto.CriteriaResponse;
import com.switcherapi.client.remote.dto.SnapshotVersionResponse;
import com.switcherapi.client.remote.dto.SwitchersCheck;
import com.switcherapi.client.utils.SwitcherUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class ClientRemoteService implements ClientRemote {

	private static final Logger log = LoggerFactory.getLogger(ClientRemoteService.class);

	private final ScheduledExecutorService scheduledExecutorService;

	private final SwitcherProperties switcherProperties;

	private final ClientWS clientWs;
	
	private AuthResponse authResponse;

	private ScheduledFuture<?> refreshFuture;

	private enum TokenStatus {
		VALID, INVALID, SILENT
	}
	
	public ClientRemoteService(ClientWS clientWs, SwitcherProperties switcherProperties,
							   ScheduledExecutorService scheduledExecutorService) {
		this.clientWs = clientWs;
		this.switcherProperties = switcherProperties;
		this.scheduledExecutorService = scheduledExecutorService;
	}

	@Override
	public CriteriaResponse executeCriteria(final CriteriaRequest criteriaRequest)  {
		final TokenStatus tokenStatus = this.isTokenValid();

		try {
			this.auth(tokenStatus);

			return this.clientWs.executeCriteria(criteriaRequest,
					Optional.of(this.authResponse).orElseGet(AuthResponse::new).getToken());
		} catch (final SwitcherRemoteException e) {
			if (tokenStatus != TokenStatus.SILENT) {
				this.setSilentModeExpiration();
			}

			throw e;
		}
	}

	@Override
	public Snapshot resolveSnapshot() throws SwitcherException {
		this.auth(this.isTokenValid());
		
		return this.clientWs.resolveSnapshot(
				Optional.of(this.authResponse).orElseGet(AuthResponse::new).getToken());
	}

	@Override
	public boolean checkSnapshotVersion(final long version) {
		this.auth(this.isTokenValid());

		final SnapshotVersionResponse snapshotVersionResponse = this.clientWs.checkSnapshotVersion(version,
				Optional.of(this.authResponse).orElseGet(AuthResponse::new).getToken());

		return snapshotVersionResponse.isUpdated();
	}

	@Override
	public SwitchersCheck checkSwitchers(final Set<String> switchers) {
		final TokenStatus tokenStatus = this.isTokenValid();

		try {
			this.auth(tokenStatus);

			return this.clientWs.checkSwitchers(switchers,
					Optional.of(this.authResponse).orElseGet(AuthResponse::new).getToken());
		} catch (final SwitcherRemoteException e) {
			if (tokenStatus != TokenStatus.SILENT) {
				this.setSilentModeExpiration();
			}

			throw e;
		}
	}

	private void auth(TokenStatus tokenStatus) {
		if (tokenStatus == TokenStatus.INVALID) {
			log.debug("Auth token is invalid or expired. Attempting to authenticate...");
			this.authResponse = this.clientWs.auth().orElseGet(AuthResponse::new);

			if (isAutoRefreshable()) {
				scheduleNextAuth();
			}
		}

		if (tokenStatus == TokenStatus.SILENT) {
			throw new SwitcherRemoteException(switcherProperties.getValue(ContextKey.URL));
		}
	}
	
	private TokenStatus isTokenValid() throws SwitcherRemoteException,
		SwitcherInvalidDateTimeArgumentException {

		final Optional<AuthResponse> optAuthResponse = Optional.ofNullable(this.authResponse);

		if (!optAuthResponse.isPresent()) {
			return TokenStatus.INVALID;
		}

		if (ContextKey.SILENT_MODE.getParam().equals(optAuthResponse.get().getToken())
				&& !optAuthResponse.get().isExpired()) {
			return TokenStatus.SILENT;
		}

		return optAuthResponse.orElseGet(AuthResponse::new).isExpired() ?
				TokenStatus.INVALID : TokenStatus.VALID;
	}
	
	private void setSilentModeExpiration() throws SwitcherInvalidDateTimeArgumentException {
		if (StringUtils.isNotBlank(switcherProperties.getValue(ContextKey.SILENT_MODE))) {
			final String addValue = switcherProperties.getValue(ContextKey.SILENT_MODE);
			final AuthResponse response = new AuthResponse();
			
			response.setToken(ContextKey.SILENT_MODE.getParam());
			response.setExp(SwitcherUtils.addTimeDuration(addValue, new Date()).getTime()/1000);
			this.authResponse = response;
		}
	}

	private void scheduleNextAuth() {
		long msUntilExpiry = (authResponse.getExp() * 1000L) - (System.currentTimeMillis());
		long refreshAt = Math.max(msUntilExpiry - 5000, 0); // 5s before expiry

		terminateAutoRefresh();
		refreshFuture = scheduledExecutorService.schedule(() -> {
			try {
				log.debug("Auto-refreshing auth token...");
				this.authResponse = this.clientWs.auth().orElseGet(AuthResponse::new);
				scheduleNextAuth();
			} catch (Exception e) {
				log.error("Failed to auto-refresh auth token: {}", e.getMessage());
				terminateAutoRefresh();
			}
		}, refreshAt, TimeUnit.MILLISECONDS);
	}

	private boolean isAutoRefreshable() {
		return switcherProperties.getBoolean(ContextKey.AUTH_AUTO_REFRESH) &&
				(Objects.isNull(refreshFuture) || refreshFuture.isDone());
	}

	private void terminateAutoRefresh() {
		if (Objects.nonNull(refreshFuture)) {
			refreshFuture.cancel(true);
			refreshFuture = null;
			log.debug("Terminated existing auto-refresh task.");
		}
	}
}


