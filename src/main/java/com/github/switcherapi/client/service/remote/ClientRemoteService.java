package com.github.switcherapi.client.service.remote;

import com.github.switcherapi.client.SwitcherContextBase;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherInvalidDateTimeArgumentException;
import com.github.switcherapi.client.exception.SwitcherRemoteException;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.github.switcherapi.client.model.criteria.SwitchersCheck;
import com.github.switcherapi.client.model.response.AuthResponse;
import com.github.switcherapi.client.model.response.CriteriaResponse;
import com.github.switcherapi.client.model.response.SnapshotVersionResponse;
import com.github.switcherapi.client.remote.ClientWS;
import com.github.switcherapi.client.remote.ClientWSImpl;
import com.github.switcherapi.client.utils.SwitcherUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class ClientRemoteService {
	
	private final ClientWS clientWs;
	
	private AuthResponse authResponse;

	private enum TokenStatus {
		VALID, INVALID, SILENT
	}

	private enum Singleton {
		INSTANCE;

		private final ClientRemoteService instance = new ClientRemoteService();

		public ClientRemoteService getInstance() {
			return this.instance;
		}
	}
	
	private ClientRemoteService() {
		this.clientWs = new ClientWSImpl();
	}
	
	public static ClientRemoteService getInstance() {
		return Singleton.INSTANCE.getInstance();
	}
	
	public CriteriaResponse executeCriteria(final Switcher switcher)  {
		final TokenStatus tokenStatus = this.isTokenValid();

		try {
			this.auth(tokenStatus);

			return this.clientWs.executeCriteriaService(switcher,
					Optional.of(this.authResponse).orElseGet(AuthResponse::new).getToken());
		} catch (final SwitcherRemoteException e) {
			if (tokenStatus != TokenStatus.SILENT) {
				this.setSilentModeExpiration();
			}

			throw e;
		}
	}

	public Snapshot resolveSnapshot() throws SwitcherException {
		this.auth(this.isTokenValid());
		
		return this.clientWs.resolveSnapshot(
				Optional.of(this.authResponse).orElseGet(AuthResponse::new).getToken());
	}
	
	public boolean checkSnapshotVersion(final long version) {
		this.auth(this.isTokenValid());

		final SnapshotVersionResponse snapshotVersionResponse = this.clientWs.checkSnapshotVersion(version,
				Optional.of(this.authResponse).orElseGet(AuthResponse::new).getToken());

		return snapshotVersionResponse.isUpdated();
	}
	
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
			this.authResponse = this.clientWs.auth().orElseGet(AuthResponse::new);
		}

		if (tokenStatus == TokenStatus.SILENT) {
			throw new SwitcherRemoteException(SwitcherContextBase.contextStr(ContextKey.URL));
		}
	}
	
	private TokenStatus isTokenValid() throws SwitcherRemoteException,
		SwitcherInvalidDateTimeArgumentException {

		final Optional<AuthResponse> optAuthResponse = Optional.ofNullable(this.authResponse);

		if (!optAuthResponse.isPresent()) {
			return TokenStatus.INVALID;
		}

		if (optAuthResponse.get().getToken().equals(ContextKey.SILENT_MODE.getParam())
				&& !optAuthResponse.get().isExpired()) {
			return TokenStatus.SILENT;
		}

		return optAuthResponse.orElseGet(AuthResponse::new).isExpired() ?
				TokenStatus.INVALID : TokenStatus.VALID;
	}
	
	private void setSilentModeExpiration() throws SwitcherInvalidDateTimeArgumentException {
		if (StringUtils.isNotBlank(SwitcherContextBase.contextStr(ContextKey.SILENT_MODE))) {
			final String addValue = SwitcherContextBase.contextStr(ContextKey.SILENT_MODE);
			final AuthResponse response = new AuthResponse();
			
			response.setToken(ContextKey.SILENT_MODE.getParam());
			response.setExp(SwitcherUtils.addTimeDuration(addValue, new Date()).getTime()/1000);
			this.authResponse = response;
		}
	}

	public void clearAuthResponse() {
		this.authResponse = null;
	}

}
