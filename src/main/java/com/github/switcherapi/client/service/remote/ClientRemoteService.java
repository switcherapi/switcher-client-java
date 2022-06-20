package com.github.switcherapi.client.service.remote;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

import com.github.switcherapi.client.SwitcherContextBase;
import com.github.switcherapi.client.exception.SwitcherAPIConnectionException;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherInvalidDateTimeArgumentException;
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

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class ClientRemoteService {
	
	private static ClientRemoteService instance;
	
	private ClientWS clientWs;
	
	private Optional<AuthResponse> authResponse = Optional.empty();
	
	private ClientRemoteService() {
		this.clientWs = new ClientWSImpl();
	}
	
	public static ClientRemoteService getInstance() {
		if (instance == null) {
			instance = new ClientRemoteService();
		}
		return instance;
	}
	
	public CriteriaResponse executeCriteria(final Switcher switcher)  {
		if (!this.isTokenValid()) {
			this.auth();
		}
		
		return this.clientWs.executeCriteriaService(
				switcher, this.authResponse.get().getToken());
	}
	
	public Snapshot resolveSnapshot() throws SwitcherException {
		if (!this.isTokenValid()) {
			this.auth();
		}
		
		return this.clientWs.resolveSnapshot(
				this.authResponse.orElseGet(AuthResponse::new).getToken());
	}
	
	public boolean checkSnapshotVersion(final long version) {
		if (!this.isTokenValid()) {
			this.auth();
		}
				
		final SnapshotVersionResponse snapshotVersionResponse = this.clientWs.checkSnapshotVersion(version, 
				this.authResponse.orElseGet(AuthResponse::new).getToken());

		return snapshotVersionResponse.isUpdated();
	}
	
	public SwitchersCheck checkSwitchers(final Set<String> switchers) {
		try {
			if (!this.isTokenValid()) {
				this.auth();
			}
					
			return this.clientWs.checkSwitchers(switchers, 
					this.authResponse.orElseGet(AuthResponse::new).getToken());
		} catch (final Exception e) {
			throw new SwitcherAPIConnectionException(SwitcherContextBase.contextStr(ContextKey.URL), e);
		}
	}
	
	private void auth() {
		try {
			this.authResponse = this.clientWs.auth();
		} catch (final SwitcherException e) {
			throw e;
		} catch (final Exception e) {
			this.setSilentModeExpiration();
			throw new SwitcherAPIConnectionException(SwitcherContextBase.contextStr(ContextKey.URL), e);
		}
	}
	
	private boolean isTokenValid() throws SwitcherAPIConnectionException, 
		SwitcherInvalidDateTimeArgumentException {
		
		if (this.authResponse.isPresent()) {
			if (this.authResponse.get().getToken().equals(ContextKey.SILENT_MODE.getParam()) 
					&& !this.authResponse.get().isExpired()) {
				throw new SwitcherAPIConnectionException(SwitcherContextBase.contextStr(ContextKey.URL));
			} else {
				if (!this.clientWs.isAlive()) {
					this.setSilentModeExpiration();
					throw new SwitcherAPIConnectionException(SwitcherContextBase.contextStr(ContextKey.URL));
				}
				
				return !this.authResponse.orElseGet(AuthResponse::new).isExpired();
			}
		}
		
		return false;
	}
	
	private void setSilentModeExpiration() throws SwitcherInvalidDateTimeArgumentException {
		if (SwitcherContextBase.contextBol(ContextKey.SILENT_MODE)) {
			final String addValue = SwitcherContextBase.contextStr(ContextKey.RETRY_AFTER);
			final AuthResponse response = new AuthResponse();
			
			response.setToken(ContextKey.SILENT_MODE.getParam());
			response.setExp(SwitcherUtils.addTimeDuration(addValue, new Date()).getTime()/1000);
			this.authResponse = Optional.of(response);
		}
	}

	public void clearAuthResponse() {
		this.authResponse = Optional.empty();
	}

}
